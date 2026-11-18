# 🤖 Machine Learning — GameHarbor

Carpeta que contiene el **modelo de Machine Learning** encargado de generar recomendaciones personalizadas de videojuegos para cada usuario de GameHarbor.

## ¿Qué hace esta carpeta?
1. **Entrena** un modelo de recomendación con los datos de interacción del usuario (vistas, reseñas, compras).
2. **Expone** una **API REST** (Flask o FastAPI) para que el backend Java pueda pedirle recomendaciones.
3. **Guarda** las recomendaciones generadas en la base de datos SQL Server.

## Estructura principal

machine-learning/
├── app.py → API REST del modelo
├── train_model.py → script de entrenamiento
├── model.pkl → modelo entrenado (no se sube al repo)
├── requirements.txt → dependencias Python
├── data/
│ └── dataset.csv → datos de entrenamiento
└── notebooks/
└── exploracion.ipynb → análisis exploratorio


## Tecnologías
- Python 3.10+
- Pandas / NumPy
- Scikit-learn (filtrado colaborativo o content-based)
- Flask o FastAPI
- Pickle para persistir el modelo

## Cómo ejecutar
```bash
cd machine-learning
pip install -r requirements.txt
python train_model.py        # entrena y genera model.pkl
python app.py                # levanta la API en http://localhost:5000

Endpoints principales
Método	Ruta	Descripción
POST	/recommend	Devuelve recomendaciones para un usuario
POST	/train	Reentrena el modelo con datos actuales
GET	/health	Verifica que el servicio esté activo
Integración con el backend
El backend Java (capa ml/MlClient.java) llama a esta API después de generar nuevas compras o vistas, y luego guarda los resultados en la tabla RECOMENDACION de SQL Server.

## Modelo implementado: Árbol de Decisión (scikit-learn)

Archivos:
- limpieza_datos.py  → lee database/GameHarborInserts.sql, elimina nulos/vacíos y genera data/limpio/*.csv
- app.py             → API REST (Flask): GET /health, POST /recommend {"usuario_id": 2, "top_n": 3}, POST /train
- train_model.py     → arma el dataset usuario × juego, entrena el árbol y genera model.pkl
- data/limpio/       → 18 tablas limpias en CSV + _bitacora_limpieza.csv
- data/dataset.csv   → dataset de entrenamiento (usuarios SIMULADOS sobre el catálogo real)
- data/dataset_real.csv → dataset con los 3 clientes reales (validación)
- data/recomendaciones.csv → top 3 por cliente (formato de la tabla RECOMENDACION)
- data/metricas.csv  → accuracy, precision, recall, F1, ROC-AUC
- notebooks/exploracion.ipynb → versión Colab con gráficos

Ejecutar:
    pip install -r requirements.txt
    python limpieza_datos.py
    python train_model.py
    python app.py      # http://localhost:5000

Variable objetivo INTERES = 1 si el usuario tiene el juego en biblioteca, en carrito o lo vio ≥ 60 s.
Nota: la BD de ejemplo solo tiene 3 clientes (15 filas), por eso se entrena con datos simulados;
en producción se reemplaza por el historial real de Azure SQL.
