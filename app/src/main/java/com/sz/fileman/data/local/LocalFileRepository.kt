package com.sz.fileman.data.local

import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Implementation of FileRepository for local file system operations.
 */
class LocalFileRepository : FileRepository {
    
    override fun getFiles(path: String): Flow<List<FileItem>> {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isDirectory) {
                Timber.w("Path does not exist or is not a directory: $path")
                return flowOf(emptyList())
            }
            
            val files = file.listFiles()?.map { FileItem.fromFile(it) } ?: emptyList()
            flowOf(files)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get files for path: $path")
            flowOf(emptyList())
        }
    }
    
    override suspend fun getFile(path: String): FileItem? {
        return try {
            val file = File(path)
            if (file.exists()) {
                FileItem.fromFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get file: $path")
            null
        }
    }
    
    override suspend fun createDirectory(parentPath: String, name: String): FileItem? {
        return try {
            val parentFile = File(parentPath)
            if (!parentFile.exists() || !parentFile.isDirectory) {
                Timber.w("Parent path does not exist or is not a directory: $parentPath")
                return null
            }
            
            val newDir = File(parentFile, name)
            if (newDir.exists()) {
                Timber.w("Directory already exists: ${newDir.absolutePath}")
                return null
            }
            
            val created = newDir.mkdirs()
            if (created) {
                Timber.d("Created directory: ${newDir.absolutePath}")
                FileItem.fromFile(newDir)
            } else {
                Timber.w("Failed to create directory: ${newDir.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create directory: $name in $parentPath")
            null
        }
    }
    
    override suspend fun delete(path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Timber.w("File does not exist: $path")
                return false
            }
            
            val deleted = if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            
            if (deleted) {
                Timber.d("Deleted: $path")
            } else {
                Timber.w("Failed to delete: $path")
            }
            deleted
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete: $path")
            false
        }
    }
    
    override suspend fun rename(oldPath: String, newName: String): Boolean {
        return try {
            val oldFile = File(oldPath)
            if (!oldFile.exists()) {
                Timber.w("File does not exist: $oldPath")
                return false
            }
            
            val newFile = File(oldFile.parent, newName)
            val renamed = oldFile.renameTo(newFile)
            
            if (renamed) {
                Timber.d("Renamed: $oldPath to ${newFile.absolutePath}")
            } else {
                Timber.w("Failed to rename: $oldPath to $newName")
            }
            renamed
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename: $oldPath to $newName")
            false
        }
    }
    
    override suspend fun copy(sourcePath: String, destinationPath: String): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            val destFile = File(destinationPath)
            
            if (!sourceFile.exists()) {
                Timber.w("Source file does not exist: $sourcePath")
                return false
            }
            
            if (destFile.exists()) {
                Timber.w("Destination file already exists: $destinationPath")
                return false
            }
            
            if (sourceFile.isDirectory) {
                // Copy directory recursively
                copyDirectory(sourceFile, destFile)
            } else {
                // Copy file
                sourceFile.copyTo(destFile, overwrite = false)
            }
            
            Timber.d("Copied: $sourcePath to $destinationPath")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy: $sourcePath to $destinationPath")
            false
        }
    }
    
    override suspend fun move(sourcePath: String, destinationPath: String): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            val destFile = File(destinationPath)
            
            if (!sourceFile.exists()) {
                Timber.w("Source file does not exist: $sourcePath")
                return false
            }
            
            if (destFile.exists()) {
                Timber.w("Destination file already exists: $destinationPath")
                return false
            }
            
            val moved = sourceFile.renameTo(destFile)
            
            if (moved) {
                Timber.d("Moved: $sourcePath to $destinationPath")
            } else {
                Timber.w("Failed to move: $sourcePath to $destinationPath")
            }
            moved
        } catch (e: Exception) {
            Timber.e(e, "Failed to move: $sourcePath to $destinationPath")
            false
        }
    }
    
    override suspend fun exists(path: String): Boolean {
        return try {
            File(path).exists()
        } catch (e: Exception) {
            Timber.e(e, "Failed to check if path exists: $path")
            false
        }
    }
    
    override fun getParentPath(path: String): String? {
        return try {
            val file = File(path)
            file.parent
        } catch (e: Exception) {
            Timber.e(e, "Failed to get parent path: $path")
            null
        }
    }
    
    override suspend fun getSize(path: String): Long {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return 0
            }
            
            if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get size: $path")
            0
        }
    }
    
    override fun search(path: String, query: String, recursive: Boolean): Flow<List<FileItem>> {
        return try {
            val rootFile = File(path)
            if (!rootFile.exists() || !rootFile.isDirectory) {
                return flowOf(emptyList())
            }
            
            val results = mutableListOf<FileItem>()
            searchDirectory(rootFile, query, recursive, results)
            
            flowOf(results)
        } catch (e: Exception) {
            Timber.e(e, "Failed to search: $query in $path")
            flowOf(emptyList())
        }
    }
    
    /**
     * Copy a directory recursively.
     */
    private fun copyDirectory(source: File, dest: File) {
        if (!dest.exists()) {
            dest.mkdirs()
        }
        
        source.listFiles()?.forEach { file ->
            val destFile = File(dest, file.name)
            if (file.isDirectory) {
                copyDirectory(file, destFile)
            } else {
                file.copyTo(destFile, overwrite = false)
            }
        }
    }
    
    /**
     * Get the total size of a directory.
     */
    private fun getDirectorySize(directory: File): Long {
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
    
    /**
     * Search for files matching a query.
     */
    private fun searchDirectory(
        directory: File,
        query: String,
        recursive: Boolean,
        results: MutableList<FileItem>
    ) {
        directory.listFiles()?.forEach { file ->
            if (file.name.contains(query, ignoreCase = true)) {
                results.add(FileItem.fromFile(file))
            }
            
            if (recursive && file.isDirectory) {
                searchDirectory(file, query, recursive, results)
            }
        }
    }
}
