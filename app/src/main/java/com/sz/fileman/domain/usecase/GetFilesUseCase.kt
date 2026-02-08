package com.sz.fileman.domain.usecase

import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.model.FilterOptions
import com.sz.fileman.domain.model.SortOption
import com.sz.fileman.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting files from a directory with sorting and filtering.
 */
class GetFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    /**
     * Get files from a directory with sorting and filtering applied.
     * @param path The directory path
     * @param sortOption The sorting option to apply
     * @param filterOptions The filter options to apply
     * @return Flow emitting sorted and filtered list of FileItem
     */
    operator fun invoke(
        path: String,
        sortOption: SortOption = SortOption.NAME_ASC,
        filterOptions: FilterOptions = FilterOptions.default()
    ): Flow<List<FileItem>> {
        return fileRepository.getFiles(path).map { files ->
            files
                .filter { filterOptions.matches(it) }
                .sortedWith(getComparator(sortOption))
        }
    }
    
    /**
     * Get a comparator for sorting files based on the sort option.
     */
    private fun getComparator(sortOption: SortOption): Comparator<FileItem> {
        return when (sortOption) {
            SortOption.NAME_ASC -> compareBy { it.name.lowercase() }
            SortOption.NAME_DESC -> compareByDescending { it.name.lowercase() }
            SortOption.DATE_ASC -> compareBy { it.lastModified }
            SortOption.DATE_DESC -> compareByDescending { it.lastModified }
            SortOption.SIZE_ASC -> compareBy { it.size }
            SortOption.SIZE_DESC -> compareByDescending { it.size }
            SortOption.TYPE_ASC -> compareBy { it.extension.lowercase() }
            SortOption.TYPE_DESC -> compareByDescending { it.extension.lowercase() }
        }
    }
}
