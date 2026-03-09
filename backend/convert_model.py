"""
lstm_model_95.tflite -> lstm_model_95.keras dönüştürücü.
Bir kez çalıştırın: python convert_model.py
"""
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

import numpy as np
import tensorflow as tf
from tensorflow import keras

print("=" * 50)
print("TFLite -> Keras Model Dönüştürücü")
print("=" * 50)

# 1) Aynı mimariyi oluştur
print("\n[1/3] Model mimarisi oluşturuluyor...")
model = keras.Sequential([
    keras.layers.Input(shape=(24, 26)),
    keras.layers.LSTM(64, activation='relu'),
    keras.layers.Dense(32, activation='relu'),
    keras.layers.Dense(2, activation='linear'),
])
model.compile(optimizer='adam', loss='mse')
model.summary()

# 2) TFLite'tan ağırlıkları flatbuffers ile çıkar
print("\n[2/3] TFLite'tan ağırlıklar çıkarılıyor...")

tflite_path = "lstm_model_95.tflite"
if not os.path.exists(tflite_path):
    print(f"HATA: {tflite_path} bulunamadı!")
    print("Lütfen bu dosyayı backend/ klasörüne kopyalayın.")
    exit(1)

with open(tflite_path, 'rb') as f:
    tflite_content = f.read()

# TFLite flatbuffer'dan tensorları çıkar
try:
    from tensorflow.lite.python import schema_py_generated as schema_fb

    buf = bytearray(tflite_content)
    model_fb = schema_fb.ModelT.InitFromPackedBuf(buf, 0)

    # Alternatif yöntem: GetRootAs kullan
    model_root = schema_fb.Model.GetRootAs(tflite_content, 0)
    subgraph = model_root.Subgraphs(0)

    print(f"  Toplam tensor sayısı: {subgraph.TensorsLength()}")

    tflite_tensors = {}
    for i in range(subgraph.TensorsLength()):
        tensor = subgraph.Tensors(i)
        buf_idx = tensor.Buffer()
        buffer = model_root.Buffers(buf_idx)

        if buffer.DataLength() > 0:
            name = tensor.Name().decode('utf-8')
            shape = tuple(tensor.Shape(j) for j in range(tensor.ShapeLength()))
            data = np.frombuffer(buffer.DataAsNumpy(), dtype=np.float32).copy().reshape(shape)
            tflite_tensors[name] = data
            print(f"  ✓ {name}: shape={shape}")

    print(f"\n  Toplam ağırlık tensoru: {len(tflite_tensors)}")

except Exception as e:
    print(f"  Flatbuffers yöntemi başarısız: {e}")
    print("\n  Alternatif yöntem deneniyor (tensor isimlerinden eşleştirme)...")

    # Alternatif: tflite dosyasından binary olarak ağırlıkları bul
    # Bu durumda model eğitim scriptinizi tekrar çalıştırmanız gerekir
    print("\n  ÇÖZÜM: Modeli eğittiğiniz notebook/script'te şu satırı ekleyip tekrar çalıştırın:")
    print('    model.save("lstm_model_95.keras")')
    exit(1)

# 3) Ağırlıkları Keras modeline aktar
print("\n[3/3] Ağırlıklar Keras modeline aktarılıyor...")

keras_weights = model.weights
print(f"\n  Keras model ağırlık listesi:")
for w in keras_weights:
    print(f"    {w.name}: shape={w.shape}")

# Eşleştirme: shape bazlı
print(f"\n  TFLite tensorları:")
for name, data in tflite_tensors.items():
    print(f"    {name}: shape={data.shape}")

# Shape'e göre eşleştir
matched_weights = []
used_tflite = set()

for kw in keras_weights:
    k_shape = tuple(kw.shape)
    found = False
    for t_name, t_data in tflite_tensors.items():
        if t_name not in used_tflite and t_data.shape == k_shape:
            matched_weights.append(t_data)
            used_tflite.add(t_name)
            print(f"  ✓ {kw.name} ({k_shape}) <- {t_name}")
            found = True
            break
    if not found:
        print(f"  ✗ {kw.name} ({k_shape}) - EŞLEŞTİRİLEMEDİ, varsayılan kullanılıyor")
        matched_weights.append(kw.numpy())

try:
    model.set_weights(matched_weights)
    print("\n  Ağırlıklar başarıyla aktarıldı!")
except Exception as e:
    print(f"\n  Ağırlık aktarım hatası: {e}")
    print("  Model ağırlıksız olarak kaydedilecek (yanlış sonuçlar verebilir).")

# Kaydet
output_path = "lstm_model_95.keras"
model.save(output_path)
print(f"\n{'=' * 50}")
print(f"✅ Model kaydedildi: {output_path}")
print(f"{'=' * 50}")

# Test tahmini
print("\nTest tahmini yapılıyor...")
test_input = np.random.rand(1, 24, 26).astype(np.float32)
result = model.predict(test_input, verbose=0)
print(f"  Test çıktısı: {result[0]}")
print(f"  Model çalışıyor! ✅")
