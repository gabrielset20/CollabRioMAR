package com.example.riomarappnav.telaprincipal.EditInfo

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.riomarappnav.databinding.ActivityEditInfoBinding

class EditInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditInfoBinding

    // Registrando o ActivityResultLauncher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Define a imagem selecionada no ImageView
            binding.ivEditProfilePicture.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma imagem foi selecionada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seleção de nova imagem
        binding.btnChangePicture.setOnClickListener {
            pickImageLauncher.launch("image/*") // Permite selecionar imagens
        }

        // Salvar nome
        binding.btnSave.setOnClickListener {
            val newName = binding.etEditName.text.toString()
            if (newName.isNotBlank()) {
                saveUserInfo(newName)
            } else {
                Toast.makeText(this, "O nome não pode estar vazio!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Salvar no BD
    private fun saveUserInfo(name: String) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPref.edit().putString("userName", name).apply()
        Toast.makeText(this, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
