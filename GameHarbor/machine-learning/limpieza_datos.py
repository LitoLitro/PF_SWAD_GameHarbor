"""
GameHarbor - Limpieza de datos
------------------------------
Lee los datos de ejemplo de la BD (database/GameHarborInserts.sql, exportado desde SSMS),
los convierte en tablas, elimina nulos/vacíos y guarda un CSV limpio por tabla en data/limpio/.

Uso:
    python limpieza_datos.py
    python limpieza_datos.py --sql ruta/a/GameHarborInserts.sql --salida data/limpio
"""
import argparse
import os
import re

import numpy as np
import pandas as pd

BASE = os.path.dirname(os.path.abspath(__file__))
SQL_POR_DEFECTO = os.path.join(BASE, "..", "database", "GameHarborInserts.sql")
SALIDA_POR_DEFECTO = os.path.join(BASE, "data", "limpio")

# Columnas que no aportan al análisis y contienen datos sensibles
COLUMNAS_SENSIBLES = ["USUARIO_PASSWORD_HASH"]


# ---------------------------------------------------------------- lectura
def leer_texto(ruta):
    """Lee el script respetando la codificación (SSMS exporta en UTF-16)."""
    with open(ruta, "rb") as f:
        crudo = f.read()
    if crudo[:2] in (b"\xff\xfe", b"\xfe\xff"):
        texto = crudo.decode("utf-16")
    else:
        texto = crudo.decode("utf-8-sig")
    return texto.replace("\r", "")


def dividir(texto, sep=","):
    """Divide por 'sep' respetando comillas simples y paréntesis."""
    partes, actual, nivel, en_cadena, i = [], "", 0, False, 0
    while i < len(texto):
        c = texto[i]
        if c == "'":
            if en_cadena and i + 1 < len(texto) and texto[i + 1] == "'":
                actual += "''"
                i += 2
                continue
            en_cadena = not en_cadena
        elif not en_cadena:
            if c == "(":
                nivel += 1
            elif c == ")":
                nivel -= 1
            elif c == sep and nivel == 0:
                partes.append(actual.strip())
                actual, i = "", i + 1
                continue
        actual += c
        i += 1
    if actual.strip():
        partes.append(actual.strip())
    return partes


def valor_sql(tok):
    """Convierte un literal de SQL Server a un valor de Python."""
    tok = tok.strip()
    if tok.upper() == "NULL":
        return None
    m = re.fullmatch(r"CAST\((.*)\s+AS\s+(\w+).*\)", tok, flags=re.S | re.I)
    if m:
        valor, tipo = valor_sql(m.group(1)), m.group(2).lower()
        if tipo in ("date", "datetime", "datetime2"):
            return pd.Timestamp(valor)
        if tipo in ("decimal", "numeric", "float", "money"):
            return float(valor)
        return valor
    m = re.fullmatch(r"N?'(.*)'", tok, flags=re.S)
    if m:
        return m.group(1).replace("''", "'")
    if re.fullmatch(r"-?\d+", tok):
        return int(tok)
    if re.fullmatch(r"-?\d+\.\d+", tok):
        return float(tok)
    return tok


def cargar_tablas(ruta_sql):
    """Devuelve {NOMBRE_TABLA: DataFrame} a partir de los INSERT del script."""
    texto = leer_texto(ruta_sql)
    patron = re.compile(
        r"INSERT\s+(?:INTO\s+)?(?:\[?dbo\]?\.)?\[?(\w+)\]?\s*\((.*?)\)\s*VALUES\s*(.*?)(?=\n\s*(?:INSERT|SET|GO)\b|\Z)",
        flags=re.S | re.I,
    )
    filas = {}
    for tabla, columnas, valores in patron.findall(texto):
        columnas = [c.strip().strip("[]") for c in columnas.split(",")]
        for tupla in dividir(valores.strip().rstrip(";")):
            tupla = tupla.strip()[1:-1]
            filas.setdefault(tabla.upper(), []).append(
                dict(zip(columnas, [valor_sql(v) for v in dividir(tupla)]))
            )
    return {t: pd.DataFrame(r) for t, r in filas.items()}


# ---------------------------------------------------------------- limpieza
def contar_vacios(df):
    """Cuenta NULL + cadenas vacías por columna."""
    vacios_txt = df.apply(
        lambda s: s.astype(str).str.strip().eq("") if s.dtype == object else pd.Series(False, index=s.index)
    )
    return df.isna().sum() + vacios_txt.sum()


def limpiar(tablas):
    limpias, bitacora = {}, []
    for t, df in tablas.items():
        d = df.copy()
        filas_ini, cols_ini = d.shape
        # 1) recortar textos y convertir '' en nulo
        for c in [c for c in d.columns if d[c].map(lambda v: isinstance(v, str)).any()]:
            d[c] = d[c].apply(lambda v: v.strip() if isinstance(v, str) else v).replace("", np.nan)
        # 2) eliminar columnas 100 % vacías
        cols_vacias = [c for c in d.columns if d[c].isna().all()]
        d = d.drop(columns=cols_vacias)
        # 3) quitar datos sensibles
        sensibles = [c for c in COLUMNAS_SENSIBLES if c in d.columns]
        d = d.drop(columns=sensibles)
        # 4) eliminar filas con nulos y duplicados (sin contar la columna ID)
        d = d.dropna()
        d = d.drop_duplicates(subset=list(d.columns[1:]) or None)
        limpias[t] = d.reset_index(drop=True)
        bitacora.append({
            "tabla": t,
            "filas_antes": filas_ini,
            "filas_despues": len(d),
            "columnas_antes": cols_ini,
            "columnas_despues": d.shape[1],
            "nulos_antes": int(contar_vacios(df).sum()),
            "nulos_despues": int(contar_vacios(d).sum()),
            "columnas_eliminadas": ", ".join(cols_vacias + sensibles) or "-",
        })
    return limpias, pd.DataFrame(bitacora)


def main():
    ap = argparse.ArgumentParser(description="Limpieza de datos de GameHarbor")
    ap.add_argument("--sql", default=SQL_POR_DEFECTO, help="Script SQL con los INSERT")
    ap.add_argument("--salida", default=SALIDA_POR_DEFECTO, help="Carpeta de salida de los CSV")
    args = ap.parse_args()
    main_args(args.sql, args.salida)


def main_args(ruta_sql, salida):
    tablas = cargar_tablas(ruta_sql)
    limpias, bitacora = limpiar(tablas)

    os.makedirs(salida, exist_ok=True)
    for t, d in limpias.items():
        d.to_csv(os.path.join(salida, f"{t}.csv"), index=False, encoding="utf-8")
    bitacora.to_csv(os.path.join(salida, "_bitacora_limpieza.csv"), index=False, encoding="utf-8")

    print(f"Tablas leídas: {len(tablas)}")
    print(f"Celdas nulas/vacías antes: {bitacora.nulos_antes.sum()} | después: {bitacora.nulos_despues.sum()}")
    print(bitacora[bitacora.columnas_eliminadas != "-"][["tabla", "columnas_eliminadas"]].to_string(index=False))
    print(f"CSV limpios guardados en: {os.path.abspath(salida)}")


if __name__ == "__main__":
    main()
