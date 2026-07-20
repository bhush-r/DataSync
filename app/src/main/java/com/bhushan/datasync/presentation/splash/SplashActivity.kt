package com.bhushan.datasync.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.presentation.auth.LoginActivity
import com.bhushan.datasync.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Requirement #4: Splash Screen.
 *
 * Also doubles as the session-restore gate (#9 session management): if
 * Firebase Auth already has a signed-in user we skip Login entirely and go
 * straight to the Dashboard, otherwise we route to [LoginActivity].
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    companion object {
        private const val SPLASH_DELAY_MS = 1200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.bhushan.datasync.R.layout.activity_splash)

        lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)
            val destination = if (authRepository.isUserAuthenticated()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, destination))
            finish()
        }
    }
}
