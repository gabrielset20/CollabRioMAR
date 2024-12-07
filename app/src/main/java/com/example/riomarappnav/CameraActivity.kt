package com.example.riomarappnav

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        val captureButton = findViewById<ImageButton>(R.id.capture_button)

        // Diretório onde as fotos serão salvas
        outputDirectory = getOutputDirectory()

        // Solicitar permissão da câmera
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
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

        // Configurar o botão de captura
        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Configurar a visualização da câmera
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                // Configuração do ImageCapture
                imageCapture = ImageCapture.Builder().build()

                // Selecionar a câmera traseira
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Ver os usos da câmera
                cameraProvider.unbindAll()  // Liberar quaisquer casos de uso anteriores
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Falha ao iniciar a câmera", exc)
                Toast.makeText(this, "Erro ao iniciar a câmera: ${exc.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        // Configuração da visualização da câmera
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Configuração do ImageCapture para tirar foto
        imageCapture = ImageCapture.Builder().build()

        // Usar a câmera traseira por padrão
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Verficar uso da camera
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview, imageCapture
            )
        } catch (exc: Exception) {
            Toast.makeText(this, "Erro ao iniciar a câmera.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        // Nome do arquivo de imagem
        val photoFile = File(outputDirectory, SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Foto salva com sucesso
                    val msg = "Foto salva: ${photoFile.absolutePath}"
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    // Erro ao tirar foto
                    val msg = "Erro ao salvar a foto."
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()  // Se a permissão for concedida, iniciar a câmera
            } else {
                Toast.makeText(this, "Permissão para usar a câmera é necessária.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}