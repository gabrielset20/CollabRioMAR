package com.example.riomarappnav

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView


class SettingsActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var ivProfilePicture: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tvWelcome = findViewById(R.id.tvWelcome)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)

        // Simulação de Info de usuário
        val userName = "Luis"
        val profileImageUrl = "https://pixabay.com/photos/cat-domestic-cat-animal-8611246/"

        // Atualiza a UI
        tvWelcome.text = "Olá, $userName!"
        Glide.with(this).load(profileImageUrl).into(ivProfilePicture)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_profile
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

                R.id.bottom_search -> {
                    startActivity(
                        Intent(
                            applicationContext,
                            CameraActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@setOnItemSelectedListener true
                }

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

                R.id.bottom_profile -> return@setOnItemSelectedListener true
            }
            false
        }

        findViewById<EditText>(R.id.etSearchSettings).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Não precisa de alterações aqui
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Não precisa de alterações aqui
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                // Exibir ou ocultar itens baseados na consulta
                findViewById<View>(R.id.llEditInfo).visibility =
                    if ("editar" in query) View.VISIBLE else View.GONE
                findViewById<View>(R.id.llTema).visibility =
                    if ("tema" in query) View.VISIBLE else View.GONE
                findViewById<View>(R.id.llPermissoes).visibility =
                    if ("permissões" in query) View.VISIBLE else View.GONE
                findViewById<View>(R.id.llAjuda).visibility =
                    if ("ajuda" in query) View.VISIBLE else View.GONE
                findViewById<View>(R.id.llSobre).visibility =
                    if ("sobre" in query) View.VISIBLE else View.GONE
            }
        })

        // Implementações para voltar as demais opções de configuração quando o buscar perder o foco:
        findViewById<EditText>(R.id.etSearchSettings).setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // Quando o campo de busca perde o foco, restaura todas as opções
                findViewById<View>(R.id.llEditInfo).visibility = View.VISIBLE
                findViewById<View>(R.id.llTema).visibility = View.VISIBLE
                findViewById<View>(R.id.llPermissoes).visibility = View.VISIBLE
                findViewById<View>(R.id.llAjuda).visibility = View.VISIBLE
                findViewById<View>(R.id.llSobre).visibility = View.VISIBLE
            }
        }

        findViewById<EditText>(R.id.etSearchSettings).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && findViewById<EditText>(R.id.etSearchSettings).text.isEmpty()) {
                findViewById<View>(R.id.llEditInfo).visibility = View.VISIBLE
                findViewById<View>(R.id.llTema).visibility = View.VISIBLE
                findViewById<View>(R.id.llPermissoes).visibility = View.VISIBLE
                findViewById<View>(R.id.llAjuda).visibility = View.VISIBLE
                findViewById<View>(R.id.llSobre).visibility = View.VISIBLE
            }
        }


        // Clicar demais botões
        findViewById<View>(R.id.llTema).setOnClickListener {
        }

        findViewById<View>(R.id.llEditInfo).setOnClickListener {
        }

        findViewById<View>(R.id.llPermissoes).setOnClickListener {
        }

        findViewById<View>(R.id.llSobre).setOnClickListener {
        }

        findViewById<View>(R.id.llAjuda).setOnClickListener {
        }

    }

}