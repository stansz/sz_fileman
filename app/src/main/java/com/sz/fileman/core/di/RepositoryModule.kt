package com.sz.fileman.core.di

import com.sz.fileman.data.local.LocalFileRepository
import com.sz.fileman.domain.repository.FileRepository
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
    
    // TODO: Provide NasRepository when implemented
    // @Provides
    // @Singleton
    // fun provideNasRepository(
    //     secureStorage: SecureStorage,
    //     cryptoUtils: CryptoUtils
    // ): NasRepository {
    //     return NasFileRepository(secureStorage, cryptoUtils)
    // }
}
