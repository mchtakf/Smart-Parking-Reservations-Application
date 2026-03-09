package com.example.myapplication.ui.theme.auth

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.ApiClient
import com.example.myapplication.network.PredictionRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * ----------------------------------------------------------
 * TensorFlow Lite – Tek Seferlik Yükleme Yardımcısı
 * (Offline fallback olarak korunuyor)
 * ----------------------------------------------------------
 */
object Predictor {
    private var interpreter: Interpreter? = null
    private var isInitialized = false

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        try {
            val model = FileUtil.loadMappedFile(context, "lstm_model_95.tflite")
            interpreter = Interpreter(model)
            isInitialized = true
            Log.d("Predictor", "✅ Interpreter yüklendi (offline fallback hazır).")
        } catch (e: Exception) {
            Log.e("Predictor", "❌ Interpreter yüklenemedi", e)
        }
    }

    fun run(
        input: Array<Array<FloatArray>>,
        output: Array<FloatArray>
    ): Array<FloatArray>? {
        if (!isInitialized) {
            Log.e("Predictor", "❌ run(): Interpreter başlatılmamış.")
            return null
        }
        return try {
            interpreter?.run(input, output)
            output
        } catch (e: Exception) {
            Log.e("Predictor", "❌ run() çağrısı sırasında hata", e)
            null
        }
    }
}

/**
 * ----------------------------------------------------------
 * Min-Max Scaler Sabitleri (Python'dan aktarıldı)
 * ----------------------------------------------------------
 */
object Scaler {
    private val FEATURE_MIN = floatArrayOf(
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
    )
    private val FEATURE_MAX = floatArrayOf(
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
    )
    private val TARGET_MIN = floatArrayOf(0.0f, 0.0f)
    private val TARGET_MAX = floatArrayOf(236.0f, 20008.0f)

    fun scaleFeature(value: Float, index: Int): Float {
        val range = FEATURE_MAX[index] - FEATURE_MIN[index]
        return if (range == 0f) 0f else (value - FEATURE_MIN[index]) / range
    }

    fun inverseTarget(scaledValue: Float, index: Int): Float {
        val res = scaledValue * (TARGET_MAX[index] - TARGET_MIN[index]) + TARGET_MIN[index]
        return max(0f, res)
    }
}

data class UserProfileData(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val vehicleName: String = "",
    val vehiclePlate: String = "",
    val vehicleModel: String = ""
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class PredictionUiState(
    val vehicleCount: Int,
    val avgParkMinutes: Int,
    val density: String,
    val source: PredictionSource = PredictionSource.LOCAL
)

/**
 * Tahminin nereden geldiğini belirtir
 */
enum class PredictionSource {
    API,    // Backend sunucusundan
    LOCAL   // Cihaz üzerinde TFLite
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signUpState = _signUpState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    val userProfileData = _userProfile.asStateFlow()

    private val _predictionUi = MutableStateFlow<PredictionUiState?>(null)
    val predictionUi = _predictionUi.asStateFlow()

    private val _predictionError = MutableStateFlow<String?>(null)
    val predictionError = _predictionError.asStateFlow()

    private val _predictionLoading = MutableStateFlow(false)
    val predictionLoading = _predictionLoading.asStateFlow()

    init {
        auth.currentUser?.let { fetchUserProfile(it) }
        Predictor.init(app)
    }

    // ==================== AUTH ====================

    fun login(email: String, password: String) {
        _loginState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _loginState.value = if (task.isSuccessful) {
                auth.currentUser?.let { fetchUserProfile(it) }
                AuthState.Success("Giriş Başarılı")
            } else {
                AuthState.Error("Giriş Başarısız: ${task.exception?.message}")
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String = "",
        phone: String = "",
        vehicleName: String = "",
        vehiclePlate: String = "",
        vehicleModel: String = ""
    ) {
        _signUpState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                _signUpState.value = AuthState.Error("Kayıt Başarısız: ${task.exception?.message}")
                return@addOnCompleteListener
            }
            val user = auth.currentUser ?: return@addOnCompleteListener
            val data = UserProfileData(name, email, phone, vehicleName, vehiclePlate, vehicleModel)
            db.collection("users").document(user.uid).set(data).addOnSuccessListener {
                fetchUserProfile(user)
                _signUpState.value = AuthState.Success("Kayıt Başarılı")
            }.addOnFailureListener { e ->
                _signUpState.value = AuthState.Error("Veritabanı Hatası: ${e.message}")
            }
        }
    }

    fun updateUserProfile(
        name: String,
        phone: String,
        vehicleName: String,
        vehiclePlate: String,
        vehicleModel: String
    ) {
        val user = auth.currentUser ?: return
        val map = mapOf(
            "name" to name,
            "phone" to phone,
            "vehicleName" to vehicleName,
            "vehiclePlate" to vehiclePlate,
            "vehicleModel" to vehicleModel
        )
        db.collection("users").document(user.uid).set(map, SetOptions.merge()).addOnSuccessListener {
            _userProfile.value = _userProfile.value?.copy(
                name = name,
                phone = phone,
                vehicleName = vehicleName,
                vehiclePlate = vehiclePlate,
                vehicleModel = vehicleModel
            )
        }
    }

    private fun fetchUserProfile(user: FirebaseUser) {
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            _userProfile.value = if (doc.exists()) {
                doc.toObject(UserProfileData::class.java)
            } else {
                UserProfileData(name = user.displayName ?: "", email = user.email ?: "")
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun logout() {
        auth.signOut()
        _userProfile.value = null
    }

    // ==================== TAHMİN ====================

    /**
     * Tahmin yapar: Önce API'yi dener, başarısız olursa cihaz üzerinde TFLite kullanır.
     */
    fun runPredictionForSingle(dateStr: String, hour: Int) {
        viewModelScope.launch {
            _predictionError.value = null
            _predictionUi.value = null
            _predictionLoading.value = true

            // 1) Önce API'yi dene (internet varsa)
            if (isNetworkAvailable()) {
                val apiResult = predictFromApi(dateStr, hour)
                if (apiResult != null) {
                    _predictionUi.value = apiResult
                    _predictionLoading.value = false
                    Log.d("Prediction", "✅ API'den tahmin alındı")
                    return@launch
                }
                Log.w("Prediction", "⚠️ API başarısız, offline fallback deneniyor...")
            } else {
                Log.d("Prediction", "📴 İnternet yok, offline fallback kullanılıyor")
            }

            // 2) Fallback: Cihaz üzerinde TFLite
            val localResult = predictOnDevice(dateStr, hour)
            if (localResult != null) {
                _predictionUi.value = localResult
                _predictionLoading.value = false
                Log.d("Prediction", "✅ Cihaz üzerinde tahmin alındı (offline)")
                return@launch
            }

            // 3) Her ikisi de başarısız
            _predictionError.value = "Tahmin alınamadı. İnternet bağlantınızı kontrol edin."
            _predictionLoading.value = false
        }
    }

    /**
     * API üzerinden tahmin al
     */
    private suspend fun predictFromApi(dateStr: String, hour: Int): PredictionUiState? =
        withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.predictionApi.predict(
                    PredictionRequest(date = dateStr, hour = hour)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    PredictionUiState(
                        vehicleCount = body.vehicle_count,
                        avgParkMinutes = body.avg_park_minutes,
                        density = body.density,
                        source = PredictionSource.API
                    )
                } else {
                    Log.e("PredictAPI", "HTTP ${response.code()}: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("PredictAPI", "API çağrısı başarısız", e)
                null
            }
        }

    /**
     * Cihaz üzerinde TFLite ile tahmin (offline fallback)
     */
    private suspend fun predictOnDevice(dateStr: String, hour: Int): PredictionUiState? =
        withContext(Dispatchers.IO) {
            try {
                val input = Array(1) { Array(24) { FloatArray(FEATURE_COUNT) } }
                val output = Array(1) { FloatArray(2) }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val cal = Calendar.getInstance().apply {
                    time = dateFormat.parse(dateStr) ?: Date()
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                for (i in 23 downTo 0) {
                    val currHour = cal[Calendar.HOUR_OF_DAY]
                    val currDate = dateFormat.format(cal.time)
                    input[0][i] = createFeatureVector(currDate, currHour)
                    cal.add(Calendar.HOUR_OF_DAY, -1)
                }

                Predictor.run(input, output) ?: return@withContext null

                val vehicleCount = Scaler.inverseTarget(output[0][0], 0)
                val avgMinutes = Scaler.inverseTarget(output[0][1], 1)

                PredictionUiState(
                    vehicleCount = vehicleCount.toInt(),
                    avgParkMinutes = avgMinutes.toInt(),
                    density = mapToDensity(vehicleCount, avgMinutes),
                    source = PredictionSource.LOCAL
                )
            } catch (e: Exception) {
                Log.e("PredictLocal", "Cihaz üzerinde tahmin hatası", e)
                null
            }
        }

    /**
     * İnternet bağlantısı kontrolü
     */
    private fun isNetworkAvailable(): Boolean {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ==================== FEATURE ENGINEERING ====================

    private fun createFeatureVector(dateStr: String, hour: Int): FloatArray {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance().apply { time = dateFormat.parse(dateStr) ?: Date() }

        val month = cal[Calendar.MONTH]
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK]

        val hourRad = 2.0 * PI * hour / 24.0
        val sinH = sin(hourRad).toFloat()
        val cosH = cos(hourRad).toFloat()

        val isHoliday = if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) 1f else 0f

        val dayVec = FloatArray(7).apply { this[(dayOfWeek + 5) % 7] = 1f }
        val seasonVec = when (month) {
            in 2..4 -> floatArrayOf(0f, 1f, 0f, 0f)
            in 5..7 -> floatArrayOf(0f, 0f, 1f, 0f)
            in 8..10 -> floatArrayOf(0f, 0f, 0f, 1f)
            else -> floatArrayOf(1f, 0f, 0f, 0f)
        }
        val monthVec = FloatArray(12).apply { this[month] = 1f }

        val raw = floatArrayOf(sinH, cosH, isHoliday) + dayVec + seasonVec + monthVec
        return FloatArray(raw.size) { idx -> Scaler.scaleFeature(raw[idx], idx) }
    }

    private fun mapToDensity(vehicleCount: Float, avgMinutes: Float): String = when {
        vehicleCount <= 40f && avgMinutes <= 80f -> "DÜŞÜK"
        vehicleCount <= 40f -> "ORTA"
        vehicleCount <= 100f && avgMinutes <= 180f -> "ORTA"
        vehicleCount <= 100f -> "YÜKSEK"
        else -> "YÜKSEK"
    }

    companion object {
        private const val FEATURE_COUNT = 26
    }
}
