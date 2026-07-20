package com.bhushan.datasync.presentation.calllogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.CallLogItem
import com.bhushan.datasync.domain.model.CallType
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.CallLogRepository
import com.bhushan.datasync.utils.Constants
import com.bhushan.datasync.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _callLogsResource = MutableStateFlow<Resource<List<CallLogItem>>>(Resource.Loading)
    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(Constants.FILTER_ALL)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val filter: StateFlow<String> = _filter.asStateFlow()

    val uiState: StateFlow<Resource<List<CallLogItem>>> =
        combine(_callLogsResource, _searchQuery, _filter) { resource, query, filter ->
            if (resource is Resource.Success) {
                var filtered = resource.data
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.name.contains(query, ignoreCase = true) ||
                            it.phoneNumber.contains(query, ignoreCase = true)
                    }
                }
                filtered = when (filter) {
                    Constants.FILTER_INCOMING -> filtered.filter { it.callType == CallType.INCOMING }
                    Constants.FILTER_OUTGOING -> filtered.filter { it.callType == CallType.OUTGOING }
                    Constants.FILTER_MISSED -> filtered.filter { it.callType == CallType.MISSED }
                    else -> filtered
                }
                Resource.Success(filtered)
            } else resource
        }.stateIn(viewModelScope, SharingStarted.Eagerly, Resource.Loading)

    init {
        observeCallLogs()
    }

    private fun observeCallLogs() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            callLogRepository.observeSyncedCallLogs(uid).collect { _callLogsResource.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: String) {
        _filter.value = filter
    }
}
