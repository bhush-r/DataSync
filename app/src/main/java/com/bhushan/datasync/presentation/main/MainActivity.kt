package com.bhushan.datasync.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bhushan.datasync.R
import com.bhushan.datasync.databinding.ActivityMainBinding
import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.presentation.common.BaseActivity
import com.bhushan.datasync.service.DataSyncObserverService
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fix status bar overlap so title & 3-dot menu are never cut off
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        setSupportActionBar(binding.toolbar)
        setupNavigation()
        observeProfile()
        startDataSyncObserverService()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun startDataSyncObserverService() {
        try {
            val serviceIntent = Intent(this, DataSyncObserverService::class.java)
            startService(serviceIntent)
        } catch (_: Exception) {}
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { resource ->
                    if (resource is Resource.Success) {
                        isAdmin = resource.data.role == Role.ADMIN
                        invalidateOptionsMenu() // Show/hide Admin Panel menu item dynamically
                    } else if (resource is Resource.Error) {

                        if (authRepository.isUserAuthenticated()) {
                            toast(resource.message)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        // Show Admin Panel option ONLY if logged-in user is ADMIN
        menu.findItem(R.id.action_admin_panel)?.isVisible = isAdmin
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_admin_panel -> {
                navigateToAdminPanel()
                true
            }
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToAdminPanel() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(R.id.adminPanelFragment)
    }

    private fun confirmLogout() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_message)
            .setPositiveButton(R.string.action_logout) { _, _ -> performLogout() }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun performLogout() {
        viewModel.logout()
        val intent = Intent(this, com.bhushan.datasync.presentation.auth.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}