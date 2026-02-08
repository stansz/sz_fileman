package com.sz.fileman.domain.usecase

import com.sz.fileman.domain.repository.FileRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for moving (renaming) files or directories.
 */
class MoveFileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    /**
     * Move a file or directory from source to destination.
     * @param sourcePath The source file/directory path
     * @param destinationPath The destination path
     * @return Result containing success status and message
     */
    suspend operator fun invoke(
        sourcePath: String,
        destinationPath: String
    ): FileOperationResult {
        return try {
            Timber.d("Moving file from $sourcePath to $destinationPath")
            
            // Check if source exists
            if (!fileRepository.exists(sourcePath)) {
                return FileOperationResult.failure("Source file not found")
            }
            
            // Check if destination already exists
            if (fileRepository.exists(destinationPath)) {
                return FileOperationResult.failure("Destination already exists")
            }
            
            // Perform move
            val success = fileRepository.move(sourcePath, destinationPath)
            
            if (success) {
                Timber.d("File moved successfully")
                FileOperationResult.success("File moved successfully")
            } else {
                Timber.e("Failed to move file")
                FileOperationResult.failure("Failed to move file")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error moving file")
            FileOperationResult.failure("Error moving file: ${e.message}")
        }
    }
}
