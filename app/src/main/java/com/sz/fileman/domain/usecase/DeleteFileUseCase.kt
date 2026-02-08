package com.sz.fileman.domain.usecase

import com.sz.fileman.domain.repository.FileRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for deleting files or directories.
 */
class DeleteFileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    /**
     * Delete a file or directory.
     * @param path The file/directory path to delete
     * @return Result containing success status and message
     */
    suspend operator fun invoke(path: String): FileOperationResult {
        return try {
            Timber.d("Deleting file at $path")
            
            // Check if file exists
            if (!fileRepository.exists(path)) {
                return FileOperationResult.failure("File not found")
            }
            
            // Perform deletion
            val success = fileRepository.delete(path)
            
            if (success) {
                Timber.d("File deleted successfully")
                FileOperationResult.success("File deleted successfully")
            } else {
                Timber.e("Failed to delete file")
                FileOperationResult.failure("Failed to delete file")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting file")
            FileOperationResult.failure("Error deleting file: ${e.message}")
        }
    }
}
