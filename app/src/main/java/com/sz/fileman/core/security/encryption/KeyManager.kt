package com.sz.fileman.core.security.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages encryption keys using Android Keystore.
 * Provides secure key generation and retrieval.
 */
object KeyManager {
    
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "fileman_master_key"
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }
    
    /**
     * Get or create the master encryption key from Android Keystore.
     */
    fun getOrCreateMasterKey(): SecretKey {
        return try {
            // Try to get existing key
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: run {
                Timber.d("Key not found, creating new key")
                // Key doesn't exist, create it
                createKey()
            }
        } catch (e: Exception) {
            Timber.d("Key not found, creating new key")
            // Key doesn't exist, create it
            createKey()
        }
    }
    
    /**
     * Create a new encryption key in Android Keystore.
     */
    private fun createKey(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenSpec)
            val key = keyGenerator.generateKey()
            
            Timber.d("Successfully created encryption key in Keystore")
            key
        } catch (e: Exception) {
            Timber.e(e, "Failed to create encryption key")
            throw SecurityException("Failed to create encryption key", e)
        }
    }
    
    /**
     * Delete the master key from Keystore.
     * Warning: This will make all encrypted data unrecoverable.
     */
    fun deleteKey() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
            Timber.d("Successfully deleted encryption key from Keystore")
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete encryption key")
            throw SecurityException("Failed to delete encryption key", e)
        }
    }
    
    /**
     * Check if the master key exists in Keystore.
     */
    fun keyExists(): Boolean {
        return keyStore.containsAlias(KEY_ALIAS)
    }
}
