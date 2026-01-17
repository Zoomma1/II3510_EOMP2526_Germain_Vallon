# Party together!

## Description

This project consists in developing a collaborative music queue application inspired by existing karaoke and music room platforms such as KaraFun. The objective of the application is to allow multiple users to join a shared music room and collaboratively manage a playlist in real time.

A room is created by a host, who plays the music directly on their device. Other users can join the room as guests by entering a short code or scanning a QR code. Guests are able to search for music tracks and add them to the shared queue, but playback control remains exclusively under the responsibility of the host.

The application was developed for the Android platform using Kotlin and Jetpack Compose. Firebase services were chosen to handle user authentication and real-time data synchronization. The selected implementation corresponds to Option A, where audio playback is centralized on the host device.

## Development Team and process

This project was developed by Victor Germain and Angélique Vallon, all this project was pair codded using Code With Me from JetBrains on Android Studio so sadly there is no git commit history.
This project was developed as part of the course II3510 - EOMP2526 Mobile Application Development for Android at Isep.
## Features

- **Room Creation and Joining**: Hosts create rooms, guests join via code or QR scan.
- **Real-time Playlist Management**: Collaborative queue updates.
- **Music Search and Addition**: Guests can search and add tracks.
- **Host-Controlled Playback**: Only host manages playback.
- **Authentication**: Firebase Auth for user management.
- **QR Code Integration**: For easy room joining.

## Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Platform**: Android (min SDK 24, target SDK 36)
- **Backend**: Firebase (Authentication, Firestore for real-time data)
- **Build Tool**: Gradle with Kotlin DSL

## Important Libraries and APIs

- **Jetpack Compose BOM**: For UI components (Material3, Navigation, etc.)
- **Firebase BOM**: Includes Firebase Auth and Firestore
- **ExoPlayer (Media3)**: For audio playback on host device
- **Retrofit**: For HTTP requests (likely for music search API)
- **Kotlinx Serialization**: For JSON handling with Retrofit
- **ZXing**: For QR code generation
- **CameraX and ML Kit**: For QR code scanning via camera
- **DataStore**: For local preferences
- **OkHttp**: HTTP client
- **Navigation Compose**: For app navigation
- **Lifecycle**: For ViewModel and Compose integration

## App Structure

The app follows a modular architecture with the following main packages:

- **`data/`**: Handles data management
  - `local/`: Local data storage (DataStore)
  - `logic/`: Business logic
  - `model/`: Data models
  - `remote/`: Remote API calls
  - `repository/`: Data repositories (Firestore, API)

- **`ui/`**: User interface components
  - Screens and Composables for different app sections (e.g., room creation, playlist, search)

- **`player/`**: Music playback logic using ExoPlayer

- **`utils/`**: Utility classes and helpers

- **`MainActivity.kt`**: Main entry point of the app

## Installation and Setup

1. **Prerequisites**:
   - Android Studio (latest version recommended)
   - JDK 11
   - Android SDK with API 36

2. **Clone the Repository**:
   ```
   git clone https://github.com/Zoomma1/II3510_EOMP2526_Germain_Vallon
   cd II3510_EOMP2526_Germain_Vallon
   ```

3. **Firebase Setup**:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication and Firestore
   - Download `google-services.json` and place it in `app/` directory (already present in the project)

4. **Build the Project**:
   - Open in Android Studio
   - Sync Gradle files
   - Build and run on device/emulator

   Or via command line:
   ```
   ./gradlew build
   ./gradlew installDebug
   ```

## Usage

- **Host**: Create a room, share code/QR, control playback
- **Guest**: Join room, search music, add to queue

