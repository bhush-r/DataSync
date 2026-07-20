package com.bhushan.datasync.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bhushan.datasync.databinding.FragmentAdminPanelBinding
import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.collectFlow
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.toast
import com.bhushan.datasync.utils.visible
import dagger.hilt.android.AndroidEntryPoint

/**
 * Requirement #12/#13/#31: Admin-only screen. Even though the toolbar entry
 * point that leads here is hidden for non-admins, this Fragment independently
 * re-validates the role once its own data loads and kicks non-admins out --
 * defense in depth against a deep link or back-stack restoration bypassing
 * the menu visibility check.
 */
@AndroidEntryPoint
class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private var isProgrammaticToggle = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchDevMode.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticToggle) viewModel.toggleDevMode(isChecked)
        }

        collectFlow(viewModel.userState) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.contentGroup.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    if (resource.data.role != Role.ADMIN) {
                        toast("Access denied. Admin role required.")
                        findNavController().popBackStack()
                        return@collectFlow
                    }
                    binding.contentGroup.visible()
                    isProgrammaticToggle = true
                    binding.switchDevMode.isChecked = resource.data.devModeEnabled
                    isProgrammaticToggle = false
                    binding.tvAdminEmail.text = resource.data.email
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    toast(resource.message)
                }
            }
        }

        collectFlow(viewModel.toastEvents) { toast(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
