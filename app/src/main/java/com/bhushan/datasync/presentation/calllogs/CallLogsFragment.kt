package com.bhushan.datasync.presentation.calllogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhushan.datasync.databinding.FragmentCallLogsBinding
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.collectFlow
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.setVisibleIf
import com.bhushan.datasync.utils.visible
import dagger.hilt.android.AndroidEntryPoint

/**
 * Requirement #17/#18/#22: RecyclerView of call logs (name, number, type,
 * duration, date/time) with search and a Chip-based type filter.
 */
@AndroidEntryPoint
class CallLogsFragment : Fragment() {

    private var _binding: FragmentCallLogsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CallLogsViewModel by viewModels()
    private val adapter = CallLogsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallLogsBinding.inflate(inflater, container, false)
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

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                binding.chipIncoming.id -> Constants.FILTER_INCOMING
                binding.chipOutgoing.id -> Constants.FILTER_OUTGOING
                binding.chipMissed.id -> Constants.FILTER_MISSED
                else -> Constants.FILTER_ALL
            }
            viewModel.onFilterChanged(filter)
        }

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
