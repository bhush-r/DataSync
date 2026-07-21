package com.bhushan.datasync.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bhushan.datasync.databinding.ActivityLoginBinding
import com.bhushan.datasync.presentation.main.MainActivity
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.textOrEmpty
import com.bhushan.datasync.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            binding.tilEmail.error = null
            binding.tilPassword.error = null
            viewModel.login(
                binding.etEmail.textOrEmpty(),
                binding.etPassword.textOrEmpty()
            )
        }

        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collect { form ->
                        binding.tilEmail.error = form.emailError
                        binding.tilPassword.error = form.passwordError
                    }
                }
                launch {
                    viewModel.loginState.collect { resource ->
                        when (resource) {
                            null -> Unit
                            is Resource.Loading -> {
                                binding.progressBar.visible()
                                binding.btnLogin.isEnabled = false
                                binding.tvError.gone()
                            }
                            is Resource.Success -> {
                                binding.progressBar.gone()
                                navigateToMain()
                            }
                            is Resource.Error -> {
                                binding.progressBar.gone()
                                binding.btnLogin.isEnabled = true
                                binding.tvError.visible()
                                binding.tvError.text = resource.message
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}