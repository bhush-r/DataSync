package com.bhushan.datasync.di

import com.bhushan.datasync.data.repository.AuthRepositoryImpl
import com.bhushan.datasync.data.repository.CallLogRepositoryImpl
import com.bhushan.datasync.data.repository.ContactRepositoryImpl
import com.bhushan.datasync.data.repository.DashboardRepositoryImpl
import com.bhushan.datasync.data.repository.RecordRepositoryImpl
import com.bhushan.datasync.data.repository.SmsRepositoryImpl
import com.bhushan.datasync.data.repository.UserRepositoryImpl
import com.bhushan.datasync.domain.repository.AuthRepository
import com.bhushan.datasync.domain.repository.CallLogRepository
import com.bhushan.datasync.domain.repository.ContactRepository
import com.bhushan.datasync.domain.repository.DashboardRepository
import com.bhushan.datasync.domain.repository.RecordRepository
import com.bhushan.datasync.domain.repository.SmsRepository
import com.bhushan.datasync.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds every domain [Repository] interface to its concrete Firebase-backed
 * implementation. This is the seam that makes Clean Architecture real here:
 * ViewModels only ever see the interfaces in `domain.repository`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindRecordRepository(impl: RecordRepositoryImpl): RecordRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindCallLogRepository(impl: CallLogRepositoryImpl): CallLogRepository

    @Binds
    @Singleton
    abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
