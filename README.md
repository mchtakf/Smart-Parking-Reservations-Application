# ESTÜ Otopark - Elektrikli Araç Şarj & Park Yönetim Sistemi

Eskişehir Teknik Üniversitesi kampüsü için geliştirilmiş, yapay zeka destekli otopark yoğunluk tahmini ve elektrikli araç şarj istasyonu rezervasyon sistemidir. Android mobil uygulama ve FastAPI backend olmak üzere iki bileşenden oluşmaktadır.

---

## Özellikler

### Kullanıcı Yönetimi
- Firebase Authentication ile e-posta/şifre tabanlı kayıt ve giriş sistemi
- Firebase Firestore üzerinde kullanıcı profili ve araç bilgileri yönetimi
- Profil güncelleme ve güvenli çıkış işlemleri

### Otopark Rezervasyonu
- Tarih, giriş/çıkış saati ve pil doluluk oranı seçimi ile rezervasyon oluşturma
- Ödeme ekranı ile kart bilgileri girişi ve rezervasyon onaylama

### Yapay Zeka ile Yoğunluk Tahmini
- LSTM (Long Short-Term Memory) sinir ağı modeli ile otopark yoğunluk tahmini
- %95 doğruluk oranına sahip tahmin modeli
- **Hibrit mimari**: Öncelikli olarak backend API'den, internet yoksa cihaz üzerinde (TFLite) tahmin
- Yoğunluk seviyesi renk kodlu gösterim (Düşük / Orta / Yüksek)
- Tahmini araç sayısı ve ortalama park süresi bilgisi

### Harita & Konum
- Google Maps entegrasyonu ile kampüs haritası
- Şarj istasyonu konumu ve detay bilgileri (güç, durum, çalışma saatleri)

### Modern Arayüz
- Jetpack Compose ile Material Design 3 tasarım sistemi
- Gradient arka planlar, gölgeli kartlar, ikonlu giriş alanları
- Alt navigasyon çubuğu ile kolay ekran geçişi

---

## Teknolojiler

### Android (Kotlin)
| Teknoloji | Kullanım Alanı |
|-----------|---------------|
| Jetpack Compose | Modern UI framework |
| Material Design 3 | Tasarım sistemi |
| Firebase Auth | Kullanıcı doğrulama |
| Firebase Firestore | Bulut veritabanı |
| TensorFlow Lite | Cihaz üzerinde ML (offline) |
| Retrofit + OkHttp | REST API iletişimi |
| Google Maps Compose | Harita görüntüleme |
| Navigation Compose | Ekran yönlendirme |
| Kotlin Coroutines | Asenkron işlemler |

### Backend (Python)
| Teknoloji | Kullanım Alanı |
|-----------|---------------|
| FastAPI | REST API framework |
| TensorFlow / Keras | ML model çıkarımı |
| Uvicorn | ASGI sunucusu |
| Pydantic | Veri doğrulama |

---

## Proje Yapısı

```
ESTU-Otopark/
├── app/
│   └── src/main/
│       ├── assets/
│       │   ├── lstm_model_95.tflite     # LSTM tahmin modeli
│       │   └── scalers_95.json          # Model mimarisi
│       ├── java/com/example/myapplication/
│       │   ├── MainActivity.kt
│       │   ├── MainViewModel.kt
│       │   ├── network/
│       │   │   ├── ApiClient.kt         # Retrofit istemcisi
│       │   │   └── PredictionApi.kt     # API arayüzü
│       │   └── ui/theme/
│       │       ├── Color.kt             # Renk tanımları
│       │       ├── Theme.kt             # Tema yapılandırması
│       │       ├── auth/
│       │       │   ├── AuthViewModel.kt       # İş mantığı
│       │       │   ├── LoginScreen.kt         # Giriş ekranı
│       │       │   ├── SignupScreen.kt        # Kayıt ekranı
│       │       │   ├── ProfileScreen.kt       # Profil ekranı
│       │       │   ├── RezervasyonSayfasi.kt  # Rezervasyon ekranı
│       │       │   ├── PredictionSection.kt   # Tahmin bileşeni
│       │       │   ├── PaymentScreen.kt       # Ödeme ekranı
│       │       │   ├── NavigationScreen.kt    # Harita ekranı
│       │       │   └── BottomNavigationBar.kt # Alt menü
│       │       └── navigation/
│       │           ├── AppNavHost.kt          # Navigasyon yönlendirici
│       │           └── NavigationItem.kt      # Rota tanımları
│       ├── res/
│       └── AndroidManifest.xml
├── backend/
│   ├── main.py                # FastAPI sunucusu
│   ├── convert_model.py       # TFLite → Keras dönüştürücü
│   ├── requirements.txt       # Python bağımlılıkları
│   └── Dockerfile             # Docker yapılandırması
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Kurulum

### Gereksinimler
- Android Studio Hedgehog (2023.1.1) veya üzeri
- JDK 11+
- Python 3.10+
- Google Maps API Anahtarı
- Firebase Projesi (Auth + Firestore)

### Android Uygulaması

1. Projeyi klonlayın:
```bash
git clone https://github.com/KULLANICI_ADIN/ESTU-Otopark.git
cd ESTU-Otopark
```

2. Android Studio ile açın ve Gradle sync yapın.

3. `google-services.json` dosyanızı `app/` klasörüne ekleyin (Firebase Console'dan alın).

4. `AndroidManifest.xml` içindeki Google Maps API anahtarını kendi anahtarınızla değiştirin.

5. Uygulamayı derleyin ve çalıştırın.

### Backend Sunucusu

1. Backend klasörüne gidin:
```bash
cd backend
```

2. Bağımlılıkları kurun:
```bash
pip install -r requirements.txt
```

3. Model dosyasını hazırlayın:
```bash
# TFLite modelini kopyalayın
cp ../app/src/main/assets/lstm_model_95.tflite .

# Keras modeline dönüştürün
python convert_model.py
```

4. Sunucuyu başlatın:
```bash
python main.py
```

5. API dokümantasyonu: `http://localhost:8000/docs`

### Docker ile Çalıştırma

```bash
cd backend
docker build -t estu-otopark-api .
docker run -p 8000:8000 estu-otopark-api
```

---

## API Endpoint'leri

| Metot | Endpoint | Açıklama |
|-------|----------|----------|
| `GET` | `/` | API durumu |
| `GET` | `/health` | Sağlık kontrolü |
| `POST` | `/predict` | Yoğunluk tahmini |

### Tahmin İsteği Örneği

```json
POST /predict
{
  "date": "15/03/2026",
  "hour": 14
}
```

### Tahmin Yanıtı

```json
{
  "vehicle_count": 85,
  "avg_park_minutes": 120,
  "density": "ORTA",
  "date": "15/03/2026",
  "hour": 14
}
```

---

## ML Model Detayları

| Özellik | Değer |
|---------|-------|
| Model Tipi | LSTM (Sequential) |
| Doğruluk | %95 |
| Giriş Boyutu | (24, 26) - 24 saatlik pencere, 26 özellik |
| Çıkış | 2 değer (araç sayısı, ortalama park süresi) |
| Özellikler | Saat (sin/cos), gün, ay, mevsim, tatil günü |
| Çıkarım | API öncelikli, offline fallback (TFLite) |

### Özellik Mühendisliği
- **Zamansal kodlama**: Saat bilgisi sinüs/kosinüs ile döngüsel olarak kodlanır
- **Kategorik değişkenler**: Gün (7), mevsim (4) ve ay (12) one-hot encoding
- **Min-Max normalizasyon**: Tüm özellikler [0,1] aralığına ölçeklenir

---

## Ekran Görüntüleri

> Ekran görüntülerini `screenshots/` klasörüne ekleyerek bu bölümü güncelleyebilirsiniz.

| Giriş | Rezervasyon | Tahmin | Harita | Profil |
|-------|-------------|--------|--------|--------|
| ![Login](screenshots/login.png) | ![Reservation](screenshots/reservation.png) | ![Prediction](screenshots/prediction.png) | ![Map](screenshots/map.png) | ![Profile](screenshots/profile.png) |

---

## Mimari

```
┌─────────────────┐     ┌─────────────────┐
│  Android Uygulama│     │  FastAPI Backend │
│                 │     │                 │
│  ┌───────────┐  │     │  ┌───────────┐  │
│  │  Compose  │  │     │  │   Keras   │  │
│  │    UI     │  │     │  │   LSTM    │  │
│  └─────┬─────┘  │     │  └─────┬─────┘  │
│        │        │     │        │        │
│  ┌─────┴─────┐  │ API │  ┌─────┴─────┐  │
│  │ ViewModel │◄─┼─────┼─►│  /predict  │  │
│  └─────┬─────┘  │     │  └───────────┘  │
│        │        │     │                 │
│  ┌─────┴─────┐  │     └─────────────────┘
│  │  TFLite   │  │
│  │ (Offline) │  │
│  └───────────┘  │
│        │        │
│  ┌─────┴─────┐  │
│  │ Firebase  │  │
│  │Auth+Store │  │
│  └───────────┘  │
└─────────────────┘
```

---

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakınız.

---

## İletişim

**Geliştirici**: Mücahit
**E-posta**: mucahitakfidan11@gmail.com
