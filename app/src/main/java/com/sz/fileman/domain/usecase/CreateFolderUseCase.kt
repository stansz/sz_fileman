package com.sz.fileman.domain.usecase

import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.repository.FileRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for creating new directories.
 */
class CreateFolderUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    /**
     * Create a new directory.
     * @param parentPath The parent directory path
     * @param name The name of the new directory
     * @return Result containing success status, message, and created FileItem
     */
    suspend operator fun invoke(
        parentPath: String,
        name: String
    ): CreateFolderResult {
        return try {
            Timber.d("Creating folder '$name' in $parentPath")
            
            // Validate folder name
            if (name.isBlank()) {
                return CreateFolderResult.failure("Folder name cannot be empty")
            }
            
            if (name.contains("/") || name.contains("\\")) {
                return CreateFolderResult.failure("Folder name contains invalid characters")
            }
            
            // Check if parent exists
            if (!fileRepository.exists(parentPath)) {
                return CreateFolderResult.failure("Parent directory not found")
            }
            
            // Create the directory
            val createdItem = fileRepository.createDirectory(parentPath, name)
            
            if (createdItem != null) {
                Timber.d("Folder created successfully")
                CreateFolderResult.success("Folder created successfully", createdItem)
            } else {
                Timber.e("Failed to create folder")
                CreateFolderResult.failure("Failed to create folder")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating folder")
            CreateFolderResult.failure("Error creating folder: ${e.message}")
        }
    }
}

/**
 * Result of a create folder operation.
 */
data class CreateFolderResult(
    val isSuccess: Boolean,
    val message: String,
    val fileItem: FileItem? = null
) {
    companion object {
        fun success(message: String, fileItem: FileItem) = 
            CreateFolderResult(true, message, fileItem)
        fun failure(message: String) = 
            CreateFolderResult(false, message, null)
    }
}
