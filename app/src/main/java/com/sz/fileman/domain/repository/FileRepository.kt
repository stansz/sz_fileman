package com.sz.fileman.domain.repository

import com.sz.fileman.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for file operations.
 * Provides methods for browsing and managing files.
 */
interface FileRepository {
    
    /**
     * Get files in a directory.
     * @param path The directory path
     * @return Flow emitting list of FileItem
     */
    fun getFiles(path: String): Flow<List<FileItem>>
    
    /**
     * Get a file item by path.
     * @param path The file path
     * @return FileItem or null if not found
     */
    suspend fun getFile(path: String): FileItem?
    
    /**
     * Create a new directory.
     * @param parentPath The parent directory path
     * @param name The directory name
     * @return Created FileItem or null on failure
     */
    suspend fun createDirectory(parentPath: String, name: String): FileItem?
    
    /**
     * Delete a file or directory.
     * @param path The file/directory path
     * @return true if successful
     */
    suspend fun delete(path: String): Boolean
    
    /**
     * Rename a file or directory.
     * @param oldPath The current path
     * @param newName The new name
     * @return true if successful
     */
    suspend fun rename(oldPath: String, newName: String): Boolean
    
    /**
     * Copy a file or directory.
     * @param sourcePath The source path
     * @param destinationPath The destination path
     * @return true if successful
     */
    suspend fun copy(sourcePath: String, destinationPath: String): Boolean
    
    /**
     * Move a file or directory.
     * @param sourcePath The source path
     * @param destinationPath The destination path
     * @return true if successful
     */
    suspend fun move(sourcePath: String, destinationPath: String): Boolean
    
    /**
     * Check if a path exists.
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
     * Search for files matching a query.
     * @param path The search root path
     * @param query The search query
     * @param recursive Whether to search recursively
     * @return Flow emitting matching FileItem list
     */
    fun search(path: String, query: String, recursive: Boolean = true): Flow<List<FileItem>>
}
