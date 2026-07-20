package com.bhushan.datasync.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.RecordItem
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.RecordRepository
import com.bhushan.datasync.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecordEvent {
    data class Saved(val message: String) : RecordEvent()
    data class Deleted(val message: String) : RecordEvent()
    data class Failure(val message: String) : RecordEvent()
}

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _recordsResource = MutableStateFlow<Resource<List<RecordItem>>>(Resource.Loading)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = MutableSharedFlow<RecordEvent>()
    val events: SharedFlow<RecordEvent> = _events

    val uiState: StateFlow<Resource<List<RecordItem>>> =
        combine(_recordsResource, _searchQuery) { resource, query ->
            if (resource is Resource.Success) {
                val filtered = if (query.isBlank()) {
                    resource.data
                } else {
                    resource.data.filter {
                        it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                    }
                }
                Resource.Success(filtered)
            } else resource
        }.stateIn(viewModelScope, SharingStarted.Eagerly, Resource.Loading)

    init {
        observeRecords()
    }

    private fun observeRecords() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            recordRepository.observeRecords(uid).collect { _recordsResource.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Requirement #16: Add Record. */
    fun addRecord(title: String, description: String) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = recordRepository.addRecord(uid, title, description)) {
                is Resource.Success -> _events.emit(RecordEvent.Saved("Record saved"))
                is Resource.Error -> _events.emit(RecordEvent.Failure(result.message))
                else -> Unit
            }
        }
    }

    /** Requirement #16: Update Record. */
    fun updateRecord(record: RecordItem) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = recordRepository.updateRecord(uid, record)) {
                is Resource.Success -> _events.emit(RecordEvent.Saved("Record updated"))
                is Resource.Error -> _events.emit(RecordEvent.Failure(result.message))
                else -> Unit
            }
        }
    }

    /** Requirement #16: Delete Record (confirmation dialog is handled by the Fragment). */
    fun deleteRecord(recordId: String) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = recordRepository.deleteRecord(uid, recordId)) {
                is Resource.Success -> _events.emit(RecordEvent.Deleted("Record deleted"))
                is Resource.Error -> _events.emit(RecordEvent.Failure(result.message))
                else -> Unit
            }
        }
    }
}
