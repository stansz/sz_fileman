package com.sz.fileman.core.security.encryption

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import timber.log.Timber

/**
 * Utility class for encryption and decryption operations.
 * Uses AES-256-GCM for secure encryption.
 */
object CryptoUtils {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    
    /**
     * Generate a new AES-256 secret key.
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE, SecureRandom())
        return keyGenerator.generateKey()
    }
    
    /**
     * Create a SecretKey from a byte array.
     */
    fun createKey(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, ALGORITHM)
    }
    
    /**
     * Encrypt data using AES-256-GCM.
     * Returns Base64 encoded encrypted data with IV prepended.
     */
    fun encrypt(data: ByteArray, key: SecretKey): String {
        try {
            // Generate random IV
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            
            // Initialize cipher for encryption
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
            
            // Encrypt data
            val encryptedData = cipher.doFinal(data)
            
            // Combine IV and encrypted data
            val combined = iv + encryptedData
            
            // Return Base64 encoded
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "Encryption failed")
            throw SecurityException("Failed to encrypt data", e)
        }
    }
    
    /**
     * Encrypt a string using AES-256-GCM.
     */
    fun encryptString(text: String, key: SecretKey): String {
        return encrypt(text.toByteArray(Charsets.UTF_8), key)
    }
    
    /**
     * Decrypt Base64 encoded data using AES-256-GCM.
     * Expects IV to be prepended to the encrypted data.
     */
    fun decrypt(encryptedData: String, key: SecretKey): ByteArray {
        try {
            // Decode Base64
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            
            // Extract IV and encrypted data
            val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
            val data = combined.sliceArray(GCM_IV_LENGTH until combined.size)
            
            // Initialize cipher for decryption
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
            
            // Decrypt data
            return cipher.doFinal(data)
        } catch (e: Exception) {
            Timber.e(e, "Decryption failed")
            throw SecurityException("Failed to decrypt data", e)
        }
    }
    
    /**
     * Decrypt a string using AES-256-GCM.
     */
    fun decryptString(encryptedData: String, key: SecretKey): String {
        return String(decrypt(encryptedData, key), Charsets.UTF_8)
    }
    
    /**
     * Generate a random salt for key derivation.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    /**
     * Encode bytes to Base64 string.
     */
    fun encodeToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }
    
    /**
     * Decode Base64 string to bytes.
     */
    fun decodeFromBase64(data: String): ByteArray {
        return Base64.decode(data, Base64.NO_WRAP)
    }
}
