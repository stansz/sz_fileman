package com.sz.fileman.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing security-related dependencies.
 * Note: SecureStorage and BiometricAuthManager use constructor injection with @Inject annotation,
 * so they don't need explicit provider methods here.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule

