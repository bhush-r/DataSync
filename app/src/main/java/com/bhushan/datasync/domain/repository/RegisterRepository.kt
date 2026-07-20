package com.bhushan.datasync.domain.repository

import com.bhushan.datasync.domain.model.User
import com.bhushan.datasync.utils.Resource
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    fun register(name: String, email: String, password: String): Flow<Resource<User>>
}