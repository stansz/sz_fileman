package com.sz.fileman

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Main application class for SZ FileMan.
 * Sets up Hilt dependency injection and initializes logging.
 */
@HiltAndroidApp
class FileManApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("SZ FileMan application initialized")
    }
}
