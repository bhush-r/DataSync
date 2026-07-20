package com.bhushan.datasync.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.datasync.domain.model.DashboardStats
import com.bhushan.datasync.domain.model.SyncState
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.DashboardRepository
import com.bhushan.datasync.sync.SyncManager
import com.bhushan.datasync.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<DashboardStats>>(Resource.Loading)
    val stats: StateFlow<Resource<DashboardStats>> = _stats.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    init {
        loadStats()
    }

    fun loadStats() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _stats.value = Resource.Loading
            _stats.value = dashboardRepository.getDashboardStats(uid)
        }
    }

    /** Requirement #26: manual sync triggered from the Dashboard's Sync Now button. */
    fun syncNow() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val success = syncManager.syncAll(uid)
            if (success) loadStats()
        }
    }
}
