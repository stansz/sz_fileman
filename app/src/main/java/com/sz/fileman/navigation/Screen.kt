package com.sz.fileman.navigation

/**
 * Sealed class representing all screens in the app.
 */
sealed class Screen(val route: String) {
    
    // Main screens
    data object LocalFiles : Screen("local_files")
    data object NasFiles : Screen("nas_files")
    data object NasConnections : Screen("nas_connections")
    data object NasConnectionEdit : Screen("nas_connection_edit/{connectionId}") {
        fun createRoute(connectionId: String = "new") = "nas_connection_edit/$connectionId"
    }
    
    // Preview screens
    data object ImagePreview : Screen("image_preview/{filePath}") {
        fun createRoute(filePath: String) = "image_preview/${filePath.encode()}"
    }
    data object VideoPreview : Screen("video_preview/{filePath}") {
        fun createRoute(filePath: String) = "video_preview/${filePath.encode()}"
    }
    data object DocumentPreview : Screen("document_preview/{filePath}") {
        fun createRoute(filePath: String) = "document_preview/${filePath.encode()}"
    }
    
    // Authentication
    data object BiometricAuth : Screen("biometric_auth")
    
    companion object {
        /**
         * Encode a string for use in route parameters.
         */
        private fun String.encode(): String {
            return java.net.URLEncoder.encode(this, "UTF-8")
        }
        
        /**
         * Decode a string from route parameters.
         */
        fun String.decode(): String {
            return java.net.URLDecoder.decode(this, "UTF-8")
        }
    }
}
