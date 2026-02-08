# Architecture - SZ FileMan

## High-Level Architecture

The application follows a **Clean Architecture** pattern with three main layers:
- **Presentation Layer**: UI components (Compose) and ViewModels
- **Domain Layer**: Business logic, use cases, and domain models
- **Data Layer**: Data sources (local files, NAS, secure storage)

## Package Structure

```
com.sz.fileman/
├── app/                           # Application-level components
│   ├── MainActivity.kt            # Single activity entry point
│   ├── FileManApplication.kt     # Hilt application class
│   └── navigation/                # Navigation graph setup
│       ├── Screen.kt             # Screen routes
│       └── NavGraph.kt            # Navigation graph
│
├── core/                          # Core utilities and base classes
│   ├── security/                 # Security components
│   │   ├── biometric/            # Biometric authentication
│   │   │   ├── BiometricAuthManager.kt
│   │   │   └── BiometricPromptWrapper.kt
│   │   ├── encryption/           # Encryption utilities
│   │   │   ├── CryptoUtils.kt
│   │   │   └── KeyManager.kt
│   │   └── storage/              # Secure storage
│   │       └── SecureStorage.kt
│   ├── common/                   # Shared utilities
│   │   ├── extensions/           # Kotlin extensions
│   │   ├── utils/                # Helper functions
│   │   └── constants/            # App constants
│   └── di/                       # Hilt dependency injection
│       ├── AppModule.kt
│       ├── ViewModelModule.kt
│       ├── RepositoryModule.kt
│       └── SecurityModule.kt
│
├── domain/                       # Domain layer (business logic)
│   ├── model/                    # Domain models
│   │   ├── FileItem.kt          # File/folder model
│   │   ├── NasConnection.kt     # NAS connection config
│   │   └── SortOption.kt        # Sort options
│   ├── repository/               # Repository interfaces
│   │   ├── FileRepository.kt
│   │   └── NasRepository.kt
│   └── usecase/                  # Use cases
│       ├── GetFilesUseCase.kt
│       ├── CopyFileUseCase.kt
│       ├── DeleteFileUseCase.kt
│       └── ConnectNasUseCase.kt
│
├── data/                         # Data layer
│   ├── local/                    # Local file operations
│   │   ├── LocalFileRepository.kt
│   │   └── FileOperations.kt
│   ├── nas/                      # NAS operations
│   │   ├── NasFileRepository.kt
│   │   ├── SmbClient.kt
│   │   └── NasDiscovery.kt
│   └── secure/                   # Secure storage implementation
│       └── SecureStorageImpl.kt
│
└── presentation/                  # UI layer
    ├── common/                   # Shared UI components
    │   ├── components/           # Reusable composables
    │   ├── theme/                # Material3 theme
    │   └── state/                # UI state classes
    ├── local/                    # Local file browser UI
    │   ├── LocalFilesScreen.kt
    │   └── LocalFilesViewModel.kt
    ├── nas/                      # NAS browser UI
    │   ├── NasFilesScreen.kt
    │   ├── NasFilesViewModel.kt
    │   ├── NasConnectionListScreen.kt
    │   └── NasConnectionEditScreen.kt
    ├── auth/                     # Authentication UI
    │   └── BiometricAuthScreen.kt
    └── preview/                  # Media preview UI
        ├── ImagePreviewScreen.kt
        ├── VideoPreviewScreen.kt
        └── DocumentPreviewScreen.kt
```

## Key Components

### Presentation Layer
- **Jetpack Compose**: Declarative UI toolkit
- **Material Design 3**: Design system with dynamic colors
- **ViewModel**: Manages UI state and business logic
- **Navigation**: Type-safe navigation with Compose Navigation

### Domain Layer
- **Use Cases**: Encapsulate single business operations
- **Repository Interfaces**: Abstract data sources
- **Domain Models**: Pure data classes without dependencies

### Data Layer
- **Repository Implementations**: Concrete implementations of repository interfaces
- **Data Sources**: Local files, NAS (SMB), secure storage
- **Mappers**: Convert between domain models and data models

### Security Layer
- **Biometric Authentication**: BiometricPrompt for secure app access
- **Encrypted Storage**: EncryptedSharedPreferences for credentials
- **Android Keystore**: Secure key storage for encryption

## Data Flow

```
User Action
    ↓
Compose UI
    ↓
ViewModel
    ↓
Use Case
    ↓
Repository Interface
    ↓
Repository Implementation
    ↓
Data Source (Local/NAS/Secure)
    ↓
Result
    ↓
Domain Model
    ↓
UI State Update
    ↓
Compose UI Re-render
```

## Navigation Structure

```
MainActivity
├── BiometricAuthScreen (if biometric enabled)
└── MainNavigation
    ├── LocalFilesScreen
    │   ├── Folder navigation
    │   ├── File operations
    │   └── MediaPreviewScreen
    └── NasFilesScreen
        ├── NasConnectionListScreen
        ├── NasConnectionEditScreen
        ├── Folder navigation
        ├── File operations
        └── MediaPreviewScreen
```

## Key Design Patterns

| Pattern | Usage |
|---------|-------|
| MVVM | Separates UI from business logic |
| Repository | Abstracts data sources |
| Dependency Injection | Hilt for loose coupling |
| Use Case | Encapsulates business operations |
| Strategy | Different file operation implementations |
| Factory | Creating different file types |
| Observer | State observation in ViewModels |

## Security Architecture

1. **App Launch**: Check biometric auth setting
2. **Authentication**: BiometricPrompt for secure access
3. **Credential Storage**: EncryptedSharedPreferences with Android Keystore
4. **Data Encryption**: AES-256 for sensitive data
5. **Session Management**: Timeout for inactivity

## Technology Stack Summary

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Repository |
| DI | Hilt |
| Navigation | Compose Navigation |
| SMB | JCIFS-NG |
| Biometric | BiometricPrompt |
| Secure Storage | EncryptedSharedPreferences |
| Image Loading | Coil |
| Logging | Timber |
