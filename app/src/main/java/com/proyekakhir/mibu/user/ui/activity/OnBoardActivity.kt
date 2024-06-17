package com.proyekakhir.mibu.user.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.proyekakhir.mibu.databinding.ActivityOnBoardBinding
import com.proyekakhir.mibu.user.api.UserPreference
import com.proyekakhir.mibu.user.api.dataStore
import com.proyekakhir.mibu.user.auth.LoginActivity
import com.proyekakhir.mibu.user.auth.RegisterActivity
import kotlinx.coroutines.launch

class OnBoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoardBinding
    private lateinit var userPreference: UserPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.button2.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        userPreference = UserPreference.getInstance(dataStore)

    }

    private fun checkLoginStatus() {
        lifecycleScope.launch {
            userPreference.getSession().collect { session ->
                if (session.token.isNotEmpty()) {
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // check if user has login previously
        checkLoginStatus()
    }

}