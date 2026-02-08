package com.sz.fileman.domain.model

/**
 * Represents sorting options for file lists.
 */
enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_ASC("Date (Oldest First)"),
    DATE_DESC("Date (Newest First)"),
    SIZE_ASC("Size (Smallest First)"),
    SIZE_DESC("Size (Largest First)"),
    TYPE_ASC("Type (A-Z)"),
    TYPE_DESC("Type (Z-A)");
    
    companion object {
        /**
         * Get all sort options.
         */
        fun getAll(): List<SortOption> = values().toList()
        
        /**
         * Get sort option by index.
         */
        fun fromIndex(index: Int): SortOption {
            return values().getOrNull(index) ?: NAME_ASC
        }
    }
}

/**
 * Represents the view mode for file lists.
 */
enum class ViewMode {
    LIST,
    GRID
}

/**
 * Represents filter options for file lists.
 */
data class FilterOptions(
    val showHiddenFiles: Boolean = false,
    val fileCategories: Set<FileCategory> = FileCategory.entries.toSet(),
    val minSize: Long = 0,
    val maxSize: Long = Long.MAX_VALUE,
    val searchQuery: String = ""
) {
    /**
     * Check if a file item matches the filter criteria.
     */
    fun matches(fileItem: FileItem): Boolean {
        // Check hidden files
        if (!showHiddenFiles && fileItem.name.startsWith(".")) {
            return false
        }
        
        // Check file category
        if (fileItem.fileType?.category !in fileCategories) {
            return false
        }
        
        // Check size
        if (fileItem.size < minSize || fileItem.size > maxSize) {
            return false
        }
        
        // Check search query
        if (searchQuery.isNotBlank() && !fileItem.name.contains(searchQuery, ignoreCase = true)) {
            return false
        }
        
        return true
    }
    
    companion object {
        /**
         * Default filter options (show all files).
         */
        fun default() = FilterOptions()
    }
}
