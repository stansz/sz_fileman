package com.sz.fileman.core.di

import com.sz.fileman.core.security.storage.SecureStorage
import com.sz.fileman.data.local.LocalFileRepository
import com.sz.fileman.data.nas.NasFileRepository
import com.sz.fileman.domain.repository.FileRepository
import com.sz.fileman.domain.repository.NasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provide the local file repository implementation.
     */
    @Provides
    @Singleton
    fun provideFileRepository(): FileRepository {
        return LocalFileRepository()
    }
    
    /**
     * Provide the NAS repository implementation.
     */
    @Provides
    @Singleton
    fun provideNasRepository(
        secureStorage: SecureStorage
    ): NasRepository {
        return NasFileRepository(secureStorage = secureStorage)
    }
}
