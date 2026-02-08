package com.sz.fileman.domain.model

/**
 * Represents a NAS (Network Attached Storage) connection configuration.
 */
data class NasConnection(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = DEFAULT_PORT,
    val username: String,
    val password: String, // Should be encrypted when stored
    val domain: String = "",
    val share: String = "",
    val workgroup: String = "",
    val isAnonymous: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastConnectedAt: Long = 0
) {
    companion object {
        const val DEFAULT_PORT = 445
    }
    
    /**
     * Validate the connection configuration.
     */
    fun isValid(): Boolean {
        return when {
            name.isBlank() -> false
            host.isBlank() -> false
            port < 1 || port > 65535 -> false
            !isAnonymous && username.isBlank() -> false
            !isAnonymous && password.isBlank() -> false
            else -> true
        }
    }
    
    /**
     * Get the SMB URL for this connection.
     */
    fun getSmbUrl(): String {
        return if (isAnonymous) {
            "smb://$host:$port/$share"
        } else {
            val auth = if (domain.isNotBlank()) "$domain\\" else ""
            "smb://$auth$username:$password@$host:$port/$share"
        }
    }
    
    /**
     * Create a copy with updated last connected timestamp.
     */
    fun updateLastConnected(): NasConnection {
        return copy(lastConnectedAt = System.currentTimeMillis())
    }
    
    /**
     * Create a copy with default status toggled.
     */
    fun toggleDefault(): NasConnection {
        return copy(isDefault = !isDefault)
    }
}

/**
 * Represents the status of a NAS connection.
 */
enum class NasConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Represents the result of a NAS connection attempt.
 */
data class NasConnectionResult(
    val status: NasConnectionStatus,
    val message: String? = null,
    val connection: NasConnection? = null
) {
    companion object {
        fun success(connection: NasConnection) = NasConnectionResult(
            status = NasConnectionStatus.CONNECTED,
            connection = connection
        )
        
        fun connecting() = NasConnectionResult(
            status = NasConnectionStatus.CONNECTING
        )
        
        fun error(message: String) = NasConnectionResult(
            status = NasConnectionStatus.ERROR,
            message = message
        )
        
        fun disconnected() = NasConnectionResult(
            status = NasConnectionStatus.DISCONNECTED
        )
    }
}
