package com.sz.fileman.domain.repository

import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.model.NasConnection
import com.sz.fileman.domain.model.NasConnectionResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for NAS (Network Attached Storage) operations.
 * Provides methods for managing NAS connections and browsing NAS files.
 */
interface NasRepository {
    
    /**
     * Get all saved NAS connections.
     * @return Flow emitting list of NasConnection
     */
    fun getConnections(): Flow<List<NasConnection>>
    
    /**
     * Get a NAS connection by ID.
     * @param id The connection ID
     * @return NasConnection or null if not found
     */
    suspend fun getConnection(id: String): NasConnection?
    
    /**
     * Save a NAS connection.
     * @param connection The connection to save
     * @return Saved NasConnection or null on failure
     */
    suspend fun saveConnection(connection: NasConnection): NasConnection?
    
    /**
     * Delete a NAS connection.
     * @param id The connection ID
     * @return true if successful
     */
    suspend fun deleteConnection(id: String): Boolean
    
    /**
     * Test a NAS connection.
     * @param connection The connection to test
     * @return NasConnectionResult with connection status
     */
    suspend fun testConnection(connection: NasConnection): NasConnectionResult
    
    /**
     * Connect to a NAS.
     * @param connection The connection to use
     * @return NasConnectionResult with connection status
     */
    suspend fun connect(connection: NasConnection): NasConnectionResult
    
    /**
     * Disconnect from the current NAS.
     */
    suspend fun disconnect()
    
    /**
     * Get the current connection status.
     * @return Flow emitting current NasConnectionResult
     */
    fun getConnectionStatus(): Flow<NasConnectionResult>
    
    /**
     * Get files from a NAS directory.
     * @param path The directory path on the NAS
     * @return Flow emitting list of FileItem
     */
    fun getFiles(path: String): Flow<List<FileItem>>
    
    /**
     * Get a file item from the NAS by path.
     * @param path The file path on the NAS
     * @return FileItem or null if not found
     */
    suspend fun getFile(path: String): FileItem?
    
    /**
     * Create a new directory on the NAS.
     * @param parentPath The parent directory path
     * @param name The directory name
     * @return Created FileItem or null on failure
     */
    suspend fun createDirectory(parentPath: String, name: String): FileItem?
    
    /**
     * Delete a file or directory on the NAS.
     * @param path The file/directory path
     * @return true if successful
     */
    suspend fun delete(path: String): Boolean
    
    /**
     * Rename a file or directory on the NAS.
     * @param oldPath The current path
     * @param newName The new name
     * @return true if successful
     */
    suspend fun rename(oldPath: String, newName: String): Boolean
    
    /**
     * Copy a file or directory on the NAS.
     * @param sourcePath The source path
     * @param destinationPath The destination path
     * @return true if successful
     */
    suspend fun copy(sourcePath: String, destinationPath: String): Boolean
    
    /**
     * Move a file or directory on the NAS.
     * @param sourcePath The source path
     * @param destinationPath The destination path
     * @return true if successful
     */
    suspend fun move(sourcePath: String, destinationPath: String): Boolean
    
    /**
     * Download a file from NAS to local storage.
     * @param remotePath The path on the NAS
     * @param localPath The local destination path
     * @return true if successful
     */
    suspend fun downloadFile(remotePath: String, localPath: String): Boolean
    
    /**
     * Upload a file from local storage to NAS.
     * @param localPath The local file path
     * @param remotePath The destination path on the NAS
     * @return true if successful
     */
    suspend fun uploadFile(localPath: String, remotePath: String): Boolean
    
    /**
     * Check if a path exists on the NAS.
     * @param path The path to check
     * @return true if exists
     */
    suspend fun exists(path: String): Boolean
    
    /**
     * Get the parent directory path.
     * @param path The current path
     * @return Parent path or null if at root
     */
    fun getParentPath(path: String): String?
    
    /**
     * Get the file size.
     * @param path The file path
     * @return File size in bytes
     */
    suspend fun getSize(path: String): Long
    
    /**
     * Search for files on the NAS.
     * @param path The search root path
     * @param query The search query
     * @param recursive Whether to search recursively
     * @return Flow emitting matching FileItem list
     */
    fun search(path: String, query: String, recursive: Boolean = true): Flow<List<FileItem>>
}
