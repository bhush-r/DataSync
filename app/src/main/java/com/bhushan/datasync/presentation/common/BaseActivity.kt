package com.bhushan.datasync.presentation.common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.presentation.auth.LoginActivity
import javax.inject.Inject

/**
 * Requirement #8/#30: "Restrict all screens to authenticated users only".
 *
 * Every Activity in the app except [com.bhushan.datasync.presentation.splash.SplashActivity]
 * and [LoginActivity] itself extends this class. `onStart` re-validates the
 * Firebase session on every resume (not just at launch) so a token that
 * expires or a manual sign-out from another device immediately kicks the
 * user back to the login screen instead of leaving a stale screen reachable.
 */
abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onStart() {
        super.onStart()
        if (!authRepository.isUserAuthenticated()) {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
