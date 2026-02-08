package com.sz.fileman.core.security.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage implementation using EncryptedSharedPreferences.
 * Provides encrypted storage for sensitive data like NAS credentials.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create encrypted shared preferences")
            throw e
        }
    }
    
    /**
     * Save a string value securely.
     */
    fun putString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
        Timber.d("Saved secure value for key: $key")
    }
    
    /**
     * Retrieve a string value securely.
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }
    
    /**
     * Save an integer value securely.
     */
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
        Timber.d("Saved secure int value for key: $key")
    }
    
    /**
     * Retrieve an integer value securely.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    /**
     * Save a boolean value securely.
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        Timber.d("Saved secure boolean value for key: $key")
    }
    
    /**
     * Retrieve a boolean value securely.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Remove a value from secure storage.
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
        Timber.d("Removed secure value for key: $key")
    }
    
    /**
     * Clear all values from secure storage.
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
        Timber.d("Cleared all secure storage values")
    }
    
    /**
     * Check if a key exists in secure storage.
     */
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
    
    companion object {
        private const val PREFS_NAME = "secure_prefs"
    }
}
