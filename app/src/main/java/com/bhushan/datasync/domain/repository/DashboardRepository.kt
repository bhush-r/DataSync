package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.DashboardStats
import com.bhushan.datasync.utils.Resource

interface DashboardRepository {
    /** Aggregates counts from all three synced collections plus the user profile. */
    suspend fun getDashboardStats(uid: String): Resource<DashboardStats>
}
