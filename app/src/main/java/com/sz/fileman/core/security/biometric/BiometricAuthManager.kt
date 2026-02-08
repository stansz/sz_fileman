package com.sz.fileman.core.security.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages biometric authentication for the app.
 * Provides methods to check biometric availability and prompt for authentication.
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * Check if biometric authentication is available on the device.
     */
    @Suppress("WrongConstant")
    fun canAuthenticate(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BIOMETRIC_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("Biometric authentication is available")
                BiometricAvailability.AVAILABLE
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Timber.w("No biometric hardware available")
                BiometricAvailability.NO_HARDWARE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Timber.w("Biometric hardware is currently unavailable")
                BiometricAvailability.HW_UNAVAILABLE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Timber.w("No biometric credentials enrolled")
                BiometricAvailability.NONE_ENROLLED
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Timber.w("Security update required for biometric")
                BiometricAvailability.SECURITY_UPDATE_REQUIRED
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Timber.w("Biometric is not supported")
                BiometricAvailability.UNSUPPORTED
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Timber.w("Biometric status unknown")
                BiometricAvailability.UNKNOWN
            }
            else -> {
                Timber.w("Unknown biometric error")
                BiometricAvailability.UNKNOWN
            }
        }
    }
    
    /**
     * Prompt the user for biometric authentication.
     * Returns true if authentication succeeds, false otherwise.
     */
    @Suppress("WrongConstant")
    suspend fun authenticate(
        title: String = "Authenticate",
        subtitle: String = "Please authenticate to continue",
        description: String = "Use your fingerprint or face to unlock",
        negativeButtonText: String = "Cancel"
    ): Boolean = suspendCancellableCoroutine { continuation ->
        
        val executor = ContextCompat.getMainExecutor(context)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Timber.d("Biometric authentication succeeded")
                continuation.resume(true)
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.d("Biometric authentication failed")
                // Don't resume here, let the user try again
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Timber.e("Biometric authentication error: $errorCode - $errString")
                continuation.resume(false)
            }
        }
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_AUTHENTICATORS)
            .build()
        
        val biometricPrompt = BiometricPrompt(
            context as androidx.fragment.app.FragmentActivity,
            executor,
            callback
        )
        
        biometricPrompt.authenticate(promptInfo)
        
        continuation.invokeOnCancellation {
            Timber.d("Biometric authentication cancelled")
        }
    }
    
    companion object {
        // Using direct integer values for biometric authenticators
        // BIOMETRIC_STRONG = 0x00000001
        // DEVICE_CREDENTIAL = 0x00000002
        private const val BIOMETRIC_AUTHENTICATORS = 0x00000001 or 0x00000002
    }
}

/**
 * Represents the availability status of biometric authentication.
 */
enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}
