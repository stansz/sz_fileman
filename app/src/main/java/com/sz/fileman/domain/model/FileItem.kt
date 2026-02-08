package com.sz.fileman.domain.model

import java.io.File

/**
 * Represents a file or folder in the file system.
 */
data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val fileType: FileType? = null
) {
    companion object {
        /**
         * Create a FileItem from a Java File object.
         */
        fun fromFile(file: File): FileItem {
            return FileItem(
                id = file.absolutePath,
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified(),
                fileType = if (file.isFile) FileType.fromExtension(file.extension) else null
            )
        }
    }
    
    /**
     * Get the file extension.
     */
    val extension: String
        get() = if (!isDirectory) {
            name.substringAfterLast('.', "")
        } else {
            ""
        }
    
    /**
     * Get formatted file size for display.
     */
    val formattedSize: String
        get() = formatFileSize(size)
    
    /**
     * Format file size to human-readable string.
     */
    private fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        val gb = mb / 1024.0
        return "%.1f GB".format(gb)
    }
}

/**
 * Represents the type of a file.
 */
enum class FileType(val displayName: String, val category: FileCategory) {
    // Images
    JPEG("JPEG Image", FileCategory.IMAGE),
    JPG("JPEG Image", FileCategory.IMAGE),
    PNG("PNG Image", FileCategory.IMAGE),
    GIF("GIF Image", FileCategory.IMAGE),
    WEBP("WebP Image", FileCategory.IMAGE),
    BMP("Bitmap Image", FileCategory.IMAGE),
    SVG("SVG Image", FileCategory.IMAGE),
    
    // Videos
    MP4("MP4 Video", FileCategory.VIDEO),
    AVI("AVI Video", FileCategory.VIDEO),
    MKV("MKV Video", FileCategory.VIDEO),
    MOV("MOV Video", FileCategory.VIDEO),
    WEBM("WebM Video", FileCategory.VIDEO),
    
    // Audio
    MP3("MP3 Audio", FileCategory.AUDIO),
    WAV("WAV Audio", FileCategory.AUDIO),
    OGG("OGG Audio", FileCategory.AUDIO),
    FLAC("FLAC Audio", FileCategory.AUDIO),
    AAC("AAC Audio", FileCategory.AUDIO),
    
    // Documents
    PDF("PDF Document", FileCategory.DOCUMENT),
    DOC("Word Document", FileCategory.DOCUMENT),
    DOCX("Word Document", FileCategory.DOCUMENT),
    XLS("Excel Spreadsheet", FileCategory.DOCUMENT),
    XLSX("Excel Spreadsheet", FileCategory.DOCUMENT),
    PPT("PowerPoint Presentation", FileCategory.DOCUMENT),
    PPTX("PowerPoint Presentation", FileCategory.DOCUMENT),
    TXT("Text File", FileCategory.DOCUMENT),
    RTF("Rich Text Format", FileCategory.DOCUMENT),
    ODT("OpenDocument Text", FileCategory.DOCUMENT),
    
    // Archives
    ZIP("ZIP Archive", FileCategory.ARCHIVE),
    RAR("RAR Archive", FileCategory.ARCHIVE),
    TAR("TAR Archive", FileCategory.ARCHIVE),
    GZ("GZIP Archive", FileCategory.ARCHIVE),
    SEVEN_Z("7-Zip Archive", FileCategory.ARCHIVE),
    
    // Code
    KT("Kotlin Source", FileCategory.CODE),
    JAVA("Java Source", FileCategory.CODE),
    XML("XML File", FileCategory.CODE),
    JSON("JSON File", FileCategory.CODE),
    HTML("HTML File", FileCategory.CODE),
    CSS("CSS File", FileCategory.CODE),
    JS("JavaScript File", FileCategory.CODE),
    
    // Other
    UNKNOWN("Unknown", FileCategory.OTHER);
    
    companion object {
        private val extensionMap = values().associateBy { it.name.lowercase() }
        
        /**
         * Get FileType from file extension.
         */
        fun fromExtension(extension: String): FileType {
            return extensionMap[extension.lowercase()] ?: UNKNOWN
        }
    }
}

/**
 * Represents the category of a file.
 */
enum class FileCategory {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    CODE,
    OTHER
}
