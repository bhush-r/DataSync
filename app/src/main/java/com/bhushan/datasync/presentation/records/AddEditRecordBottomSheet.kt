package com.bhushan.datasync.presentation.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bhushan.datasync.R
import com.bhushan.datasync.databinding.FragmentAddEditRecordBinding
import com.bhushan.datasync.domain.model.RecordItem
import com.bhushan.datasync.utils.textOrEmpty
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Shared Add/Edit UI for the Records CRUD feature. Passing an existing
 * [RecordItem] via [ARG_RECORD] switches the sheet into "edit" mode;
 * omitting it creates a brand-new record on save.
 */
class AddEditRecordBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentAddEditRecordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecordsViewModel by viewModels({ requireParentFragment() })

    private var existingRecord: RecordItem? = null

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_CREATED_AT = "arg_created_at"

        fun newInstance(record: RecordItem? = null): AddEditRecordBottomSheet {
            val fragment = AddEditRecordBottomSheet()
            if (record != null) {
                fragment.arguments = Bundle().apply {
                    putString(ARG_ID, record.id)
                    putString(ARG_TITLE, record.title)
                    putString(ARG_DESCRIPTION, record.description)
                    putLong(ARG_CREATED_AT, record.createdAt)
                }
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_ID)?.let { id ->
            existingRecord = RecordItem(
                id = id,
                title = arguments?.getString(ARG_TITLE).orEmpty(),
                description = arguments?.getString(ARG_DESCRIPTION).orEmpty(),
                createdAt = arguments?.getLong(ARG_CREATED_AT) ?: 0L
            )
        }

        binding.tvSheetTitle.text = if (existingRecord != null) {
            getString(R.string.edit_record)
        } else {
            getString(R.string.add_record)
        }
        binding.etTitle.setText(existingRecord?.title)
        binding.etDescription.setText(existingRecord?.description)

        binding.btnSave.setOnClickListener { handleSave() }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun handleSave() {
        val title = binding.etTitle.textOrEmpty()
        val description = binding.etDescription.textOrEmpty()

        if (title.isBlank()) {
            binding.tilTitle.error = getString(R.string.error_title_required)
            return
        }
        binding.tilTitle.error = null

        val current = existingRecord
        if (current != null) {
            viewModel.updateRecord(current.copy(title = title, description = description))
        } else {
            viewModel.addRecord(title, description)
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
