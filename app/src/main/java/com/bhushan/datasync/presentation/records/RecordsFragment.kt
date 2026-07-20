package com.bhushan.datasync.presentation.records

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhushan.datasync.R
import com.bhushan.datasync.databinding.FragmentRecordsBinding
import com.bhushan.datasync.domain.model.RecordItem
import com.bhushan.datasync.utils.Resource
import com.bhushan.datasync.utils.collectFlow
import com.bhushan.datasync.utils.gone
import com.bhushan.datasync.utils.setVisibleIf
import com.bhushan.datasync.utils.toast
import com.bhushan.datasync.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/** Requirement #16/#17/#18: full CRUD Records screen with RecyclerView, search, and FAB. */
@AndroidEntryPoint
class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecordsViewModel by viewModels()

    private val adapter = RecordsAdapter(
        onEditClick = { record -> openEditSheet(record) },
        onDeleteClick = { record -> confirmDelete(record) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            AddEditRecordBottomSheet.newInstance().show(childFragmentManager, "add_record")
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        observeState()
        observeEvents()
    }

    private fun openEditSheet(record: RecordItem) {
        AddEditRecordBottomSheet.newInstance(record).show(childFragmentManager, "edit_record")
    }

    private fun confirmDelete(record: RecordItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_record_title)
            .setMessage(R.string.delete_record_message)
            .setPositiveButton(R.string.btn_delete) { _, _ -> viewModel.deleteRecord(record.id) }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun observeState() {
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

    private fun observeEvents() {
        collectFlow(viewModel.events) { event ->
            when (event) {
                is RecordEvent.Saved -> toast(event.message)
                is RecordEvent.Deleted -> toast(event.message)
                is RecordEvent.Failure -> toast(event.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
