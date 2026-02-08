package com.sz.fileman.data.nas

import com.sz.fileman.domain.model.FileItem
import jcifs.CIFSContext
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for SMB/CIFS file operations using JCIFS-NG.
 * Handles connections to NAS devices and file operations.
 */
@Singleton
class SmbClient @Inject constructor() {
    
    private var currentContext: CIFSContext? = null
    private var isConnected = false
    
    /**
     * Connect to an SMB server.
     * @param host The server hostname or IP address
     * @param port The SMB port (default 445)
     * @param username The username for authentication
     * @param password The password for authentication
     * @param domain The domain for authentication (optional)
     * @param workgroup The workgroup (optional)
     * @return true if connection successful
     */
    suspend fun connect(
        host: String,
        port: Int = 445,
        username: String,
        password: String,
        domain: String = "",
        workgroup: String = ""
    ): Boolean {
        return try {
            Timber.d("Connecting to SMB server: $host:$port")
            
            // Create authentication
            val auth = NtlmPasswordAuthenticator()
            if (username.isNotBlank()) {
                auth.setUserPasswordCredentials(username, password, domain, workgroup)
            }
            
            // Create SMB context
            val properties = java.util.Properties()
            properties["jcifs.smb.client.minVersion"] = "SMB202"
            properties["jcifs.smb.client.maxVersion"] = "SMB311"
            properties["jcifs.smb.client.responseTimeout"] = "60000"
            properties["jcifs.smb.client.soTimeout"] = "60000"
            
            currentContext = BaseContext(properties).withCredentials(auth)
            isConnected = true
            
            Timber.d("Successfully connected to SMB server")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to connect to SMB server")
            isConnected = false
            false
        }
    }
    
    /**
     * Disconnect from the SMB server.
     */
    fun disconnect() {
        try {
            currentContext = null
            isConnected = false
            Timber.d("Disconnected from SMB server")
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting from SMB server")
        }
    }
    
    /**
     * Check if currently connected.
     */
    fun isCurrentlyConnected(): Boolean = isConnected
    
    /**
     * List files in an SMB directory.
     * @param path The SMB path (e.g., "smb://server/share/folder")
     * @return List of FileItem or empty list on failure
     */
    suspend fun listFiles(path: String): List<FileItem> {
        return try {
            Timber.d("Listing files in: $path")
            
            val smbFile = createSmbFile(path)
            if (!smbFile.exists()) {
                Timber.w("Path does not exist: $path")
                return emptyList()
            }
            
            val files = smbFile.listFiles()
                ?.map { smbFileToFileItem(it) }
                ?: emptyList()
            
            Timber.d("Found ${files.size} files")
            files
        } catch (e: Exception) {
            Timber.e(e, "Failed to list files in: $path")
            emptyList()
        }
    }
    
    /**
     * Get file information from SMB path.
     * @param path The SMB path
     * @return FileItem or null on failure
     */
    suspend fun getFile(path: String): FileItem? {
        return try {
            val smbFile = createSmbFile(path)
            if (smbFile.exists()) {
                smbFileToFileItem(smbFile)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get file: $path")
            null
        }
    }
    
    /**
     * Create a directory on SMB server.
     * @param path The SMB path
     * @return true if successful
     */
    suspend fun createDirectory(path: String): Boolean {
        return try {
            Timber.d("Creating directory: $path")
            val smbFile = createSmbFile(path)
            smbFile.mkdirs()
        } catch (e: Exception) {
            Timber.e(e, "Failed to create directory: $path")
            false
        }
    }
    
    /**
     * Delete a file or directory on SMB server.
     * @param path The SMB path
     * @return true if successful
     */
    suspend fun delete(path: String): Boolean {
        return try {
            Timber.d("Deleting: $path")
            val smbFile = createSmbFile(path)
            if (smbFile.isDirectory) {
                smbFile.deleteRecursive()
            } else {
                smbFile.delete()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete: $path")
            false
        }
    }
    
    /**
     * Rename a file or directory on SMB server.
     * @param oldPath The current SMB path
     * @param newName The new name
     * @return true if successful
     */
    suspend fun rename(oldPath: String, newName: String): Boolean {
        return try {
            Timber.d("Renaming: $oldPath to $newName")
            val smbFile = createSmbFile(oldPath)
            val newPath = getParentPath(oldPath) + "/" + newName
            smbFile.renameTo(createSmbFile(newPath))
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename: $oldPath to $newName")
            false
        }
    }
    
    /**
     * Copy a file on SMB server.
     * @param sourcePath The source SMB path
     * @param destinationPath The destination SMB path
     * @return true if successful
     */
    suspend fun copy(sourcePath: String, destinationPath: String): Boolean {
        return try {
            Timber.d("Copying: $sourcePath to $destinationPath")
            val sourceFile = createSmbFile(sourcePath)
            val destFile = createSmbFile(destinationPath)
            
            sourceFile.copyTo(destFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy: $sourcePath to $destinationPath")
            false
        }
    }
    
    /**
     * Move a file on SMB server.
     * @param sourcePath The source SMB path
     * @param destinationPath The destination SMB path
     * @return true if successful
     */
    suspend fun move(sourcePath: String, destinationPath: String): Boolean {
        return try {
            Timber.d("Moving: $sourcePath to $destinationPath")
            val sourceFile = createSmbFile(sourcePath)
            val destFile = createSmbFile(destinationPath)
            sourceFile.renameTo(destFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to move: $sourcePath to $destinationPath")
            false
        }
    }
    
    /**
     * Download a file from SMB server.
     * @param remotePath The SMB path of the file
     * @param outputStream The output stream to write to
     * @return true if successful
     */
    suspend fun downloadFile(remotePath: String, outputStream: OutputStream): Boolean {
        return try {
            Timber.d("Downloading: $remotePath")
            val smbFile = createSmbFile(remotePath)
            smbFile.inputStream.use { input ->
                input.copyTo(outputStream)
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to download: $remotePath")
            false
        }
    }
    
    /**
     * Upload a file to SMB server.
     * @param inputStream The input stream to read from
     * @param remotePath The SMB path to upload to
     * @return true if successful
     */
    suspend fun uploadFile(inputStream: InputStream, remotePath: String): Boolean {
        return try {
            Timber.d("Uploading to: $remotePath")
            val smbFile = createSmbFile(remotePath)
            smbFile.outputStream.use { output ->
                inputStream.copyTo(output)
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload to: $remotePath")
            false
        }
    }
    
    /**
     * Check if a path exists on SMB server.
     * @param path The SMB path
     * @return true if exists
     */
    suspend fun exists(path: String): Boolean {
        return try {
            createSmbFile(path).exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check existence: $path")
            false
        }
    }
    
    /**
     * Get the size of a file or directory.
     * @param path The SMB path
     * @return Size in bytes
     */
    suspend fun getSize(path: String): Long {
        return try {
            val smbFile = createSmbFile(path)
            if (smbFile.isDirectory) {
                getDirectorySize(smbFile)
            } else {
                smbFile.length()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get size: $path")
            0L
        }
    }
    
    /**
     * Create an SmbFile from a path string.
     */
    private fun createSmbFile(path: String): SmbFile {
        return if (currentContext != null) {
            SmbFile(path, currentContext)
        } else {
            SmbFile(path)
        }
    }
    
    /**
     * Convert SmbFile to FileItem.
     */
    private fun smbFileToFileItem(smbFile: SmbFile): FileItem {
        return FileItem(
            id = smbFile.path,
            name = smbFile.name,
            path = smbFile.path,
            isDirectory = smbFile.isDirectory,
            size = if (smbFile.isFile) smbFile.length() else 0,
            lastModified = smbFile.lastModified(),
            fileType = if (smbFile.isFile) {
                val extension = smbFile.name.substringAfterLast('.', "")
                com.sz.fileman.domain.model.FileType.fromExtension(extension)
            } else null
        )
    }
    
    /**
     * Get parent path from a full SMB path.
     */
    private fun getParentPath(path: String): String {
        val lastSlash = path.lastIndexOf('/')
        return if (lastSlash > 0) {
            path.substring(0, lastSlash)
        } else {
            path
        }
    }
    
    /**
     * Get total size of a directory.
     */
    private fun getDirectorySize(directory: SmbFile): Long {
        var size = 0L
        try {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        } catch (e: SmbException) {
            Timber.e(e, "Error getting directory size")
        }
        return size
    }
    
    /**
     * Recursively delete a directory.
     */
    private fun SmbFile.deleteRecursive(): Boolean {
        return try {
            if (isDirectory) {
                listFiles()?.forEach { it.deleteRecursive() }
            }
            delete()
        } catch (e: Exception) {
            Timber.e(e, "Error in recursive delete")
            false
        }
    }
}
