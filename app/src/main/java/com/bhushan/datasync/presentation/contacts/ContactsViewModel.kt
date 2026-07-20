package com.bhushan.datasync.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.ContactItem
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.ContactRepository
import com.bhushan.datasync.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _contactsResource = MutableStateFlow<Resource<List<ContactItem>>>(Resource.Loading)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<Resource<List<ContactItem>>> =
        combine(_contactsResource, _searchQuery) { resource, query ->
            if (resource is Resource.Success) {
                val filtered = if (query.isBlank()) {
                    resource.data
                } else {
                    resource.data.filter {
                        it.name.contains(query, ignoreCase = true) ||
                            it.phoneNumber.contains(query, ignoreCase = true)
                    }
                }
                Resource.Success(filtered)
            } else resource
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, Resource.Loading)

    init {
        observeContacts()
    }

    private fun observeContacts() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            contactRepository.observeSyncedContacts(uid).collect { _contactsResource.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
