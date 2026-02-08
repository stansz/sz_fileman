package com.sz.fileman.domain.usecase

import com.sz.fileman.domain.model.NasConnection
import com.sz.fileman.domain.model.NasConnectionResult
import com.sz.fileman.domain.repository.NasRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for connecting to a NAS (Network Attached Storage).
 */
class ConnectNasUseCase @Inject constructor(
    private val nasRepository: NasRepository
) {
    /**
     * Connect to a NAS.
     * @param connection The NAS connection configuration
     * @return NasConnectionResult with connection status
     */
    suspend operator fun invoke(connection: NasConnection): NasConnectionResult {
        return try {
            Timber.d("Connecting to NAS: ${connection.name} at ${connection.host}")
            
            // Validate connection
            if (!connection.isValid()) {
                return NasConnectionResult.error("Invalid connection configuration")
            }
            
            // Update last connected timestamp
            val updatedConnection = connection.updateLastConnected()
            
            // Save the connection
            nasRepository.saveConnection(updatedConnection)
            
            // Attempt to connect
            val result = nasRepository.connect(updatedConnection)
            
            if (result.status == NasConnectionStatus.CONNECTED) {
                Timber.d("Successfully connected to NAS")
                result
            } else {
                Timber.e("Failed to connect to NAS: ${result.message}")
                result
            }
        } catch (e: Exception) {
            Timber.e(e, "Error connecting to NAS")
            NasConnectionResult.error("Error connecting to NAS: ${e.message}")
        }
    }
    
    /**
     * Disconnect from the current NAS.
     */
    suspend fun disconnect() {
        try {
            Timber.d("Disconnecting from NAS")
            nasRepository.disconnect()
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting from NAS")
        }
    }
    
    /**
     * Test a NAS connection without saving it.
     * @param connection The NAS connection configuration to test
     * @return NasConnectionResult with test result
     */
    suspend fun testConnection(connection: NasConnection): NasConnectionResult {
        return try {
            Timber.d("Testing NAS connection: ${connection.host}")
            
            if (!connection.isValid()) {
                return NasConnectionResult.error("Invalid connection configuration")
            }
            
            nasRepository.testConnection(connection)
        } catch (e: Exception) {
            Timber.e(e, "Error testing NAS connection")
            NasConnectionResult.error("Error testing connection: ${e.message}")
        }
    }
}
