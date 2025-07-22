# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building and Running
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK  
- `./gradlew build` - Full build including tests
- `./gradlew clean` - Clean build artifacts
- `./gradlew installDebug` - Install debug APK to connected device

### Testing
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumentation tests on connected device
- `./gradlew testDebugUnitTest` - Run debug unit tests specifically

### Android Studio Integration
- Open project in Android Studio for development
- Use "Run 'app'" (Shift+F10) for device deployment
- Build → Make Project (Ctrl+F9) for compilation
- Build → Build Bundle(s)/APK(s) → Build APK(s) for APK generation

## Architecture Overview

This is an **offline-first Android emergency SOS app** that provides location-based emergency contact numbers with direct calling functionality.

### Core Components

**MainActivity (`MainActivity.java`)**:
- Central UI controller handling emergency button interactions
- Manages runtime permissions (location, phone calling)  
- Coordinates between LocationService, CountryDetector, and EmergencyContactsManager
- Implements LocationService.LocationListener for real-time location updates
- Updates UI with location info and emergency numbers based on detected country

**LocationService (`service/LocationService.java`)**:
- GPS and Network location provider management
- Extends Android Service with LocationListener interface
- Configurable update intervals (10s) and distance thresholds (50m)
- Fallback between GPS (accuracy) and Network (speed) providers
- Custom LocationListener interface for external callbacks

**CountryDetector (`util/CountryDetector.java`)**:
- Coordinate-to-country mapping using hardcoded boundary boxes
- Covers 70+ countries with simplified rectangular boundaries  
- Regional fallback system for unmapped areas
- Handles longitude wraparound for countries crossing 180° meridian

**EmergencyContactsManager (`util/EmergencyContactsManager.java`)**:
- JSON-based emergency contacts database from assets
- Flexible country name matching (spaces, underscores, case-insensitive)
- Regional fallback emergency numbers
- Default universal emergency number (112)

**EmergencyReceiver (`receiver/EmergencyReceiver.java`)**:
- Broadcast receiver for emergency actions (volume button triggers, etc.)

### Key Technical Details

- **Target SDK**: API 34 (Android 14)
- **Minimum SDK**: API 21 (Android 5.0+)  
- **Build System**: Gradle with Android Gradle Plugin 8.1.2
- **Database**: Local JSON file in assets (`emergency_contacts.json`)
- **Permissions**: Location (fine/coarse), phone calling, internet, network state
- **UI Framework**: Material Design with ConstraintLayout
- **Threading**: Background country detection with UI thread updates

### Emergency Numbers Database Structure
Located in `app/src/main/assets/emergency_contacts.json`:
```json
{
  "Country_Name": {
    "police": "xxx",
    "ambulance": "xxx", 
    "fire": "xxx",
    "general": "xxx"
  }
}
```

### Permissions Architecture
Essential permissions requested at runtime:
- `ACCESS_FINE_LOCATION` - GPS coordinate detection
- `ACCESS_COARSE_LOCATION` - Network location fallback  
- `CALL_PHONE` - Direct emergency calling via Intent.ACTION_CALL

### Location Detection Flow
1. LocationService requests GPS/Network updates
2. Coordinate received → CountryDetector maps to country
3. EmergencyContactsManager loads country-specific numbers
4. UI updates with emergency buttons showing local numbers
5. Tap button → Intent.ACTION_CALL with emergency number