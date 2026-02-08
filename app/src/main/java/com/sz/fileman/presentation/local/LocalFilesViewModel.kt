package com.sz.fileman.presentation.local

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.model.SortOption
import com.sz.fileman.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for local files screen.
 * Manages file browsing and operations.
 */
@HiltViewModel
class LocalFilesViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LocalFilesUiState>(LocalFilesUiState.Loading)
    val uiState: StateFlow<LocalFilesUiState> = _uiState.asStateFlow()
    
    private val _currentPath = MutableStateFlow(getDefaultPath())
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()
    
    private val _selectedFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedFiles: StateFlow<Set<String>> = _selectedFiles.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    init {
        loadFiles()
    }
    
    /**
     * Load files in the current directory.
     */
    fun loadFiles() {
        viewModelScope.launch {
            _uiState.value = LocalFilesUiState.Loading
            
            try {
                fileRepository.getFiles(_currentPath.value).collect { files ->
                    val sortedFiles = sortFiles(files, _sortOption.value)
                    _uiState.value = LocalFilesUiState.Success(sortedFiles)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load files")
                _uiState.value = LocalFilesUiState.Error(e.message ?: "Failed to load files")
            }
        }
    }
    
    /**
     * Navigate to a directory.
     */
    fun navigateToDirectory(path: String) {
        _currentPath.value = path
        _selectedFiles.value = emptySet()
        loadFiles()
    }
    
    /**
     * Navigate to parent directory.
     */
    fun navigateUp() {
        val parentPath = fileRepository.getParentPath(_currentPath.value)
        if (parentPath != null) {
            navigateToDirectory(parentPath)
        }
    }
    
    /**
     * Toggle file selection.
     */
    fun toggleFileSelection(fileId: String) {
        val current = _selectedFiles.value.toMutableSet()
        if (fileId in current) {
            current.remove(fileId)
        } else {
            current.add(fileId)
        }
        _selectedFiles.value = current
    }
    
    /**
     * Clear all selections.
     */
    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }
    
    /**
     * Delete selected files.
     */
    fun deleteSelectedFiles() {
        viewModelScope.launch {
            val selected = _selectedFiles.value
            var successCount = 0
            var failCount = 0
            
            selected.forEach { path ->
                val result = fileRepository.delete(path)
                if (result) {
                    successCount++
                } else {
                    failCount++
                }
            }
            
            Timber.d("Deleted $successCount files, failed $failCount")
            clearSelection()
            loadFiles()
        }
    }
    
    /**
     * Create a new directory.
     */
    fun createDirectory(name: String) {
        viewModelScope.launch {
            val result = fileRepository.createDirectory(_currentPath.value, name)
            if (result != null) {
                Timber.d("Created directory: $name")
                loadFiles()
            } else {
                Timber.w("Failed to create directory: $name")
            }
        }
    }
    
    /**
     * Update sort option.
     */
    fun updateSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
        // Reload files with new sort
        (_uiState.value as? LocalFilesUiState.Success)?.let { state ->
            _uiState.value = LocalFilesUiState.Success(
                sortFiles(state.files, sortOption)
            )
        }
    }
    
    /**
     * Sort files based on sort option.
     */
    private fun sortFiles(files: List<FileItem>, sortOption: SortOption): List<FileItem> {
        return when (sortOption) {
            SortOption.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_ASC -> files.sortedBy { it.lastModified }
            SortOption.DATE_DESC -> files.sortedByDescending { it.lastModified }
            SortOption.SIZE_ASC -> files.sortedBy { it.size }
            SortOption.SIZE_DESC -> files.sortedByDescending { it.size }
            SortOption.TYPE_ASC -> files.sortedBy { it.fileType?.displayName ?: "" }
            SortOption.TYPE_DESC -> files.sortedByDescending { it.fileType?.displayName ?: "" }
        }
    }
    
    /**
     * Get default path (external storage).
     */
    private fun getDefaultPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }
}

/**
 * UI state for local files screen.
 */
sealed class LocalFilesUiState {
    data object Loading : LocalFilesUiState()
    data class Success(val files: List<FileItem>) : LocalFilesUiState()
    data class Error(val message: String) : LocalFilesUiState()
}
