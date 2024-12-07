package com.example.riomarappnav.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.riomarappnav.databinding.ActivityLoginMainBinding
import com.google.firebase.auth.FirebaseAuth

class LoginMain : AppCompatActivity() {

        private lateinit var binding: ActivityLoginMainBinding

        companion object{
            lateinit var auth: FirebaseAuth
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityLoginMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()

            if(auth.currentUser == null){
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }

            binding = ActivityLoginMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.signIn.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
            binding.signOut.setOnClickListener {
                auth.signOut()
                binding.userDetails.text = updateData()
            }
        }

        override fun onResume() {
            super.onResume()
            binding.userDetails.text = updateData()
        }

        private fun updateData(): String{
            return "Email : ${auth.currentUser?.email}"
        }

    }