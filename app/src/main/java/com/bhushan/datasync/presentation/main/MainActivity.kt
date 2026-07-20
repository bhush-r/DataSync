package com.bhushan.datasync.presentation.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bhushan.datasync.R
import androidx.activity.viewModels

import com.bhushan.datasync.databinding.ActivityMainBinding
import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.presentation.common.BaseActivity
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Single-activity shell for every authenticated screen. Owns:
 *  - the bottom navigation graph (Dashboard / Records / Contacts / Calls / SMS),
 *  - the toolbar overflow menu (Admin Panel -- visible only to Admins -- and Logout),
 *  - role-based access control: the Admin Panel menu entry is added/removed
 *    dynamically as soon as the user's role is known (requirement #12/#13/#31).
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
//    private val viewModel: MainViewModel by androidx.activity.viewModels()
    private val viewModel: MainViewModel by viewModels()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupNavigation()
        observeProfile()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { resource ->
                    if (resource is Resource.Success) {
                        isAdmin = resource.data.role == Role.ADMIN
                        invalidateOptionsMenu()
                    } else if (resource is Resource.Error) {
                        toast(resource.message)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
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
        val intent = android.content.Intent(this, com.bhushan.datasync.presentation.auth.LoginActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
