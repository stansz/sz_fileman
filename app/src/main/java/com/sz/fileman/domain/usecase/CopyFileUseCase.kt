package com.sz.fileman.domain.usecase

import com.sz.fileman.domain.repository.FileRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for copying files or directories.
 */
class CopyFileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    /**
     * Copy a file or directory from source to destination.
     * @param sourcePath The source file/directory path
     * @param destinationPath The destination path
     * @return Result containing success status and message
     */
    suspend operator fun invoke(
        sourcePath: String,
        destinationPath: String
    ): FileOperationResult {
        return try {
            Timber.d("Copying file from $sourcePath to $destinationPath")
            
            // Check if source exists
            if (!fileRepository.exists(sourcePath)) {
                return FileOperationResult.failure("Source file not found")
            }
            
            // Perform copy
            val success = fileRepository.copy(sourcePath, destinationPath)
            
            if (success) {
                Timber.d("File copied successfully")
                FileOperationResult.success("File copied successfully")
            } else {
                Timber.e("Failed to copy file")
                FileOperationResult.failure("Failed to copy file")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error copying file")
            FileOperationResult.failure("Error copying file: ${e.message}")
        }
    }
}

/**
 * Result of a file operation.
 */
data class FileOperationResult(
    val isSuccess: Boolean,
    val message: String
) {
    companion object {
        fun success(message: String) = FileOperationResult(true, message)
        fun failure(message: String) = FileOperationResult(false, message)
    }
}
