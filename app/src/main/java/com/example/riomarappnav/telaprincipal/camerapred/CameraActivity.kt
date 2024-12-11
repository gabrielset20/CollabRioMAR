package com.example.riomarappnav.telaprincipal.camerapred

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.riomarappnav.R
import com.example.riomarappnav.database.FirestoreRepository
import com.example.riomarappnav.modelYolov8n.BoundingBox
import com.example.riomarappnav.modelYolov8n.Constants.LABELS_PATH
import com.example.riomarappnav.modelYolov8n.Constants.MODEL_PATH
import com.example.riomarappnav.modelYolov8n.Detector
import com.example.riomarappnav.telaprincipal.HomeActivity
import com.example.riomarappnav.telaprincipal.telaRanking.RankingActivity
import com.example.riomarappnav.telaprincipal.SettingsActivity
import com.example.riomarappnav.trophiesgenerator.TrophyGenerator
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.GeoPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity(), Detector.DetectorListener {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var detector: Detector
    private lateinit var cameraButton: ImageButton
    private lateinit var top10predicoesDeClasseCompleta: List<String>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var localizacaoAtual: String
    private lateinit var longi: Number
    private lateinit var lati: Number

    @SuppressLint("MissingPermission", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        top10predicoesDeClasseCompleta = listOf(" ")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,
                    false) || permissions.getOrDefault(Manifest.permission
                    .ACCESS_COARSE_LOCATION, false) -> {
                        Toast.makeText(this, "acesso a localizacao permitido",
                            Toast.LENGTH_LONG).show()

                    if (isLocationEnabled()){
                        val result = fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            CancellationTokenSource().token
                        )
                        result.addOnCompleteListener {
                            val location =
                                "Latitude: " + it.result.latitude + "\n" + "Longitude: " +
                                        it.result.longitude
                            localizacaoAtual = location
                            lati = it.result.latitude
                            longi = it.result.longitude
                        }
                    } else {
                        Toast.makeText(this, "Por favor, ligue a localizacao.",
                            Toast.LENGTH_LONG)
                                .show()
                            createLocationRequest()
                        }
                    }
                    else -> {
                        Toast.makeText(this, "acesso a localizacao negado",Toast
                            .LENGTH_SHORT).show()
                    }
                }
        }

        previewView = findViewById(R.id.previewView)
        cameraButton = findViewById(R.id.capture_button)
        cameraExecutor = Executors.newSingleThreadExecutor()


        // Inicializar o Detector do modelo yolov8
        try {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
            detector.setup() // Configura o modelo
        } catch (e: Exception) {
            Log.e("CameraActivity", "Erro ao inicializar o detector: ${e.message}")
            Toast.makeText(this, "Falha ao inicializar o detector", Toast.LENGTH_LONG).show()
            return
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_search

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(
                        Intent(
                            applicationContext,
                            HomeActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_search -> return@setOnItemSelectedListener true
                R.id.bottom_settings -> {
                    startActivity(
                        Intent(
                            applicationContext,
                            RankingActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_profile -> {
                    startActivity(
                        Intent(
                            applicationContext,
                            SettingsActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
        startCamera() //inicializa a camera ao mudar de tela, e ja comeca a fazer predicoes em segundo plano
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        // Inicializar o helper com a função para atualizar latitude e longitude
        cameraButton.setOnClickListener {
            //dispara o envio dos dados para o firebase
            botaoTirarFotoQueDisparaOEnvioDoFormProFirebase()
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImage(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraActivity", "Erro ao iniciar a câmera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap() // Função de extensão para converter ImageProxy em Bitmap
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            imageProxy.imageInfo.rotationDegrees.toMatrix(), true
        )

        detector.detect(rotatedBitmap) // Passa o bitmap para o detector

        imageProxy.close() // Libera o frame após o processamento
    }

    // Função de extensão para converter rotationDegrees em Matrix
    private fun Int.toMatrix(): android.graphics.Matrix {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(this.toFloat())
        return matrix
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onEmptyDetect() {
        // Atualiza a interface caso nenhuma detecção seja feita
        Log.d("CameraActivity", "Nenhuma detecção realizada.")
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        // Log ou manipulação de detecções
        Log.d("CameraActivity", "Detecções: $boundingBoxes, Tempo: $inferenceTime ms")
        // Extraindo os valores de clsName para uma variável
        val classNames: List<String> = boundingBoxes.map { it.clsName }

        // Exemplo de uso: se precisar apenas do primeiro clsName
        val firstClassName: String? = classNames.firstOrNull()

        // Log dos valores extraídos
        Log.d("CameraActivity", "Class Names: $classNames")
        firstClassName?.let { Log.d("CameraActivity", "First Class Name: $it") }
        val listaDeClasses: List<String> = classNames
        passaNumeroDeDeteccoesDeClasses(listaDeClasses)
    }
    private fun passaNumeroDeDeteccoesDeClasses(listaDeClasses: List<String>) {
        Log.d("CameraActivity", "Processando formulário com classes: $listaDeClasses")
        setTop10predicoesDeClasse(listaDeClasses)
    }

    private fun setTop10predicoesDeClasse(top10predicoesDeClasse: List<String>) {
        top10predicoesDeClasseCompleta = top10predicoesDeClasse
    }

    private fun getTop10predicoesDeClasses(): List<String> {
        return top10predicoesDeClasseCompleta.take(10) //ver se pega a numero 10 ou a 1 ou as 10 priemrias que era oq eu queria
    }

    private fun botaoTirarFotoQueDisparaOEnvioDoFormProFirebase() {
        Log.d("CameraActivity", "Botão pressionado para processar detecções")
        val listaDeClassesPredistas: List<String> = getTop10predicoesDeClasses()
        Log.d("Localizacao", localizacaoAtual)
        Log.d("Classe da predicao", listaDeClassesPredistas.toString())

        Toast.makeText(this, "Enviando local! ",
            Toast.LENGTH_LONG).show()
        val geopoint: GeoPoint = (GeoPoint(lati.toDouble(), longi.toDouble()))

        val firestoreRepository = FirestoreRepository()
        firestoreRepository.salvarPontoDeInteresse(
            geopoint,
            listaDeClassesPredistas
        )
        val trophyGenerator = TrophyGenerator(firestoreRepository) // Passe o repositório no construtor
        trophyGenerator.gerenciarTrofeus("NomeDoUsuario", listaDeClassesPredistas) { sucesso ->
            if (sucesso) {
                Log.d("TrophyGenerator", "Troféus gerenciados com sucesso!")
            } else {
                Log.e("TrophyGenerator", "Falha ao gerenciar os troféus.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ServiceCast")
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.isLocationEnabled
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).setMinUpdateIntervalMillis(5000).build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Toast.makeText(this, "acesso a localizacao permitido",
                Toast.LENGTH_LONG).show()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException){
                try {
                    e.startResolutionForResult(
                        this,
                        100
                    )
                } catch (sendEx: java.lang.Exception) {
                    sendEx.printStackTrace()
                }
                }
            }
    }
}
