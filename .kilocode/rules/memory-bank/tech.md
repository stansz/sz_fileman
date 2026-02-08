# Technology Stack - SZ FileMan

## Core Platform
- **Language**: Kotlin 2.x
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36 (Android 16 Baklava)
- **Compile SDK**: 36
- **Java Compatibility**: VERSION_11

## UI Framework
- **Toolkit**: Jetpack Compose (BOM 2025.01.00)
- **Design System**: Material Design 3 (Material3)
- **Theming**: Dynamic colors support
- **Icons**: Material Icons Extended

## Architecture Components
- **Architecture Pattern**: MVVM + Repository
- **Dependency Injection**: Hilt 2.55
- **Navigation**: Jetpack Navigation Compose
- **ViewModel**: Lifecycle ViewModel Compose

## Security Libraries
- **Biometric Auth**: androidx.biometric:biometric-ktx:1.4.0-alpha02
- **Encrypted Storage**: androidx.security:security-crypto:1.1.0-alpha06
- **Encryption**: Android Keystore System

## Network & NAS
- **SMB Protocol**: org.codelibs:jcifs-ng:2.1.10
- **Network Discovery**: NSD (Network Service Discovery) - Android native

## Storage & Data
- **Local Storage**: Storage Access Framework (SAF)
- **Preferences**: DataStore Preferences
- **File Operations**: Kotlin IO + Android File APIs

## Media & Preview
- **Image Loading**: Coil 3.0.4 (Compose integration)
- **Video Playback**: ExoPlayer (if needed) or system MediaPlayer
- **PDF Viewing**: AndroidPdfViewer or system intent

## Development Tools
- **Build System**: Gradle 8.11.1 with Kotlin DSL
- **Logging**: Timber 5.0.1
- **Coroutines**: Kotlinx Coroutines Android

## Testing
- **Unit Testing**: JUnit 4, MockK
- **UI Testing**: Espresso, Compose UI Test
- **Assertions**: Truth library

## Key Dependencies Summary
```kotlin
// Compose BOM - includes foundation, material3, ui, etc.
implementation(platform("androidx.compose:compose-bom:2025.01.00"))

// Hilt
implementation("com.google.dagger:hilt-android:2.55")
kapt("com.google.dagger:hilt-compiler:2.55")

// Biometric
implementation("androidx.biometric:biometric-ktx:1.4.0-alpha02")

// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// SMB
implementation("org.codelibs:jcifs-ng:2.1.10")

// Image Loading
implementation("io.coil-kt.coil3:coil-compose:3.0.4")

// Logging
implementation("com.jakewharton.timber:timber:5.0.1")
```

## Build Configuration
- **Minification**: Disabled for debug, enabled for release with ProGuard
- **Build Types**: debug, release
- **Kotlin DSL**: Used for all build scripts
- **Version Catalog**: libs.versions.toml for dependency management
