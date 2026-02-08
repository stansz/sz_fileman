package com.sz.fileman.data.nas

import com.sz.fileman.core.security.storage.SecureStorage
import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.model.NasConnection
import com.sz.fileman.domain.model.NasConnectionResult
import com.sz.fileman.domain.model.NasConnectionStatus
import com.sz.fileman.domain.repository.NasRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NasRepository for NAS file operations.
 * Uses SmbClient for SMB/CIFS protocol communication.
 */
@Singleton
class NasFileRepository @Inject constructor(
    private val smbClient: SmbClient,
    private val secureStorage: SecureStorage
) : NasRepository {
    
    private val connectionStatus = MutableStateFlow(
        NasConnectionResult.disconnected()
    )
    private var currentConnection: NasConnection? = null
    
    companion object {
        private const val KEY_CONNECTIONS = "nas_connections"
        private const val KEY_CURRENT_CONNECTION = "current_nas_connection"
    }
    
    override fun getConnections(): Flow<List<NasConnection>> {
        return try {
            val connectionsJson = secureStorage.getString(KEY_CONNECTIONS, "[]")
            val connections = parseConnections(connectionsJson ?: "[]")
            connections
        } catch (e: Exception) {
            Timber.e(e, "Failed to get connections")
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    
    override suspend fun getConnection(id: String): NasConnection? {
        return try {
            val connections = getConnections().map { it }.reduce { acc, list -> acc + list }
            connections.find { it.id == id }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get connection: $id")
            null
        }
    }
    
    override suspend fun saveConnection(connection: NasConnection): NasConnection? {
        return try {
            Timber.d("Saving NAS connection: ${connection.name}")
            
            val connections = getConnections()
                .map { it }
                .reduce { acc, list -> acc + list }
                .toMutableList()
            
            // Remove existing connection with same ID
            connections.removeAll { it.id == connection.id }
            
            // Add new connection
            connections.add(connection)
            
            // Save to secure storage
            val connectionsJson = serializeConnections(connections)
            secureStorage.putString(KEY_CONNECTIONS, connectionsJson)
            
            Timber.d("Connection saved successfully")
            connection
        } catch (e: Exception) {
            Timber.e(e, "Failed to save connection")
            null
        }
    }
    
    override suspend fun deleteConnection(id: String): Boolean {
        return try {
            Timber.d("Deleting NAS connection: $id")
            
            val connections = getConnections()
                .map { it }
                .reduce { acc, list -> acc + list }
                .toMutableList()
            
            // Remove connection
            val removed = connections.removeAll { it.id == id }
            
            if (removed) {
                // Save updated list
                val connectionsJson = serializeConnections(connections)
                secureStorage.putString(KEY_CONNECTIONS, connectionsJson)
                Timber.d("Connection deleted successfully")
                true
            } else {
                Timber.w("Connection not found: $id")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete connection")
            false
        }
    }
    
    override suspend fun testConnection(connection: NasConnection): NasConnectionResult {
        return try {
            Timber.d("Testing NAS connection: ${connection.host}")
            
            connectionStatus.value = NasConnectionResult.connecting()
            
            val connected = smbClient.connect(
                host = connection.host,
                port = connection.port,
                username = if (connection.isAnonymous) "" else connection.username,
                password = if (connection.isAnonymous) "" else connection.password,
                domain = connection.domain,
                workgroup = connection.workgroup
            )
            
            if (connected) {
                smbClient.disconnect()
                NasConnectionResult.success(connection)
            } else {
                NasConnectionResult.error("Failed to connect to NAS")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error testing connection")
            NasConnectionResult.error("Connection test failed: ${e.message}")
        } finally {
            connectionStatus.value = NasConnectionResult.disconnected()
        }
    }
    
    override suspend fun connect(connection: NasConnection): NasConnectionResult {
        return try {
            Timber.d("Connecting to NAS: ${connection.name}")
            
            connectionStatus.value = NasConnectionResult.connecting()
            
            val connected = smbClient.connect(
                host = connection.host,
                port = connection.port,
                username = if (connection.isAnonymous) "" else connection.username,
                password = if (connection.isAnonymous) "" else connection.password,
                domain = connection.domain,
                workgroup = connection.workgroup
            )
            
            if (connected) {
                currentConnection = connection.updateLastConnected()
                connectionStatus.value = NasConnectionResult.success(currentConnection!!)
                Timber.d("Successfully connected to NAS")
                NasConnectionResult.success(currentConnection!!)
            } else {
                NasConnectionResult.error("Failed to connect to NAS")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error connecting to NAS")
            NasConnectionResult.error("Connection failed: ${e.message}")
        }
    }
    
    override suspend fun disconnect() {
        try {
            Timber.d("Disconnecting from NAS")
            smbClient.disconnect()
            currentConnection = null
            connectionStatus.value = NasConnectionResult.disconnected()
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting from NAS")
        }
    }
    
    override fun getConnectionStatus(): Flow<NasConnectionResult> = connectionStatus
    
    override fun getFiles(path: String): Flow<List<FileItem>> {
        return kotlinx.coroutines.flow.flow {
            try {
                if (!smbClient.isCurrentlyConnected()) {
                    Timber.w("Not connected to NAS")
                    emit(emptyList())
                    return@flow
                }
                
                val files = smbClient.listFiles(path)
                emit(files)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get files from NAS")
                emit(emptyList())
            }
        }
    }
    
    override suspend fun getFile(path: String): FileItem? {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return null
            }
            smbClient.getFile(path)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get file from NAS")
            null
        }
    }
    
    override suspend fun createDirectory(parentPath: String, name: String): FileItem? {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return null
            }
            
            val newPath = "$parentPath/$name".replace("//", "/")
            val created = smbClient.createDirectory(newPath)
            
            if (created) {
                smbClient.getFile(newPath)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create directory on NAS")
            null
        }
    }
    
    override suspend fun delete(path: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            smbClient.delete(path)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete from NAS")
            false
        }
    }
    
    override suspend fun rename(oldPath: String, newName: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            smbClient.rename(oldPath, newName)
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename on NAS")
            false
        }
    }
    
    override suspend fun copy(sourcePath: String, destinationPath: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            smbClient.copy(sourcePath, destinationPath)
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy on NAS")
            false
        }
    }
    
    override suspend fun move(sourcePath: String, destinationPath: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            smbClient.move(sourcePath, destinationPath)
        } catch (e: Exception) {
            Timber.e(e, "Failed to move on NAS")
            false
        }
    }
    
    override suspend fun downloadFile(remotePath: String, localPath: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            
            Timber.d("Downloading file from NAS: $remotePath to $localPath")
            val localFile = File(localPath)
            
            // Ensure parent directory exists
            localFile.parentFile?.mkdirs()
            
            FileOutputStream(localFile).use { outputStream ->
                smbClient.downloadFile(remotePath, outputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to download file from NAS")
            false
        }
    }
    
    override suspend fun uploadFile(localPath: String, remotePath: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            
            Timber.d("Uploading file to NAS: $localPath to $remotePath")
            val localFile = File(localPath)
            
            if (!localFile.exists()) {
                Timber.w("Local file does not exist: $localPath")
                return false
            }
            
            FileInputStream(localFile).use { inputStream ->
                smbClient.uploadFile(inputStream, remotePath)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload file to NAS")
            false
        }
    }
    
    override suspend fun exists(path: String): Boolean {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return false
            }
            smbClient.exists(path)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check existence on NAS")
            false
        }
    }
    
    override fun getParentPath(path: String): String? {
        return try {
            val lastSlash = path.lastIndexOf('/')
            if (lastSlash > 0) {
                path.substring(0, lastSlash)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get parent path")
            null
        }
    }
    
    override suspend fun getSize(path: String): Long {
        return try {
            if (!smbClient.isCurrentlyConnected()) {
                Timber.w("Not connected to NAS")
                return 0L
            }
            smbClient.getSize(path)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get size from NAS")
            0L
        }
    }
    
    override fun search(path: String, query: String, recursive: Boolean): Flow<List<FileItem>> {
        return kotlinx.coroutines.flow.flow {
            try {
                if (!smbClient.isCurrentlyConnected()) {
                    Timber.w("Not connected to NAS")
                    emit(emptyList())
                    return@flow
                }
                
                val results = mutableListOf<FileItem>()
                searchDirectory(path, query, recursive, results)
                emit(results)
            } catch (e: Exception) {
                Timber.e(e, "Failed to search on NAS")
                emit(emptyList())
            }
        }
    }
    
    /**
     * Search directory recursively for files matching query.
     */
    private suspend fun searchDirectory(
        path: String,
        query: String,
        recursive: Boolean,
        results: MutableList<FileItem>
    ) {
        try {
            val files = smbClient.listFiles(path)
            
            files.forEach { file ->
                if (file.name.contains(query, ignoreCase = true)) {
                    results.add(file)
                }
                
                if (recursive && file.isDirectory) {
                    searchDirectory(file.path, query, recursive, results)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching directory: $path")
        }
    }
    
    /**
     * Serialize connections list to JSON string.
     * Note: In production, use a proper JSON library like Gson or kotlinx.serialization.
     */
    private fun serializeConnections(connections: List<NasConnection>): String {
        return connections.joinToString(separator = ",") { conn ->
            """{"id":"${conn.id}","name":"${conn.name}","host":"${conn.host}","port":${conn.port},"username":"${conn.username}","password":"${conn.password}","domain":"${conn.domain}","share":"${conn.share}","workgroup":"${conn.workgroup}","isAnonymous":${conn.isAnonymous},"isDefault":${conn.isDefault},"createdAt":${conn.createdAt},"lastConnectedAt":${conn.lastConnectedAt}}"""
        }
    }
    
    /**
     * Parse connections from JSON string.
     * Note: In production, use a proper JSON library like Gson or kotlinx.serialization.
     */
    private fun parseConnections(json: String): List<NasConnection> {
        return try {
            // Simple JSON parsing - in production use proper JSON library
            val connections = mutableListOf<NasConnection>()
            val parts = json.split("},")
            
            parts.forEach { part ->
                try {
                    val id = extractValue(part, "id")
                    val name = extractValue(part, "name")
                    val host = extractValue(part, "host")
                    val port = extractIntValue(part, "port")
                    val username = extractValue(part, "username")
                    val password = extractValue(part, "password")
                    val domain = extractValue(part, "domain")
                    val share = extractValue(part, "share")
                    val workgroup = extractValue(part, "workgroup")
                    val isAnonymous = extractBooleanValue(part, "isAnonymous")
                    val isDefault = extractBooleanValue(part, "isDefault")
                    val createdAt = extractLongValue(part, "createdAt")
                    val lastConnectedAt = extractLongValue(part, "lastConnectedAt")
                    
                    if (id.isNotEmpty()) {
                        connections.add(
                            NasConnection(
                                id = id,
                                name = name,
                                host = host,
                                port = port,
                                username = username,
                                password = password,
                                domain = domain,
                                share = share,
                                workgroup = workgroup,
                                isAnonymous = isAnonymous,
                                isDefault = isDefault,
                                createdAt = createdAt,
                                lastConnectedAt = lastConnectedAt
                            )
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing connection")
                }
            }
            
            connections
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse connections")
            emptyList()
        }
    }
    
    private fun extractValue(json: String, key: String): String {
        val regex = "\"$key\":\"([^\"]*)\"".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1) ?: ""
    }
    
    private fun extractIntValue(json: String, key: String): Int {
        val regex = "\"$key\":(\\d+)".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    
    private fun extractBooleanValue(json: String, key: String): Boolean {
        val regex = "\"$key\":(true|false)".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toBoolean() ?: false
    }
    
    private fun extractLongValue(json: String, key: String): Long {
        val regex = "\"$key\":(\\d+)".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }
}
