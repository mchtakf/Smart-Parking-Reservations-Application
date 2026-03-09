# AI-Powered EV Charging & Parking Management System

An intelligent parking density prediction and electric vehicle (EV) charging station reservation system developed for Eskişehir Technical University campus. The project consists of two main components: an Android mobile application built with Jetpack Compose and a FastAPI backend server powered by an LSTM deep learning model.

---

## Features

### User Management
- Email/password-based registration and login via Firebase Authentication
- User profile and vehicle information management on Firebase Firestore
- Profile update and secure logout functionality

### Parking Reservation
- Create reservations by selecting date, entry/exit time, and battery level
- Payment screen with credit card input and reservation confirmation
- Live credit card preview that updates in real-time as the user types

### AI-Powered Parking Density Prediction
- **LSTM (Long Short-Term Memory)** neural network model for parking occupancy forecasting
- **95% accuracy** prediction model trained on historical parking data
- **Hybrid inference architecture**: API-first prediction from backend server, with on-device TFLite fallback when offline
- Color-coded density display (Low / Medium / High) with corresponding icons
- Predicted vehicle count and average parking duration statistics
- Source indicator badge showing whether prediction came from server (API) or device (Offline)

### Map & Location
- Google Maps integration displaying the campus map
- EV charging station location with detail overlay (power output, status, operating hours)

### Modern UI
- Jetpack Compose with Material Design 3 design system
- Gradient backgrounds, elevated cards with shadows, icon-enhanced input fields
- Bottom navigation bar for seamless screen transitions
- Professional color palette with consistent theming across all screens

---

## Technology Stack

### Android Application (Kotlin)

| Technology | Purpose |
|-----------|---------|
| Jetpack Compose | Declarative modern UI framework |
| Material Design 3 | Design system and theming |
| Firebase Authentication | User authentication (email/password) |
| Firebase Firestore | Cloud NoSQL database for user profiles |
| TensorFlow Lite 2.13.0 | On-device ML inference (offline fallback) |
| Retrofit 2.9.0 + OkHttp | REST API communication with backend |
| Gson Converter | JSON serialization/deserialization |
| Google Maps Compose | Interactive map rendering |
| Navigation Compose | Screen routing and navigation graph |
| Kotlin Coroutines + StateFlow | Asynchronous operations and reactive state |
| ViewModel (AndroidX) | MVVM architecture lifecycle-aware state |

### Backend Server (Python)

| Technology | Purpose |
|-----------|---------|
| FastAPI | High-performance async REST API framework |
| TensorFlow / Keras | LSTM model loading and inference |
| Uvicorn | ASGI server for production deployment |
| Pydantic v2 | Request/response data validation and serialization |
| NumPy | Numerical computation for feature engineering |
| Docker | Containerized deployment |

---

## Machine Learning Model

### Model Architecture

The prediction engine uses a **Sequential LSTM neural network** built with TensorFlow/Keras:

```
Input Layer     →  shape: (24, 26)  — 24-hour sliding window × 26 features
LSTM Layer      →  64 units, ReLU activation, return_sequences=False
Dense Layer     →  32 units, ReLU activation
Output Layer    →  2 units, Linear activation (vehicle_count, avg_park_minutes)
```

| Property | Value |
|----------|-------|
| Model Type | LSTM (Long Short-Term Memory) Sequential |
| Framework | TensorFlow / Keras |
| Accuracy | 95% |
| Input Shape | (24, 26) — 24-hour time window with 26 features each |
| Output | 2 values: predicted vehicle count, average parking duration (minutes) |
| On-device Format | TensorFlow Lite (.tflite) |
| Server Format | Keras (.keras) |

### Feature Engineering Pipeline

The model uses **26 engineered features** per time step, constructed from raw datetime information:

1. **Cyclical Hour Encoding** (2 features):
   - `hour_sin = sin(2π × hour / 24)` — captures cyclical nature of time
   - `hour_cos = cos(2π × hour / 24)` — preserves continuity between 23:00 and 00:00

2. **Day-of-Week One-Hot Encoding** (7 features):
   - Binary vector representing Monday through Sunday
   - Captures weekly parking patterns (weekday vs. weekend)

3. **Season One-Hot Encoding** (4 features):
   - Spring (March–May), Summer (June–August), Fall (September–November), Winter (December–February)
   - Accounts for seasonal variations in campus usage

4. **Month One-Hot Encoding** (12 features):
   - Binary vector for each month (January through December)
   - Fine-grained temporal patterns (exam periods, holidays)

5. **Holiday Detection** (1 feature):
   - Binary flag for Turkish national holidays
   - Holidays list: New Year's Day, National Sovereignty Day, Labor Day, Commemoration of Atatürk Day, Victory Day, Republic Day

### Data Normalization

All features and targets use **Min-Max scaling** to normalize values to the [0, 1] range:

```
scaled_value = (value - min) / (max - min)
```

Scaler constants are synchronized between the Android app (`Scaler` object) and the backend server to ensure consistent predictions across both inference paths.

### Prediction Flow (Hybrid Architecture)

```
User Input (date, hour)
        │
        ▼
┌─── Network Available? ───┐
│                          │
▼ YES                     ▼ NO
API Request ──►            │
FastAPI Backend             │
(Keras LSTM)               │
    │                      │
    ▼                      ▼
┌─ Success? ─┐      TFLite Model
│            │      (On-Device)
▼ YES       ▼ NO        │
Return      Fallback ────┘
Result      to TFLite    │
                         ▼
                    Return Result
                  (source: LOCAL)
```

The `AuthViewModel` implements this hybrid strategy:
- **API Path**: Retrofit sends a POST request to `/predict` endpoint → receives JSON response → maps to UI state
- **Offline Path**: TFLite interpreter loads `lstm_model_95.tflite` from assets → runs inference locally → maps to UI state
- **Source Tracking**: Each prediction result includes a `PredictionSource` enum (API or LOCAL) displayed as a badge in the UI

### Model Conversion

The `convert_model.py` script handles TFLite → Keras conversion:
- Rebuilds the LSTM architecture from known model configuration
- Extracts weights from TFLite binary using flatbuffers
- Maps weights by tensor shape to corresponding Keras layers
- Saves the reconstructed model as `.keras` file for server-side inference

---

## Project Structure

```
ESTU-Otopark/
├── app/
│   └── src/main/
│       ├── assets/
│       │   ├── lstm_model_95.tflite        # LSTM model (on-device)
│       │   └── scalers_95.json             # Model architecture config
│       ├── java/com/example/myapplication/
│       │   ├── MainActivity.kt
│       │   ├── MainViewModel.kt
│       │   ├── network/
│       │   │   ├── ApiClient.kt            # Retrofit client singleton
│       │   │   └── PredictionApi.kt        # API interface & data classes
│       │   └── ui/theme/
│       │       ├── Color.kt                # Color palette definitions
│       │       ├── Theme.kt                # Material3 theme config
│       │       ├── auth/
│       │       │   ├── AuthViewModel.kt          # Core business logic & ML
│       │       │   ├── LoginScreen.kt            # Login screen
│       │       │   ├── SignupScreen.kt           # Registration screen
│       │       │   ├── ProfileScreen.kt          # User profile screen
│       │       │   ├── RezervasyonSayfasi.kt     # Reservation screen
│       │       │   ├── PredictionSection.kt      # Density prediction UI
│       │       │   ├── PaymentScreen.kt          # Payment screen
│       │       │   ├── NavigationScreen.kt       # Map screen
│       │       │   └── BottomNavigationBar.kt    # Bottom navigation
│       │       └── navigation/
│       │           ├── AppNavHost.kt             # Navigation graph
│       │           └── NavigationItem.kt         # Route definitions
│       ├── res/
│       │   └── xml/
│       │       └── network_security_config.xml   # HTTP cleartext config
│       └── AndroidManifest.xml
├── backend/
│   ├── main.py                 # FastAPI server with LSTM inference
│   ├── convert_model.py        # TFLite → Keras model converter
│   ├── requirements.txt        # Python dependencies
│   └── Dockerfile              # Docker configuration
├── build.gradle.kts
├── settings.gradle.kts
├── LICENSE
└── README.md
```

---

## Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11+
- Python 3.10+
- Google Maps API Key
- Firebase Project (Authentication + Firestore enabled)

### Android Application

1. Clone the repository:
```bash
git clone https://github.com/YOUR_USERNAME/ESTU-Otopark.git
cd ESTU-Otopark
```

2. Open the project in Android Studio and sync Gradle.

3. Add your `google-services.json` file to the `app/` directory (download from Firebase Console).

4. Replace the Google Maps API key in `AndroidManifest.xml` with your own key.

5. Build and run the application on an emulator or physical device.

### Backend Server

1. Navigate to the backend directory:
```bash
cd backend
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Prepare the model file:
```bash
# Copy the TFLite model
cp ../app/src/main/assets/lstm_model_95.tflite .

# Convert to Keras format
python convert_model.py
```

4. Start the server:
```bash
python main.py
```

5. Access the interactive API docs at: `http://localhost:8000/docs`

### Docker Deployment

```bash
cd backend
docker build -t estu-otopark-api .
docker run -p 8000:8000 estu-otopark-api
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | API status check |
| `GET` | `/health` | Health check with model status |
| `POST` | `/predict` | Parking density prediction |

### Prediction Request

```json
POST /predict
{
  "date": "15/03/2026",
  "hour": 14
}
```

### Prediction Response

```json
{
  "vehicle_count": 85,
  "avg_park_minutes": 120,
  "density": "MEDIUM",
  "date": "15/03/2026",
  "hour": 14
}
```

Density classification thresholds:
- **LOW**: vehicle_count < 50
- **MEDIUM**: 50 ≤ vehicle_count < 100
- **HIGH**: vehicle_count ≥ 100

---

## Architecture Overview

```
┌──────────────────────┐       ┌──────────────────────┐
│   Android App        │       │   FastAPI Backend     │
│                      │       │                      │
│  ┌────────────────┐  │       │  ┌────────────────┐  │
│  │  Jetpack       │  │       │  │  TensorFlow    │  │
│  │  Compose UI    │  │       │  │  Keras LSTM    │  │
│  └───────┬────────┘  │       │  └───────┬────────┘  │
│          │           │       │          │           │
│  ┌───────┴────────┐  │  API  │  ┌───────┴────────┐  │
│  │  AuthViewModel │◄─┼──────┼──►│  /predict      │  │
│  │  (MVVM)        │  │ HTTP  │  │  /health       │  │
│  └───────┬────────┘  │       │  └────────────────┘  │
│          │           │       │                      │
│  ┌───────┴────────┐  │       │  ┌────────────────┐  │
│  │  TFLite Model  │  │       │  │  NumPy         │  │
│  │  (Offline      │  │       │  │  Feature Eng.  │  │
│  │   Fallback)    │  │       │  └────────────────┘  │
│  └───────┬────────┘  │       └──────────────────────┘
│          │           │
│  ┌───────┴────────┐  │
│  │  Firebase       │  │
│  │  Auth + Store   │  │
│  └────────────────┘  │
│          │           │
│  ┌───────┴────────┐  │
│  │  Retrofit +    │  │
│  │  OkHttp Client │  │
│  └────────────────┘  │
└──────────────────────┘
```

---

## App Screens

| Login | Reservation | Prediction | Map | Profile | Payment |
|-------|-------------|------------|-----|---------|---------|
| Gradient background with card-based form | Date/time picker with battery level input | LSTM-powered density forecast with stats | Google Maps with station overlay | Editable profile with vehicle info | Live card preview with payment form |

> Add your screenshots to a `screenshots/` directory and update the table above with image links.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Contact

**Developer**: Mücahit
**Email**: mucahitakfidan11@gmail.com
