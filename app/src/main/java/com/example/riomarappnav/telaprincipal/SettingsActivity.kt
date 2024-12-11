package com.example.riomarappnav.telaprincipal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.riomarappnav.R
import com.example.riomarappnav.ThemePreferenceManager
import com.example.riomarappnav.database.FirestoreRepository
import com.example.riomarappnav.telaprincipal.EditInfo.EditInfoActivity
import com.example.riomarappnav.telaprincipal.Help.HelpActivity
import com.example.riomarappnav.telaprincipal.camerapred.CameraActivity
import com.example.riomarappnav.telaprincipal.telaRanking.RankingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class SettingsActivity : AppCompatActivity() {

    private lateinit var etWelcomeName: EditText

    private lateinit var themeManager: ThemePreferenceManager
    private lateinit var firestoreRepository: FirestoreRepository

    private fun recuperarNomeLocal(): String? {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("usuario_nome", null)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        themeManager = ThemePreferenceManager(this)

        val darkModeSwitch = findViewById<Switch>(R.id.switchDarkMode)

        etWelcomeName = findViewById(R.id.etWelcomeName)
        firestoreRepository = FirestoreRepository()

        val etWelcomeName = findViewById<EditText>(R.id.etWelcomeName)
        val btnConfirmName = findViewById<Button>(R.id.btnConfirmName)


        etWelcomeName.setText(recuperarNomeLocal())

        btnConfirmName.setOnClickListener {
            val nome = etWelcomeName.text.toString().trim()
            if (nome.isNotEmpty()) {
                salvarNomeLocalmente(nome)
                Toast.makeText(this, "Nome confirmado: $nome", Toast.LENGTH_SHORT).show()
                firestoreRepository.atualizarNomeUsuario(nome) { sucesso ->
                    if (sucesso) {
                        Toast.makeText(this, "Nome salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao salvar nome.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, insira um nome válido.", Toast.LENGTH_SHORT).show()
            }
        }

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

        lifecycleScope.launch {
            val isDarkMode = themeManager.isDarkModeEnabled.first() ?: false
            darkModeSwitch.isChecked = isDarkMode
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                themeManager.setDarkMode(isChecked)
                recreate() // Reinicia a atividade para aplicar o tema
            }
        }


        // Clicar demais botões

        findViewById<View>(R.id.llEditInfo).setOnClickListener {
            val intent = Intent(this, EditInfoActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.llPermissoes).setOnClickListener {
        }

        findViewById<View>(R.id.llSobre).setOnClickListener {
        }

        findViewById<View>(R.id.llAjuda).setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

    }
    private fun salvarNomeLocalmente(nome: String) {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("usuario_nome", nome)
        editor.apply()
    }

}