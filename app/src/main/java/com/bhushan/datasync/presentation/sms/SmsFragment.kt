package com.bhushan.datasync.presentation.sms

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhushan.datasync.databinding.FragmentSmsBinding
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.collectFlow
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.setVisibleIf
import com.bhushan.datasync.utils.visible
import dagger.hilt.android.AndroidEntryPoint

/** Requirement #17/#18/#24: RecyclerView of SMS (sender, preview, date/time) with search. */
@AndroidEntryPoint
class SmsFragment : Fragment() {

    private var _binding: FragmentSmsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SmsViewModel by viewModels()
    private val adapter = SmsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        collectFlow(viewModel.uiState) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.recyclerView.gone()
                    binding.emptyState.root.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.emptyState.root.setVisibleIf(resource.data.isEmpty())
                    binding.recyclerView.setVisibleIf(resource.data.isNotEmpty())
                    adapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.recyclerView.gone()
                    binding.emptyState.root.visible()
                    binding.emptyState.tvEmptyMessage.text = resource.message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
