# Build Instructions for Android SOS Emergency App

This document provides proven build steps to successfully compile the Android SOS Emergency App from source code.

## Prerequisites

### Required Software
- **Java Development Kit (JDK) 17+**: Required for Android development
- **Android SDK**: API 34 and build-tools for Android development
- **Git**: For source code management

### System Requirements
- **Operating System**: Linux (Ubuntu/Debian preferred), macOS, or Windows
- **RAM**: Minimum 8GB recommended for Android builds
- **Storage**: At least 10GB free space for SDK and build artifacts

## Environment Setup

### Step 1: Install Java Development Kit

#### Option A: Download Portable OpenJDK 17 (Recommended for CI/CD)
```bash
# Download and extract OpenJDK 17
curl -L -o openjdk17.tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8.1%2B1/OpenJDK17U-jdk_x64_linux_hotspot_17.0.8.1_1.tar.gz"
tar -xzf openjdk17.tar.gz

# Set JAVA_HOME and PATH
export JAVA_HOME=/path/to/jdk-17.0.8.1+1
export PATH=$JAVA_HOME/bin:$PATH

# Verify installation
java -version
```

#### Option B: System Package Manager
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install -y openjdk-17-jdk

# macOS with Homebrew
brew install openjdk@17
```

### Step 2: Setup Gradle Wrapper

The project includes Gradle wrapper files. If missing, create them:

```bash
# Download gradle wrapper script (if not present)
curl -L -o gradlew "https://github.com/gradle/gradle/raw/v8.1.0/gradlew"
chmod +x gradlew

# Create gradle wrapper properties
mkdir -p gradle/wrapper
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.1-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# Download gradle wrapper jar
curl -L -o gradle/wrapper/gradle-wrapper.jar "https://github.com/gradle/gradle/raw/v8.1.0/gradle/wrapper/gradle-wrapper.jar"
```

### Step 3: Install Android SDK

#### Option A: Download Command Line Tools (Recommended)
```bash
# Create SDK directory
mkdir -p android-sdk && cd android-sdk

# Download Android command line tools
curl -L -o commandlinetools.zip "https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"

# Extract using Python (if unzip not available)
python3 -c "
import zipfile
with zipfile.ZipFile('commandlinetools.zip', 'r') as zip_ref:
    zip_ref.extractall('.')
"

# Setup directory structure
mkdir -p cmdline-tools/latest
cp -r cmdline-tools/* cmdline-tools/latest/

# Set environment variables
export ANDROID_HOME=/path/to/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
```

#### Option B: Use Existing Android Studio SDK
```bash
# If you have Android Studio installed
export ANDROID_HOME=$HOME/Android/Sdk  # Linux/macOS
export PATH=$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$PATH
```

### Step 4: Install Required SDK Components
```bash
# Accept licenses and install required components
echo y | sdkmanager "platforms;android-34" "build-tools;34.0.0"

# Alternative: Accept all licenses at once
yes | sdkmanager --licenses
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### Step 5: Create Local Properties File
```bash
# Create local.properties in project root
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

## Build Commands

### Environment Setup (Run before each build)
```bash
# Set all required environment variables
export JAVA_HOME=/path/to/jdk-17.0.8.1+1
export ANDROID_HOME=/path/to/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
```

### Build Debug APK
```bash
# Clean build (recommended)
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Output location: app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK
```bash
# Build release APK (unsigned)
./gradlew assembleRelease

# Output location: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Other Useful Commands
```bash
# Check Gradle and environment
./gradlew --version

# List all available tasks
./gradlew tasks

# Build with verbose output
./gradlew assembleDebug --info

# Build with stack trace (for debugging)
./gradlew assembleDebug --stacktrace

# Clean all build artifacts
./gradlew clean
```

## Verification

### Check APK Generation
```bash
# Verify debug APK exists
ls -la app/build/outputs/apk/debug/app-debug.apk

# Check APK information (if aapt available)
$ANDROID_HOME/build-tools/34.0.0/aapt dump badging app/build/outputs/apk/debug/app-debug.apk
```

### Install APK on Device
```bash
# Install via ADB (if device connected)
$ANDROID_HOME/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
```

## Troubleshooting

### Common Issues and Solutions

#### Java Issues
```bash
# Error: "java: command not found"
# Solution: Install JDK and set JAVA_HOME
export JAVA_HOME=/path/to/jdk
export PATH=$JAVA_HOME/bin:$PATH

# Error: "Unsupported Java version"
# Solution: Use JDK 17 (recommended for Android development)
```

#### Android SDK Issues
```bash
# Error: "SDK location not found"
# Solution: Create local.properties file
echo "sdk.dir=/path/to/android-sdk" > local.properties

# Error: "Failed to find Build Tools revision"
# Solution: Install required build tools
sdkmanager "build-tools;34.0.0"

# Error: "License not accepted"
# Solution: Accept all licenses
yes | sdkmanager --licenses
```

#### Gradle Issues
```bash
# Error: "gradlew: Permission denied"
# Solution: Make gradlew executable
chmod +x gradlew

# Error: "Could not find gradle wrapper jar"
# Solution: Download gradle-wrapper.jar
curl -L -o gradle/wrapper/gradle-wrapper.jar "https://github.com/gradle/gradle/raw/v8.1.0/gradle/wrapper/gradle-wrapper.jar"

# Error: Build timeout or memory issues
# Solution: Increase memory allocation
export GRADLE_OPTS="-Xmx4g"
```

#### Network/Download Issues
```bash
# Error: "Failed to download dependencies"
# Solution: Check internet connection and try offline build
./gradlew assembleDebug --offline
```

## Build Script Example

For automated builds, create a `build.sh` script:

```bash
#!/bin/bash
set -e

# Setup environment
export JAVA_HOME=/path/to/jdk-17.0.8.1+1
export ANDROID_HOME=/path/to/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# Verify environment
echo "Java version:"
java -version
echo "Android SDK:"
ls $ANDROID_HOME

# Clean and build
./gradlew clean
./gradlew assembleDebug

# Verify output
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Build successful! APK location:"
    echo "$(pwd)/app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Build failed - APK not found"
    exit 1
fi
```

## Project Structure

Key files for building:
```
├── app/
│   ├── build.gradle           # App-level build configuration
│   ├── proguard-rules.pro     # ProGuard rules for release builds
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/              # Java source code
│       ├── res/               # Android resources
│       └── assets/            # App assets (emergency contacts JSON)
├── build.gradle               # Project-level build configuration
├── settings.gradle            # Project settings
├── gradle.properties          # Gradle configuration
├── local.properties           # SDK location (create this)
├── gradlew                    # Gradle wrapper script
└── gradle/wrapper/            # Gradle wrapper files
```

## Build Time

Expected build times:
- **First build**: 5-15 minutes (includes SDK component downloads)
- **Incremental builds**: 30 seconds - 2 minutes
- **Clean builds**: 1-5 minutes

## Output Files

After successful build:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **Build logs**: `build/` directory in each module

---

**Note**: This build process has been tested and verified to work. The Android SOS Emergency App is designed for offline use and includes emergency contact numbers for 70+ countries.