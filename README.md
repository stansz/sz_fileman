# SZ FileMan

<div align="center">

A modern, secure Android file manager with seamless NAS integration.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-blue?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green?logo=android)](https://www.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.01.00-purple?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

## 📖 About

**SZ FileMan** is a modern Android file manager designed to provide seamless access to both local device storage and network-attached storage (NAS) devices. Built with security, ease of use, and contemporary design in mind, it bridges the gap between local and network storage while maintaining a beautiful Material Design 3 interface.

### Key Features

#### 📁 Core File Management
- Browse, copy, move, delete, and rename files and folders
- Create new folders
- Multi-select operations for batch file management
- File search with advanced filtering
- Sort options: name, date, size, type
- List and grid view modes

#### 🌐 NAS Integration
- SMB/CIFS protocol support for Windows-compatible NAS devices
- Network discovery for easy NAS detection
- Save and manage multiple NAS connections
- Automatic reconnection handling

#### 🔒 Security Features
- Biometric authentication (fingerprint/face unlock)
- Encrypted storage of NAS credentials using Android Keystore
- Optional encryption for locally cached sensitive files
- Session timeout for enhanced security

#### 🎨 Media Preview
- Image viewer with zoom and pan capabilities
- Video player integration
- Audio file playback
- Document preview (PDF, text files)
- Thumbnail caching for improved performance

## 🚀 Screenshots

<!-- Add screenshots here when available -->
<!-- 
![Screenshot 1](screenshots/screenshot1.png)
![Screenshot 2](screenshots/screenshot2.png)
-->

## 📋 Requirements

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 16 (API 36)
- **Compile SDK**: Android 16 (API 36)
- **Kotlin**: 2.x
- **Java Compatibility**: VERSION_11

## 🛠️ Technology Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.x |
| **UI Framework** | Jetpack Compose (BOM 2025.01.00) |
| **Design System** | Material Design 3 |
| **Architecture** | MVVM + Repository Pattern |
| **Dependency Injection** | Hilt 2.55 |
| **Navigation** | Jetpack Navigation Compose |
| **Biometric Auth** | BiometricPrompt 1.4.0-alpha02 |
| **Secure Storage** | EncryptedSharedPreferences 1.1.0-alpha06 |
| **SMB Protocol** | JCIFS-NG 2.1.10 |
| **Image Loading** | Coil 3.0.4 |
| **Logging** | Timber 5.0.1 |

## 📦 Installation

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 36 installed

### Clone the Repository

```bash
git clone https://github.com/yourusername/sz_fileman.git
cd sz_fileman
```

### Build the Project

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Build the project:
   ```bash
   ./gradlew build
   ```

### Run the App

1. Connect an Android device or start an emulator
2. Run the app from Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## 🏗️ Architecture

SZ FileMan follows **Clean Architecture** principles with three main layers:

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                      │
│  (Compose UI, ViewModels, Navigation)                    │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  (Use Cases, Repository Interfaces, Domain Models)      │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
│  (Repository Implementations, Data Sources)             │
│  (Local Files, NAS/SMB, Secure Storage)                 │
└─────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.sz.fileman/
├── app/                           # Application-level components
│   ├── MainActivity.kt            # Single activity entry point
│   ├── FileManApplication.kt     # Hilt application class
│   └── navigation/                # Navigation graph setup
│
├── core/                          # Core utilities and base classes
│   ├── security/                 # Security components
│   │   ├── biometric/            # Biometric authentication
│   │   ├── encryption/           # Encryption utilities
│   │   └── storage/              # Secure storage
│   ├── common/                   # Shared utilities
│   └── di/                       # Hilt dependency injection
│
├── domain/                       # Domain layer (business logic)
│   ├── model/                    # Domain models
│   ├── repository/               # Repository interfaces
│   └── usecase/                  # Use cases
│
├── data/                         # Data layer
│   ├── local/                    # Local file operations
│   ├── nas/                      # NAS operations
│   └── secure/                   # Secure storage implementation
│
└── presentation/                  # UI layer
    ├── common/                   # Shared UI components
    ├── local/                    # Local file browser UI
    ├── nas/                      # NAS browser UI
    ├── auth/                     # Authentication UI
    └── preview/                  # Media preview UI
```

### Key Design Patterns

| Pattern | Usage |
|---------|-------|
| MVVM | Separates UI from business logic |
| Repository | Abstracts data sources |
| Dependency Injection | Hilt for loose coupling |
| Use Case | Encapsulates business operations |
| Strategy | Different file operation implementations |
| Factory | Creating different file types |
| Observer | State observation in ViewModels |

## 🔐 Security Architecture

1. **App Launch**: Check biometric auth setting
2. **Authentication**: BiometricPrompt for secure access
3. **Credential Storage**: EncryptedSharedPreferences with Android Keystore
4. **Data Encryption**: AES-256 for sensitive data
5. **Session Management**: Timeout for inactivity

## 📱 Usage

### Local File Management

1. Launch the app
2. Browse through local storage
3. Use the action menu to copy, move, delete, or rename files
4. Switch between list and grid views
5. Use search to find specific files

### NAS Connection

1. Navigate to the NAS tab
2. Tap "Add Connection"
3. Enter NAS details:
   - IP address or hostname
   - Share name
   - Username and password
4. Credentials are encrypted and stored securely
5. Browse and manage files on your NAS

### Biometric Authentication

1. Enable biometric authentication in settings
2. On app launch, authenticate using fingerprint or face unlock
3. Session automatically locks after inactivity timeout

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Hilt](https://dagger.dev/hilt/) - Dependency injection
- [JCIFS-NG](https://github.com/agroce/jcifs-ng) - SMB/CIFS implementation
- [Coil](https://coil-kt.github.io/coil/) - Image loading library

## 📞 Support

For issues, questions, or suggestions, please open an issue on GitHub.

## 🗺️ Roadmap

- [ ] Phase 1: Foundation & Dependencies ✅
- [ ] Phase 2: Security Layer Implementation
- [ ] Phase 3: Local File Management
- [ ] Phase 4: NAS Integration
- [ ] Phase 5: Media Preview
- [ ] Phase 6: Testing & Optimization
- [ ] Phase 7: Release

---

<div align="center">

**Built with ❤️ using Kotlin and Jetpack Compose**

</div>
