package com.bhushan.datasync.presentation.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bhushan.datasync.databinding.ActivityRegisterBinding
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.textOrEmpty
import com.bhushan.datasync.utils.toast
import com.bhushan.datasync.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
//    private val viewModel: RegisterViewModel by androidx.activity.viewModels()

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            clearErrors()
            viewModel.register(
                name = binding.etName.textOrEmpty(),
                email = binding.etEmail.textOrEmpty(),
                password = binding.etPassword.textOrEmpty(),
                confirmPassword = binding.etConfirmPassword.textOrEmpty()
            )
        }
        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tvError.gone()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collect { form ->
                        binding.tilName.error = form.nameError
                        binding.tilEmail.error = form.emailError
                        binding.tilPassword.error = form.passwordError
                        binding.tilConfirmPassword.error = form.confirmPasswordError
                    }
                }
                launch {
                    viewModel.registerState.collect { resource ->
                        when (resource) {
                            null -> Unit
                            is Resource.Loading -> {
                                binding.progressBar.visible()
                                binding.btnRegister.isEnabled = false
                                binding.tvError.gone()
                            }
                            is Resource.Success -> {
                                binding.progressBar.gone()
                                toast("Account created successfully. Please sign in.")
                                finish()
                            }
                            is Resource.Error -> {
                                binding.progressBar.gone()
                                binding.btnRegister.isEnabled = true
                                binding.tvError.visible()
                                binding.tvError.text = resource.message
                            }
                        }
                    }
                }
            }
        }
    }
}