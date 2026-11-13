"""
GameHarbor - Entrenamiento del modelo de recomendación (Árbol de Decisión)
-------------------------------------------------------------------------
1. Lee las tablas limpias (data/limpio/*.csv, generadas por limpieza_datos.py).
2. Construye el dataset usuario x juego con sus variables.
3. Amplía los datos con usuarios SIMULADOS sobre el catálogo real (la BD de ejemplo
   solo tiene 3 clientes / 15 filas; en producción se reemplaza por el historial real).
4. Entrena un DecisionTreeClassifier (GridSearchCV + validación cruzada) y lo evalúa.
5. Guarda: model.pkl, data/dataset.csv, data/recomendaciones.csv y data/metricas.csv.

Uso:
    python limpieza_datos.py      # una sola vez, o cuando cambien los datos
    python train_model.py
"""
import os

import joblib
import numpy as np
import pandas as pd
from sklearn.metrics import (accuracy_score, classification_report, confusion_matrix,
                             f1_score, precision_score, recall_score, roc_auc_score)
from sklearn.model_selection import GridSearchCV, StratifiedKFold, train_test_split
from sklearn.tree import DecisionTreeClassifier, export_text

BASE = os.path.dirname(os.path.abspath(__file__))
DIR_LIMPIO = os.path.join(BASE, "data", "limpio")
DIR_DATA = os.path.join(BASE, "data")
RUTA_MODELO = os.path.join(BASE, "model.pkl")

SEED = 42
N_USUARIOS_SIM = 400
SEGUNDOS_MIN_VISTA = 60
FECHA_REF = pd.Timestamp("2026-11-01")  # fecha fija para que el resultado sea reproducible


def leer(tabla):
    return pd.read_csv(os.path.join(DIR_LIMPIO, f"{tabla}.csv"))


# ---------------------------------------------------------------- variables
def preparar_catalogo():
    juego, cat, jc = leer("JUEGO"), leer("CATEGORIA"), leer("JUEGO_CATEGORIA")
    cat = jc.merge(cat, on="CATEGORIA_ID")
    onehot = pd.crosstab(cat.JUEGO_ID, cat.CATEGORIA_NOMBRE).clip(upper=1).add_prefix("CAT_").reset_index()
    cats_por_juego = cat.groupby("JUEGO_ID").CATEGORIA_ID.apply(set).to_dict()

    juegos = juego.merge(onehot, on="JUEGO_ID", how="left").fillna(0)
    juegos["JUEGO_DISPONIBLE"] = (juegos.JUEGO_ESTADO == "DISPONIBLE").astype(int)
    juegos["ANTIGUEDAD_DIAS"] = (FECHA_REF - pd.to_datetime(juegos.JUEGO_FECHA_LANZAMIENTO)).dt.days
    cols_cat = [c for c in juegos.columns if c.startswith("CAT_")]
    return juegos, cats_por_juego, cols_cat


def dataset_real(juegos, cats_por_juego, features):
    usuario, usuario_rol, rol = leer("USUARIO"), leer("USUARIO_ROL"), leer("ROL")
    clientes_ids = usuario_rol.merge(rol, on="ROL_ID").query("ROL_NOMBRE == 'CLIENTE'").USUARIO_ID
    clientes = usuario[usuario.USUARIO_ID.isin(clientes_ids)]

    # Preferencias = categorías de los juegos que el usuario reseñó
    resena = leer("RESENA")
    pref = (resena.assign(cats=resena.JUEGO_ID.map(cats_por_juego))
            .groupby("USUARIO_ID").cats.apply(lambda s: set().union(*s)).to_dict())

    # Objetivo: biblioteca, carrito o vista >= 60 s
    bib = leer("BIBLIOTECA")
    biblioteca = set(zip(bib.USUARIO_ID, bib.JUEGO_ID))
    car = leer("CARRITO_ITEM").merge(leer("CARRITO"), on="CARRITO_ID")
    carrito = set(zip(car.USUARIO_ID, car.JUEGO_ID))
    vis = leer("HISTORIAL_VISTA").query(f"HISTORIAL_SEGUNDOS >= {SEGUNDOS_MIN_VISTA}")
    vistas = set(zip(vis.USUARIO_ID, vis.JUEGO_ID))

    df = clientes[["USUARIO_ID", "USUARIO_NOMBRE", "USUARIO_SALDO"]].merge(juegos, how="cross")
    df["PUEDE_PAGAR"] = (df.USUARIO_SALDO >= df.JUEGO_PRECIO).astype(int)
    df["AFINIDAD_CATEGORIA"] = [int(bool(pref.get(u, set()) & cats_por_juego.get(j, set())))
                                for u, j in zip(df.USUARIO_ID, df.JUEGO_ID)]
    df["INTERES"] = [int((u, j) in biblioteca | carrito | vistas) for u, j in zip(df.USUARIO_ID, df.JUEGO_ID)]
    df[features] = df[features].astype(float)
    return df, biblioteca


def dataset_simulado(juegos, cats_por_juego, cols_cat, features, todas_cats):
    rng = np.random.default_rng(SEED)
    filas = []
    for _ in range(N_USUARIOS_SIM):
        saldo = float(rng.choice([0, 20, 50, 80, 100, 150]))
        prefs = set(rng.choice(todas_cats, size=rng.integers(1, 3), replace=False))
        for _, j in juegos.iterrows():
            afin = int(bool(prefs & cats_por_juego.get(j.JUEGO_ID, set())))
            puede = int(saldo >= j.JUEGO_PRECIO)
            logit = (-1.6 + 2.4 * afin + 0.9 * puede + 0.8 * j.JUEGO_DISPONIBLE
                     - 0.03 * (j.JUEGO_PRECIO - 40) - 0.0006 * (j.ANTIGUEDAD_DIAS - 500))
            fila = {c: j[c] for c in ["JUEGO_PRECIO", "JUEGO_REQUIERE_CLOUD", "JUEGO_DISPONIBLE",
                                      "ANTIGUEDAD_DIAS"] + cols_cat}
            fila.update(USUARIO_SALDO=saldo, PUEDE_PAGAR=puede, AFINIDAD_CATEGORIA=afin,
                        INTERES=int(rng.random() < 1 / (1 + np.exp(-logit))))
            filas.append(fila)
    sim = pd.DataFrame(filas)[features + ["INTERES"]].astype(float)
    sim["INTERES"] = sim.INTERES.astype(int)
    return sim


# ---------------------------------------------------------------- modelo
def entrenar(X_train, y_train):
    grid = GridSearchCV(
        DecisionTreeClassifier(random_state=SEED, class_weight="balanced"),
        {"criterion": ["gini", "entropy"], "max_depth": [2, 3, 4], "min_samples_leaf": [5, 10, 20]},
        cv=StratifiedKFold(5, shuffle=True, random_state=SEED), scoring="f1", n_jobs=-1,
    )
    grid.fit(X_train, y_train)
    return grid.best_estimator_, grid.best_params_, grid.best_score_


def recomendar(modelo, reales, biblioteca, features, top_n=3):
    reales = reales.copy()
    reales["RECOMENDACION_PUNTAJE"] = modelo.predict_proba(reales[features])[:, 1].round(4)
    cand = reales[(reales.JUEGO_DISPONIBLE == 1) &
                  np.array([(u, j) not in biblioteca for u, j in zip(reales.USUARIO_ID, reales.JUEGO_ID)])]
    recs = (cand.sort_values(["USUARIO_ID", "RECOMENDACION_PUNTAJE"], ascending=[True, False])
            .groupby("USUARIO_ID").head(top_n))
    recs = recs[["USUARIO_ID", "USUARIO_NOMBRE", "JUEGO_ID", "JUEGO_TITULO", "RECOMENDACION_PUNTAJE"]].copy()
    recs["RECOMENDACION_ORIGEN"] = "ML"
    return recs


def main():
    juegos, cats_por_juego, cols_cat = preparar_catalogo()
    features = ["JUEGO_PRECIO", "JUEGO_REQUIERE_CLOUD", "JUEGO_DISPONIBLE", "ANTIGUEDAD_DIAS",
                "USUARIO_SALDO", "PUEDE_PAGAR", "AFINIDAD_CATEGORIA"] + cols_cat

    reales, biblioteca = dataset_real(juegos, cats_por_juego, features)
    sim = dataset_simulado(juegos, cats_por_juego, cols_cat, features, sorted(leer("CATEGORIA").CATEGORIA_ID))

    # Dataset de entrenamiento (simulado) para referencia / informe
    sim.assign(ORIGEN="SIMULADO").to_csv(os.path.join(DIR_DATA, "dataset.csv"), index=False, encoding="utf-8")
    reales[["USUARIO_ID", "JUEGO_ID"] + features + ["INTERES"]].to_csv(
        os.path.join(DIR_DATA, "dataset_real.csv"), index=False, encoding="utf-8")

    X, y = sim[features], sim["INTERES"]
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=SEED)
    modelo, params, cv_f1 = entrenar(X_train, y_train)

    y_pred, y_prob = modelo.predict(X_test), modelo.predict_proba(X_test)[:, 1]
    metricas = pd.Series({
        "accuracy": accuracy_score(y_test, y_pred),
        "precision": precision_score(y_test, y_pred),
        "recall": recall_score(y_test, y_pred),
        "f1": f1_score(y_test, y_pred),
        "roc_auc": roc_auc_score(y_test, y_prob),
        "f1_validacion_cruzada": cv_f1,
        "accuracy_datos_reales": accuracy_score(reales.INTERES, modelo.predict(reales[features])),
    }).round(3)
    metricas.to_frame("valor").to_csv(os.path.join(DIR_DATA, "metricas.csv"), encoding="utf-8")

    recs = recomendar(modelo, reales, biblioteca, features)
    recs.to_csv(os.path.join(DIR_DATA, "recomendaciones.csv"), index=False, encoding="utf-8")

    joblib.dump({"modelo": modelo, "features": features}, RUTA_MODELO)

    print("Mejores hiperparámetros:", params)
    print("\nMétricas (conjunto de prueba):\n" + metricas.to_string())
    print("\nMatriz de confusión:\n", confusion_matrix(y_test, y_pred))
    print("\n" + classification_report(y_test, y_pred, target_names=["Sin interés", "Con interés"]))
    print("Reglas del árbol:\n" + export_text(modelo, feature_names=features))
    print("Recomendaciones:\n" + recs.to_string(index=False))
    print(f"\nModelo guardado en: {RUTA_MODELO}")


if __name__ == "__main__":
    main()
