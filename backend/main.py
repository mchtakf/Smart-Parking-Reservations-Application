"""
ESTÜ Otopark - Yoğunluk Tahmin API'si
FastAPI backend - Keras LSTM modeli ile çalışır.

Kullanım:
  1) python convert_and_run.py   (ilk seferde modeli dönüştürür)
  2) python main.py              (sunucuyu başlatır)
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
import numpy as np
import os
import json
import math
import logging

# --------------- Logging ---------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("prediction_api")

# --------------- FastAPI ---------------
app = FastAPI(
    title="ESTÜ Otopark Yoğunluk Tahmin API",
    description="LSTM modeli ile otopark yoğunluk tahmini",
    version="1.0.0",
)

# --------------- Scaler Sabitleri ---------------
FEATURE_MIN = np.array([
    -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
     1.0,  1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
], dtype=np.float32)

FEATURE_MAX = np.array([
     1.0,  1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
     1.0,  1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
], dtype=np.float32)

TARGET_MIN = np.array([0.0, 0.0], dtype=np.float32)
TARGET_MAX = np.array([236.0, 20008.0], dtype=np.float32)

FEATURE_COUNT = 26


# --------------- Model Yükleme ---------------
keras_model = None


def load_model():
    """
    Modeli yükle. Önce .keras, sonra .h5, en son .tflite'tan dönüştürmeyi dener.
    """
    global keras_model

    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'  # TF uyarılarını azalt
    import tensorflow as tf

    # Yol 1: Keras modeli varsa doğrudan yükle
    if os.path.exists("lstm_model_95.keras"):
        try:
            keras_model = tf.keras.models.load_model("lstm_model_95.keras")
            logger.info("Model yuklendi: lstm_model_95.keras")
            return
        except Exception as e:
            logger.warning(f"Keras model yuklenemedi: {e}")

    # Yol 2: .h5 modeli varsa yükle
    if os.path.exists("lstm_model_95.h5"):
        try:
            keras_model = tf.keras.models.load_model("lstm_model_95.h5")
            logger.info("Model yuklendi: lstm_model_95.h5")
            return
        except Exception as e:
            logger.warning(f"H5 model yuklenemedi: {e}")

    # Yol 3: Model config'den yeniden oluştur + tflite'tan ağırlık çıkar
    if os.path.exists("lstm_model_95.tflite"):
        try:
            keras_model = _rebuild_from_tflite(tf)
            if keras_model is not None:
                logger.info("Model tflite'tan yeniden olusturuldu.")
                return
        except Exception as e:
            logger.warning(f"TFLite donusturme hatasi: {e}")

    logger.error("HATA: Hicbir model dosyasi yuklenemedi!")


def _rebuild_from_tflite(tf):
    """
    Bilinen mimariyi (scalers_95.json) kullanarak modeli yeniden oluştur
    ve TFLite'tan ağırlıkları çıkar.
    """
    from tensorflow import keras

    # Modeli mimariden oluştur
    model = keras.Sequential([
        keras.layers.Input(shape=(24, 26)),
        keras.layers.LSTM(64, activation='relu'),
        keras.layers.Dense(32, activation='relu'),
        keras.layers.Dense(2, activation='linear'),
    ])
    model.compile(optimizer='adam', loss='mse')

    # TFLite'tan ağırlıkları çıkar
    try:
        interp = tf.lite.Interpreter(model_path="lstm_model_95.tflite")
        interp.allocate_tensors()

        # Tensor detaylarını al
        tensor_details = interp.get_tensor_details()

        # Keras ağırlık sırası: lstm_kernel, lstm_recurrent, lstm_bias, dense_kernel, dense_bias, dense1_kernel, dense1_bias
        keras_weight_names = [w.name for w in model.weights]
        logger.info(f"Keras agirliklari: {keras_weight_names}")

        tflite_weights = {}
        for td in tensor_details:
            name = td['name']
            try:
                data = interp.get_tensor(td['index'])
                tflite_weights[name] = data
                logger.info(f"  TFLite tensor: {name}, shape={data.shape}")
            except:
                pass

        # Ağırlıkları eşleştirmeyi dene
        new_weights = []
        for kw in model.weights:
            matched = False
            for tn, tw in tflite_weights.items():
                if tw.shape == kw.shape:
                    new_weights.append(tw)
                    matched = True
                    break
            if not matched:
                new_weights.append(kw.numpy())

        model.set_weights(new_weights)
        model.save("lstm_model_95.keras")
        return model

    except RuntimeError as e:
        if "Select TensorFlow" in str(e):
            logger.error("TFLite Flex ops hatasi - model donusturulemiyor.")
            logger.error("Lutfen orijinal .keras veya .h5 model dosyasini backend/ klasorune kopyalayin.")
        raise


@app.on_event("startup")
async def startup_event():
    load_model()


# --------------- Yardımcı Fonksiyonlar ---------------
def scale_feature(value: float, index: int) -> float:
    feature_range = FEATURE_MAX[index] - FEATURE_MIN[index]
    if feature_range == 0:
        return 0.0
    return (value - FEATURE_MIN[index]) / feature_range


def inverse_target(scaled_value: float, index: int) -> float:
    result = scaled_value * (TARGET_MAX[index] - TARGET_MIN[index]) + TARGET_MIN[index]
    return max(0.0, result)


def create_feature_vector(date_str: str, hour: int) -> np.ndarray:
    from datetime import datetime as dt
    parsed = dt.strptime(date_str, "%d/%m/%Y")
    month = parsed.month - 1
    day_of_week = parsed.weekday()

    hour_rad = 2.0 * math.pi * hour / 24.0
    sin_h = math.sin(hour_rad)
    cos_h = math.cos(hour_rad)

    is_holiday = 1.0 if day_of_week >= 5 else 0.0

    day_vec = [0.0] * 7
    day_vec[day_of_week] = 1.0

    season_vec = [0.0] * 4
    if month in [1, 2, 3]:
        season_vec[1] = 1.0
    elif month in [4, 5, 6]:
        season_vec[2] = 1.0
    elif month in [7, 8, 9]:
        season_vec[3] = 1.0
    else:
        season_vec[0] = 1.0

    month_vec = [0.0] * 12
    month_vec[month] = 1.0

    raw = [sin_h, cos_h, is_holiday] + day_vec + season_vec + month_vec
    scaled = [scale_feature(raw[i], i) for i in range(len(raw))]
    return np.array(scaled, dtype=np.float32)


def map_to_density(vehicle_count: float, avg_minutes: float) -> str:
    if vehicle_count <= 40 and avg_minutes <= 80:
        return "DÜŞÜK"
    elif vehicle_count <= 40:
        return "ORTA"
    elif vehicle_count <= 100 and avg_minutes <= 180:
        return "ORTA"
    elif vehicle_count <= 100:
        return "YÜKSEK"
    else:
        return "YÜKSEK"


# --------------- Request / Response ---------------
class PredictionRequest(BaseModel):
    date: str = Field(..., description="Tarih (dd/MM/yyyy)", example="15/03/2026")
    hour: int = Field(..., ge=0, le=23, description="Saat (0-23)", example=14)


class PredictionResponse(BaseModel):
    vehicle_count: int = Field(..., description="Tahmini araç sayısı")
    avg_park_minutes: int = Field(..., description="Ortalama park süresi (dk)")
    density: str = Field(..., description="Yoğunluk seviyesi: DÜŞÜK / ORTA / YÜKSEK")
    date: str = Field(..., description="Sorgulanan tarih")
    hour: int = Field(..., description="Sorgulanan saat")


# --------------- API Endpoint'leri ---------------
@app.get("/")
async def root():
    return {
        "message": "ESTÜ Otopark Yoğunluk Tahmin API",
        "status": "active",
        "model_loaded": keras_model is not None,
    }


@app.get("/health")
async def health():
    return {
        "status": "healthy" if keras_model is not None else "model_not_loaded",
        "model_loaded": keras_model is not None,
    }


@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """Otopark yoğunluk tahmini yapar."""
    if keras_model is None:
        raise HTTPException(
            status_code=503,
            detail="Model yüklenmedi. Lütfen .keras veya .h5 model dosyasını backend/ klasörüne koyun."
        )

    from datetime import datetime as dt, timedelta

    try:
        parsed = dt.strptime(request.date, "%d/%m/%Y")
    except ValueError:
        raise HTTPException(status_code=400, detail="Geçersiz tarih formatı. dd/MM/yyyy olmalı.")

    try:
        input_data = np.zeros((1, 24, FEATURE_COUNT), dtype=np.float32)
        current_dt = parsed.replace(hour=request.hour, minute=0, second=0)

        for i in range(23, -1, -1):
            date_str = current_dt.strftime("%d/%m/%Y")
            current_hour = current_dt.hour
            input_data[0][i] = create_feature_vector(date_str, current_hour)
            current_dt -= timedelta(hours=1)

        # Keras model ile tahmin
        output = keras_model.predict(input_data, verbose=0)

        vehicle_count = inverse_target(output[0][0], 0)
        avg_minutes = inverse_target(output[0][1], 1)
        density = map_to_density(vehicle_count, avg_minutes)

        logger.info(
            f"Tahmin: {request.date} {request.hour}:00 -> "
            f"Arac={int(vehicle_count)}, Sure={int(avg_minutes)}dk, "
            f"Yogunluk={density}"
        )

        return PredictionResponse(
            vehicle_count=int(vehicle_count),
            avg_park_minutes=int(avg_minutes),
            density=density,
            date=request.date,
            hour=request.hour,
        )

    except Exception as e:
        logger.error(f"Tahmin hatasi: {e}")
        raise HTTPException(status_code=500, detail=f"Tahmin sırasında hata: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
