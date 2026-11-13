"""
GameHarbor - API REST del modelo de recomendación (Flask)
--------------------------------------------------------
Endpoints:
    GET  /health      → verifica que el servicio esté activo
    POST /recommend   → recomendaciones para un usuario   body: {"usuario_id": 2, "top_n": 3}
    POST /train       → reentrena el modelo con los datos actuales (limpieza + entrenamiento)

Uso:
    pip install -r requirements.txt
    python app.py                 # http://localhost:5000
"""
import os

import joblib
from flask import Flask, jsonify, request

import limpieza_datos
import train_model

BASE = os.path.dirname(os.path.abspath(__file__))
RUTA_MODELO = train_model.RUTA_MODELO

app = Flask(__name__)
estado = {"modelo": None, "features": None, "reales": None, "biblioteca": None}


def cargar():
    """Carga el modelo (lo entrena si no existe) y los datos de los usuarios reales."""
    if not os.path.exists(os.path.join(train_model.DIR_LIMPIO, "JUEGO.csv")):
        limpieza_datos.main_args(limpieza_datos.SQL_POR_DEFECTO, limpieza_datos.SALIDA_POR_DEFECTO)
    if not os.path.exists(RUTA_MODELO):
        train_model.main()
    datos = joblib.load(RUTA_MODELO)
    juegos, cats_por_juego, _ = train_model.preparar_catalogo()
    reales, biblioteca = train_model.dataset_real(juegos, cats_por_juego, datos["features"])
    estado.update(modelo=datos["modelo"], features=datos["features"], reales=reales, biblioteca=biblioteca)


@app.get("/health")
def health():
    return jsonify(status="ok", modelo_cargado=estado["modelo"] is not None)


@app.post("/recommend")
def recommend():
    body = request.get_json(silent=True) or {}
    try:
        usuario_id = int(body["usuario_id"])
        top_n = int(body.get("top_n", 3))
    except (KeyError, TypeError, ValueError):
        return jsonify(error="Envíe un JSON con 'usuario_id' (entero) y opcionalmente 'top_n'"), 400

    reales = estado["reales"]
    if usuario_id not in set(reales.USUARIO_ID):
        return jsonify(error=f"Usuario {usuario_id} no encontrado o no es CLIENTE"), 404

    recs = train_model.recomendar(estado["modelo"], reales[reales.USUARIO_ID == usuario_id],
                                  estado["biblioteca"], estado["features"], top_n=top_n)
    return jsonify(
        usuario_id=usuario_id,
        recomendaciones=[
            {"juego_id": int(r.JUEGO_ID), "titulo": r.JUEGO_TITULO,
             "puntaje": float(r.RECOMENDACION_PUNTAJE), "origen": r.RECOMENDACION_ORIGEN}
            for r in recs.itertuples()
        ],
    )


@app.post("/train")
def train():
    limpieza_datos.main_args(limpieza_datos.SQL_POR_DEFECTO, limpieza_datos.SALIDA_POR_DEFECTO)
    train_model.main()
    cargar()
    metricas = train_model.pd.read_csv(os.path.join(train_model.DIR_DATA, "metricas.csv"), index_col=0)
    return jsonify(status="reentrenado", metricas=metricas["valor"].to_dict())


cargar()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 5000)))
