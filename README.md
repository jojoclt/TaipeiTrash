# Taipei Trash 🗑️

An Android application that helps users locate garbage trucks and trash cans throughout Taipei City. The app displays real-time information about waste collection points on an interactive map, making it easier for residents and visitors to properly dispose of their trash.

## Features ✨

### Core Functionality
- **Interactive Map View**: Browse trash cans and garbage trucks locations across Taipei on a Google Maps interface
- **Real-Time Location Data**: Access up-to-date information about waste collection points
- **Dual Data Sources**:
  - 🗑️ **Trash Cans**: View permanent trash receptacle locations with details
  - 🚛 **Garbage Trucks**: See garbage truck routes with arrival/departure times
- **Smart Loading**: 
  - First launch shows a beautiful boarding screen with progress indicator
  - Subsequent launches load instantly from local database
- **Location Services**: View your current location on the map with my-location functionality
- **Color-Coded Markers**: 
  - Green markers for trash cans
  - Blue markers for garbage trucks

### User Experience
- **Detailed Information**: Tap any marker to see detailed information in a bottom sheet:
  - Address and district
  - Import date (last updated)
  - For trash cans: Remarks/description
  - For garbage trucks: Arrival and departure times
- **Tab Navigation**: Switch between viewing trash cans and garbage trucks
- **Zoom Controls**: Built-in zoom controls for easy map navigation
- **Material 3 Design**: Modern UI following Google's Material Design 3 guidelines
- **Smooth Animations**: Polished user experience with fluid transitions

## Tech Stack 🛠️

### Core Technologies
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 36
- **Architecture**: MVVM with Repository Pattern

### Key Libraries & Dependencies

#### UI & Design
- Jetpack Compose with Material 3
- Google Maps Compose (v6.2.1)
- Material Icons Extended

#### Networking & Data
- Retrofit 2.11.0 (REST API communication)
- Kotlin Serialization for JSON parsing
- OkHttp with logging interceptor

#### Local Storage
- Room Database for offline data persistence
- DataStore Preferences for app settings

#### Dependency Injection
- Dagger Hilt for dependency injection

#### Location Services
- Google Play Services Location (21.3.0)
- Coroutines Play Services integration

#### Reactive Programming
- Kotlin Coroutines
- Kotlin Flow for reactive streams

## Prerequisites 📋

Before you begin, ensure you have the following installed:

- **Android Studio**: Ladybug | 2024.2.2 or newer
- **JDK**: Java 21
- **Android SDK**: API level 36
- **Google Maps API Key**: Required for map functionality

## Installation & Setup 🚀

### 1. Clone the Repository

```bash
git clone https://github.com/jojoclt/TaipeiTrash.git
cd TaipeiTrash
```

### 2. Configure Google Maps API Key

The app requires a Google Maps API key to function properly.

#### Option A: Using secrets-gradle-plugin (Recommended)

1. Create a `local.properties` file in the project root (if it doesn't exist)
2. Add your Google Maps API key:

```properties
apiKey=YOUR_GOOGLE_MAPS_API_KEY_HERE
```

The `secrets-gradle-plugin` will automatically inject this into your `AndroidManifest.xml`.

#### Option B: Manual Configuration

If you prefer manual configuration, update the `AndroidManifest.xml`:

```xml
<meta-data 
    android:name="com.google.android.geo.API_KEY" 
    android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE"/>
```

### 3. Build the Project

Open the project in Android Studio and let Gradle sync. Then build:

```bash
./gradlew assembleDebug
```

Or use Android Studio's Build menu: **Build → Make Project**

### 4. Run the Application

- Connect an Android device or start an emulator
- Click the Run button in Android Studio, or use:

```bash
./gradlew installDebug
```

## Project Structure 📁

```
app/src/main/java/com/jojodev/taipeitrash/
├── core/
│   ├── data/              # Data models and preferences
│   ├── db/                # Room database setup
│   ├── di/                # Dependency injection modules
│   ├── helper/            # Utility functions
│   ├── model/             # Core interfaces (TrashModel)
│   └── presentation/      # Shared UI components
├── trashcan/              # Trash can feature module
│   ├── data/              # Data layer (repository, network, local)
│   └── TrashCanViewModel.kt
├── trashcar/              # Garbage truck feature module
│   ├── data/              # Data layer (repository, network, local)
│   └── TrashCarViewModel.kt
├── startup/               # App startup logic
│   └── StartupViewModel.kt
├── ui/
│   ├── components/        # Reusable UI components
│   └── theme/             # Material theme configuration
├── App.kt                 # Main app composable
├── MainActivity.kt        # Entry point activity
└── TaipeiTrashApplication.kt  # Application class
```

## Architecture 🏗️

The app follows a **multi-module architecture** with clear separation of concerns:

### Layers

1. **Presentation Layer**: Jetpack Compose UI with ViewModels
2. **Domain Layer**: Business logic and use cases (via repositories)
3. **Data Layer**: 
   - Network data sources (Retrofit)
   - Local data sources (Room)
   - Repository pattern for data abstraction

### Key Patterns

- **Repository Pattern**: Abstracts data sources
- **MVVM**: ViewModel manages UI state, View observes state changes
- **Dependency Injection**: Hilt provides dependencies
- **Single Source of Truth**: Room database as the primary data source
- **Offline-First**: Data persisted locally for instant app loads

### Data Flow

```
Network API → Repository → Room Database → ViewModel → UI (Compose)
                    ↓
              DataStore (Preferences)
```

## Usage Guide 📱

### First Launch
1. Open the app - you'll see a boarding screen with the app logo
2. The app loads trash can and garbage truck data from the network (shows progress)
3. Data is saved locally for future use

### Subsequent Launches
- App loads instantly from the local database
- No loading screen needed

### Using the Map
1. **View Locations**: The map shows your current location and nearby trash points
2. **Switch Views**: Use the tabs at the top to toggle between trash cans and garbage trucks
3. **Zoom In/Out**: Use pinch gestures or the zoom control buttons
4. **View Details**: Tap any marker to see detailed information in a bottom sheet
5. **Navigate**: Pan the map to explore different areas of Taipei

### Permissions
- **Location Permission**: Required to show your current location on the map
- **Internet Permission**: Required to fetch initial data and map tiles

## Configuration ⚙️

### Build Variants

- **Debug**: Includes debugging tools, uses debug keystore
  - Application ID: `com.jojodev.taipeitrash.debug`
- **Release**: Optimized with ProGuard/R8 minification
  - Application ID: `com.jojodev.taipeitrash`

### Version Information

- **Version Code**: 6
- **Version Name**: 1.0.3

## Development 👨‍💻

### Building for Different Environments

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Code Style

The project uses the official Kotlin code style. Configure Android Studio:
- Go to **Settings → Editor → Code Style → Kotlin**
- Select **Set from... → Kotlin style guide**

## Data Sources 📊

The app fetches data from Taipei City's open data APIs for:
- Public trash can locations
- Garbage truck routes and schedules

Data is cached locally using Room database for offline access and faster subsequent launches.

## Troubleshooting 🔧

### Common Issues

**Map not displaying**
- Verify your Google Maps API key is correctly configured
- Ensure the API key has Maps SDK for Android enabled
- Check that internet permission is granted

**Location not showing**
- Grant location permissions when prompted
- Enable location services on your device
- Ensure GPS is turned on

**Build errors**
- Clean and rebuild: `./gradlew clean assembleDebug`
- Invalidate caches: **File → Invalidate Caches / Restart** in Android Studio
- Ensure you're using JDK 21

**DataStore errors**
- Sync Gradle files
- Check that DataStore dependencies are correctly added

## Contributing 🤝

Contributions are welcome! If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code:
- Follows Kotlin coding conventions
- Includes appropriate tests
- Updates documentation as needed

## License 📄

This project's license information is not currently specified. Please contact the repository owner for licensing details.

## Acknowledgments 🙏

- Taipei City Government for providing open data APIs
- Google Maps Platform for mapping services
- The Android and Jetpack Compose communities

## Contact & Support 💬

For questions, issues, or suggestions:
- Open an issue on GitHub
- Check existing documentation in the `/docs` folder (various `.md` files)

---

**Made with ❤️ for Taipei City residents**
