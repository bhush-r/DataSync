package com.bhushan.datasync.presentation.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.SmsItem
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.SmsRepository
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
class SmsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _smsResource = MutableStateFlow<Resource<List<SmsItem>>>(Resource.Loading)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<Resource<List<SmsItem>>> =
        combine(_smsResource, _searchQuery) { resource, query ->
            if (resource is Resource.Success) {
                val filtered = if (query.isBlank()) {
                    resource.data
                } else {
                    resource.data.filter {
                        it.sender.contains(query, ignoreCase = true) ||
                            it.body.contains(query, ignoreCase = true)
                    }
                }
                Resource.Success(filtered)
            } else resource
        }.stateIn(viewModelScope, SharingStarted.Eagerly, Resource.Loading)

    init {
        observeSms()
    }

    private fun observeSms() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            smsRepository.observeSyncedSms(uid).collect { _smsResource.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
