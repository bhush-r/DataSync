package com.bhushan.datasync.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bhushan.datasync.R
import com.bhushan.datasync.databinding.FragmentDashboardBinding
import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.domain.model.SyncState
import com.bhushan.datasync.permission.PermissionManager
import com.bhushan.datasync.utils.DateUtils
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.collectFlow
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.setVisibleIf
import com.bhushan.datasync.utils.toast
import com.bhushan.datasync.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Requirement #15: Dashboard/Home Screen with all six required stat tiles.
 * Also owns the runtime-permission request flow (requirement #19) since it
 * is the very first screen the user lands on after login.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var permissionManager: PermissionManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            toast(getString(R.string.sync_now))
            viewModel.syncNow()
        } else {
            showPermissionDeniedNotice()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadStats()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnSyncNow.setOnClickListener { handleSyncClick() }

        observeStats()
        observeSyncState()

        if (!permissionManager.hasAllPermissions()) {
            requestPermissions()
        }
    }

    private fun handleSyncClick() {
        if (permissionManager.hasAllPermissions()) {
            viewModel.syncNow()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(PermissionManager.REQUIRED_PERMISSIONS)
    }

    private fun showPermissionDeniedNotice() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_required_title)
            .setMessage(R.string.permission_denied_message)
            .setPositiveButton(R.string.btn_open_settings) { _, _ -> openAppSettings() }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", requireContext().packageName, null)
        )
        startActivity(intent)
    }

    private fun observeStats() {
        collectFlow(viewModel.stats) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.contentGroup.gone()
                    binding.errorGroup.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.errorGroup.gone()
                    binding.contentGroup.visible()
                    bindStats(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.contentGroup.gone()
                    binding.errorGroup.visible()
                    binding.tvErrorMessage.text = resource.message
                    binding.btnRetry.setOnClickListener { viewModel.loadStats() }
                }
            }
        }
    }

    private fun bindStats(stats: com.bhushan.datasync.domain.model.DashboardStats) {
        binding.tvContactsCount.text = stats.totalContacts.toString()
        binding.tvCallLogsCount.text = stats.totalCallLogs.toString()
        binding.tvSmsCount.text = stats.totalSms.toString()
        binding.tvLastSync.text = DateUtils.relativeTime(stats.lastSyncAt)
        binding.tvRole.text = stats.role.name

        val isAdmin = stats.role == Role.ADMIN
        binding.devModeCard.setVisibleIf(isAdmin)
        if (isAdmin) {
            binding.tvDevModeStatus.text = if (stats.devModeEnabled) {
                getString(R.string.dev_mode_on)
            } else {
                getString(R.string.dev_mode_off)
            }
            binding.tvDevModeStatus.setTextColor(
                resources.getColor(
                    if (stats.devModeEnabled) R.color.dev_mode_on else R.color.dev_mode_off,
                    null
                )
            )
        }
    }

    private fun observeSyncState() {
        collectFlow(viewModel.syncState) { state ->
            when (state) {
                is SyncState.Idle -> {
                    binding.syncProgressBar.gone()
                    binding.btnSyncNow.isEnabled = true
                    binding.tvSyncStatus.gone()
                }
                is SyncState.Syncing -> {
                    binding.btnSyncNow.isEnabled = false
                    binding.syncProgressBar.visible()
                    binding.syncProgressBar.progress = state.progressPercent
                    binding.tvSyncStatus.visible()
                    binding.tvSyncStatus.text = state.label
                }
                is SyncState.Success -> {
                    binding.btnSyncNow.isEnabled = true
                    binding.syncProgressBar.gone()
                    binding.tvSyncStatus.visible()
                    binding.tvSyncStatus.text = getString(R.string.sync_success)
                }
                is SyncState.Error -> {
                    binding.btnSyncNow.isEnabled = true
                    binding.syncProgressBar.gone()
                    binding.tvSyncStatus.visible()
                    binding.tvSyncStatus.text = state.message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
