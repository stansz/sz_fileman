package com.sz.fileman.presentation.local

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sz.fileman.domain.model.FileItem
import com.sz.fileman.domain.model.FileType
import com.sz.fileman.domain.model.FileCategory
import timber.log.Timber

/**
 * Screen for browsing local files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFilesScreen(
    viewModel: LocalFilesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Files") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show sort options */ }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = { /* TODO: Toggle view mode */ }) {
                        Icon(Icons.Default.ViewModule, contentDescription = "View Mode")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Show create folder dialog */ }
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Current path display
            PathDisplay(
                path = currentPath,
                onNavigateToPath = { viewModel.navigateToDirectory(it) }
            )
            
            // File list
            when (val state = uiState) {
                is LocalFilesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is LocalFilesUiState.Success -> {
                    FileList(
                        files = state.files,
                        selectedFiles = selectedFiles,
                        onFileClick = { file ->
                            if (file.isDirectory) {
                                viewModel.navigateToDirectory(file.path)
                            } else {
                                // TODO: Open file preview
                                Timber.d("Open file: ${file.name}")
                            }
                        },
                        onFileLongClick = { file ->
                            viewModel.toggleFileSelection(file.id)
                        }
                    )
                }
                
                is LocalFilesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = { viewModel.loadFiles() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Display current file path with clickable segments.
 */
@Composable
private fun PathDisplay(
    path: String,
    onNavigateToPath: (String) -> Unit
) {
    val segments = path.split("/").filter { it.isNotEmpty() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        segments.forEachIndexed { index, segment ->
            Text(
                text = segment,
                style = MaterialTheme.typography.bodyMedium,
                color = if (index == segments.lastIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.clickable {
                    // Build path up to this segment
                    val newPath = segments.take(index + 1).joinToString("/")
                    onNavigateToPath(newPath)
                }
            )
            if (index < segments.lastIndex) {
                Text(
                    text = "/",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Display list of files.
 */
@Composable
private fun FileList(
    files: List<FileItem>,
    selectedFiles: Set<String>,
    onFileClick: (FileItem) -> Unit,
    onFileLongClick: (FileItem) -> Unit
) {
    if (files.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "This folder is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(files, key = { it.id }) { file ->
                FileItemRow(
                    file = file,
                    isSelected = file.id in selectedFiles,
                    onClick = { onFileClick(file) },
                    onLongClick = { onFileLongClick(file) }
                )
            }
        }
    }
}

/**
 * Display a single file item row.
 */
@Composable
private fun FileItemRow(
    file: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val icon = when {
        file.isDirectory -> Icons.Default.Folder
        file.fileType?.category == FileCategory.IMAGE -> Icons.Default.Image
        file.fileType?.category == FileCategory.VIDEO -> Icons.Default.VideoFile
        file.fileType?.category == FileCategory.AUDIO -> Icons.Default.AudioFile
        file.fileType?.category == FileCategory.DOCUMENT -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
    
    Surface(
        color = backgroundColor,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = icon,
                contentDescription = file.name,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(40.dp)
            )
            
            // File info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (!file.isDirectory) {
                    Text(
                        text = file.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
