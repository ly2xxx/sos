# Build Script Usage Examples

The `build.sh` script automates the entire Android SOS Emergency App build process.

## Quick Start

```bash
# Make script executable (one time only)
chmod +x build.sh

# Build debug APK (most common)
./build.sh

# Build release APK
./build.sh --release

# Clean build everything from scratch
./build.sh --clean --release
```

## Command Options

### Basic Usage
- `./build.sh` - Build debug APK with default settings
- `./build.sh --release` - Build release APK (unsigned)
- `./build.sh --help` - Show help and all available options

### Advanced Options
- `./build.sh --clean` - Remove all dependencies and rebuild from scratch
- `./build.sh --skip-sdk` - Skip Android SDK installation (use existing)
- `./build.sh --clean --release` - Clean build with release APK

## What the Script Does

### 1. Environment Setup
- ✅ Downloads and installs OpenJDK 17 (portable)
- ✅ Sets up Gradle wrapper (8.1)
- ✅ Downloads Android SDK command line tools
- ✅ Installs Android Platform 34 and Build Tools 34.0.0
- ✅ Creates `local.properties` with SDK path

### 2. Resource Management
- ✅ Creates missing XML resources (`data_extraction_rules.xml`, `backup_rules.xml`)
- ✅ Generates launcher icon drawable
- ✅ Updates AndroidManifest.xml references

### 3. Build Process
- ✅ Cleans previous build artifacts
- ✅ Compiles Java source code
- ✅ Processes Android resources
- ✅ Packages APK file
- ✅ Verifies successful build

## Output Locations

After successful build:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Build Times

- **First build**: 10-20 minutes (includes SDK downloads)
- **Incremental builds**: 1-3 minutes
- **Clean builds**: 3-8 minutes

## System Requirements

- **OS**: Linux, macOS, or WSL on Windows
- **RAM**: 4GB+ recommended (8GB+ for faster builds)
- **Storage**: 5GB+ free space for SDK and build artifacts
- **Network**: Internet connection for initial SDK downloads

## Troubleshooting

### Common Issues
```bash
# Permission denied
chmod +x build.sh

# Out of memory during build
export GRADLE_OPTS="-Xmx4g"
./build.sh

# Network issues during SDK download
./build.sh --skip-sdk  # Use existing SDK

# Complete clean rebuild
./build.sh --clean --release
```

### Build Logs
The script provides colored output:
- 🔵 **Blue**: Information messages
- 🟢 **Green**: Success messages  
- 🟡 **Yellow**: Warning messages
- 🔴 **Red**: Error messages

## Continuous Integration

For CI/CD pipelines:
```bash
#!/bin/bash
# CI build script
set -e

# Clone repository
git clone https://github.com/ly2xxx/sos.git
cd sos

# Build release APK
./build.sh --release

# Upload APK artifact
cp app/build/outputs/apk/release/app-release-unsigned.apk $ARTIFACTS_DIR/
```

## Development Workflow

```bash
# Initial setup (one time)
git clone <repository>
cd sos
./build.sh --clean

# Daily development
./build.sh                    # Quick debug build
./build.sh --release         # Release build for testing

# Before release
./build.sh --clean --release  # Clean release build
```

The script is fully self-contained and will download all necessary dependencies automatically.