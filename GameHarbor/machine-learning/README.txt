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