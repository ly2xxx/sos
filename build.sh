#!/bin/bash

# Android SOS Emergency App - Automated Build Script
# This script automates the complete build process from environment setup to APK generation

set -e  # Exit on any error

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
JAVA_VERSION="17.0.8.1+1"
GRADLE_VERSION="8.1"
ANDROID_API_LEVEL="34"
BUILD_TOOLS_VERSION="34.0.0"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Download file with progress
download_file() {
    local url="$1"
    local output="$2"
    local description="$3"
    
    log_info "Downloading $description..."
    if command_exists curl; then
        curl -L --progress-bar -o "$output" "$url"
    elif command_exists wget; then
        wget --progress=bar -O "$output" "$url"
    else
        log_error "Neither curl nor wget found. Please install one of them."
        exit 1
    fi
}

# Setup Java environment
setup_java() {
    log_step "Setting up Java Development Kit"
    
    # First try to use system Java if available
    if command_exists java; then
        local java_version_output=$(java -version 2>&1)
        if echo "$java_version_output" | grep -q "17\|18\|19\|20\|21"; then
            log_success "Using system Java"
            echo "$java_version_output" | head -n 1
            export JAVA_HOME=$(java -XshowSettings:properties 2>&1 | grep 'java.home' | sed 's/.*= //')
            return 0
        fi
    fi
    
    # Try to install via package manager for WSL2/Ubuntu
    if command_exists apt-get && [ -f /etc/debian_version ]; then
        log_info "Attempting to install OpenJDK via package manager..."
        if apt-get update >/dev/null 2>&1 && apt-get install -y openjdk-17-jdk >/dev/null 2>&1; then
            log_success "Java 17 installed via package manager"
            export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
            export PATH="$JAVA_HOME/bin:$PATH"
            java -version 2>&1 | head -n 1
            return 0
        fi
    fi
    
    # Fallback to manual installation
    local java_dir="$PROJECT_DIR/jdk-$JAVA_VERSION"
    local java_archive="openjdk17.tar.gz"
    
    if [ -d "$java_dir" ] && [ -x "$java_dir/bin/java" ]; then
        log_info "Java already installed at $java_dir"
        # Test if the binary works
        if "$java_dir/bin/java" -version >/dev/null 2>&1; then
            export JAVA_HOME="$java_dir"
            export PATH="$JAVA_HOME/bin:$PATH"
            log_success "Using local Java installation"
            return 0
        else
            log_warning "Local Java binary not working, removing..."
            rm -rf "$java_dir"
        fi
    fi
    
    log_info "Installing OpenJDK 17..."
    download_file \
        "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JAVA_VERSION}/OpenJDK17U-jdk_x64_linux_hotspot_${JAVA_VERSION/+/_}.tar.gz" \
        "$java_archive" \
        "OpenJDK 17"
    
    log_info "Extracting OpenJDK..."
    tar -xzf "$java_archive"
    rm "$java_archive"
    
    if [ ! -d "$java_dir" ]; then
        log_error "Java extraction failed"
        exit 1
    fi
    
    # Set Java environment
    export JAVA_HOME="$java_dir"
    export PATH="$JAVA_HOME/bin:$PATH"
    
    # Verify Java installation with better error handling
    if java -version >/dev/null 2>&1; then
        log_success "Java 17 configured successfully"
        java -version 2>&1 | head -n 1
    else
        log_error "Java verification failed - binary may not be compatible with this system"
        log_error "Consider installing Java manually: sudo apt-get install openjdk-17-jdk"
        exit 1
    fi
}

# Setup Gradle wrapper
setup_gradle() {
    log_step "Setting up Gradle Wrapper"
    
    # Check if gradlew exists and is executable
    if [ -x "$PROJECT_DIR/gradlew" ]; then
        log_info "Gradle wrapper already exists"
    else
        log_info "Installing Gradle wrapper..."
        
        # Download gradlew script
        download_file \
            "https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradlew" \
            "gradlew" \
            "Gradle wrapper script"
        
        chmod +x gradlew
        
        # Create wrapper directory and properties
        mkdir -p gradle/wrapper
        
        cat > gradle/wrapper/gradle-wrapper.properties << EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
        
        # Download wrapper jar
        download_file \
            "https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar" \
            "gradle/wrapper/gradle-wrapper.jar" \
            "Gradle wrapper JAR"
    fi
    
    # Verify Gradle
    if ./gradlew --version >/dev/null 2>&1; then
        log_success "Gradle wrapper configured successfully"
        ./gradlew --version | head -n 5
    else
        log_error "Gradle wrapper verification failed"
        exit 1
    fi
}

# Setup Android SDK
setup_android_sdk() {
    log_step "Setting up Android SDK"
    
    local sdk_dir="$PROJECT_DIR/android-sdk"
    local cmdline_tools_zip="commandlinetools.zip"
    
    # Create SDK directory
    mkdir -p "$sdk_dir"
    cd "$sdk_dir"
    
    # Check if command line tools are installed
    if [ -d "cmdline-tools/latest/bin" ]; then
        log_info "Android command line tools already installed"
    else
        log_info "Installing Android command line tools..."
        download_file \
            "https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip" \
            "$cmdline_tools_zip" \
            "Android command line tools"
        
        # Extract using Python (more reliable than unzip)
        python3 -c "
import zipfile
with zipfile.ZipFile('$cmdline_tools_zip', 'r') as zip_ref:
    zip_ref.extractall('.')
"
        
        # Setup directory structure
        mkdir -p cmdline-tools/latest
        cp -r cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
        
        rm "$cmdline_tools_zip"
    fi
    
    cd "$PROJECT_DIR"
    
    # Set Android environment
    export ANDROID_HOME="$sdk_dir"
    export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
    
    # Create local.properties
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    
    log_success "Android SDK environment configured"
}

# Install Android SDK components
install_sdk_components() {
    log_step "Installing Android SDK Components"
    
    local platform_dir="$ANDROID_HOME/platforms/android-$ANDROID_API_LEVEL"
    local build_tools_dir="$ANDROID_HOME/build-tools/$BUILD_TOOLS_VERSION"
    
    # Check if components are already installed
    if [ -d "$platform_dir" ] && [ -f "$platform_dir/android.jar" ] && 
       [ -d "$build_tools_dir" ] && [ -f "$build_tools_dir/aapt" ]; then
        log_info "Android SDK components already installed"
        return
    fi
    
    log_info "Installing Android Platform $ANDROID_API_LEVEL and Build Tools $BUILD_TOOLS_VERSION..."
    log_warning "This may take 5-15 minutes depending on your internet connection"
    
    # Accept licenses and install components
    echo y | sdkmanager --licenses >/dev/null 2>&1 || true
    echo y | sdkmanager "platforms;android-$ANDROID_API_LEVEL" "build-tools;$BUILD_TOOLS_VERSION" || {
        log_error "Failed to install SDK components"
        exit 1
    }
    
    # Verify installation
    if [ -f "$platform_dir/android.jar" ]; then
        log_success "Android Platform $ANDROID_API_LEVEL installed successfully"
    else
        log_error "Platform installation verification failed"
        exit 1
    fi
    
    if [ -d "$build_tools_dir" ]; then
        log_success "Build Tools $BUILD_TOOLS_VERSION installed successfully"
    else
        log_error "Build Tools installation verification failed"
        exit 1
    fi
}

# Create missing resources
create_missing_resources() {
    log_step "Creating Missing Resources"
    
    # Create XML resources directory
    mkdir -p app/src/main/res/xml
    
    # Create data extraction rules
    if [ ! -f "app/src/main/res/xml/data_extraction_rules.xml" ]; then
        cat > app/src/main/res/xml/data_extraction_rules.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="."/>
        <exclude domain="sharedpref" path="device.xml"/>
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="."/>
        <exclude domain="sharedpref" path="device.xml"/>
    </device-transfer>
</data-extraction-rules>
EOF
        log_info "Created data_extraction_rules.xml"
    fi
    
    # Create backup rules
    if [ ! -f "app/src/main/res/xml/backup_rules.xml" ]; then
        cat > app/src/main/res/xml/backup_rules.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="."/>
    <exclude domain="sharedpref" path="device.xml"/>
</full-backup-content>
EOF
        log_info "Created backup_rules.xml"
    fi
    
    # Create launcher icon
    if [ ! -f "app/src/main/res/drawable/ic_launcher.xml" ]; then
        cat > app/src/main/res/drawable/ic_launcher.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="@android:color/white">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,19h-2v-6h2v6zM13,11h-2L11,7h2v4z"/>
</vector>
EOF
        log_info "Created launcher icon"
    fi
    
    # Update AndroidManifest.xml if needed
    if grep -q "@mipmap/ic_launcher" app/src/main/AndroidManifest.xml 2>/dev/null; then
        sed -i 's/@mipmap\/ic_launcher/@drawable\/ic_launcher/g' app/src/main/AndroidManifest.xml
        log_info "Updated AndroidManifest.xml launcher icon reference"
    fi
    
    log_success "Resource files created/verified"
}

# Build APK
build_apk() {
    log_step "Building Android APK"
    
    local build_type="${1:-debug}"
    local start_time=$(date +%s)
    
    log_info "Starting $build_type build..."
    
    # Clean previous builds
    ./gradlew clean
    
    # Build APK
    if [ "$build_type" = "release" ]; then
        ./gradlew assembleRelease
        local apk_path="app/build/outputs/apk/release/app-release-unsigned.apk"
    else
        ./gradlew assembleDebug
        local apk_path="app/build/outputs/apk/debug/app-debug.apk"
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    # Verify APK was created
    if [ -f "$apk_path" ]; then
        local file_size=$(ls -lh "$apk_path" | awk '{print $5}')
        log_success "APK built successfully in ${duration}s"
        echo
        echo "📱 APK Details:"
        echo "   Path: $PROJECT_DIR/$apk_path"
        echo "   Size: $file_size"
        echo "   Type: $build_type"
        echo "   Generated: $(date)"
        echo
        
        # Additional APK info if aapt is available
        local aapt_path="$ANDROID_HOME/build-tools/$BUILD_TOOLS_VERSION/aapt"
        if [ -f "$aapt_path" ]; then
            echo "📋 APK Info:"
            "$aapt_path" dump badging "$apk_path" | grep -E "(package:|launchable-activity:)" || true
            echo
        fi
        
        return 0
    else
        log_error "APK build failed - file not found at $apk_path"
        return 1
    fi
}

# Cleanup function
cleanup() {
    log_info "Cleaning up temporary files..."
    rm -f *.tar.gz *.zip 2>/dev/null || true
}

# Main build function
main() {
    log_info "Android SOS Emergency App - Automated Build Script"
    log_info "Starting build process at $(date)"
    echo
    
    # Parse command line arguments
    local build_type="debug"
    local clean_build=false
    local skip_sdk=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --release)
                build_type="release"
                shift
                ;;
            --clean)
                clean_build=true
                shift
                ;;
            --skip-sdk)
                skip_sdk=true
                shift
                ;;
            --help|-h)
                echo "Usage: $0 [OPTIONS]"
                echo
                echo "Options:"
                echo "  --release     Build release APK (default: debug)"
                echo "  --clean       Clean all dependencies and rebuild"
                echo "  --skip-sdk    Skip SDK installation (use existing)"
                echo "  --help, -h    Show this help message"
                echo
                echo "Examples:"
                echo "  $0                    # Build debug APK"
                echo "  $0 --release          # Build release APK"
                echo "  $0 --clean --release  # Clean build release APK"
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                log_info "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Clean everything if requested
    if [ "$clean_build" = true ]; then
        log_step "Cleaning Previous Build Environment"
        rm -rf jdk-* android-sdk gradle .gradle build 2>/dev/null || true
        rm -f gradlew gradle-wrapper.* local.properties 2>/dev/null || true
        log_success "Clean completed"
    fi
    
    # Change to project directory
    cd "$PROJECT_DIR"
    
    # Setup build environment
    setup_java
    setup_gradle
    
    if [ "$skip_sdk" != true ]; then
        setup_android_sdk
        install_sdk_components
    else
        log_info "Skipping SDK installation (--skip-sdk enabled)"
        # Still need to set environment variables
        export ANDROID_HOME="$PROJECT_DIR/android-sdk"
        export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
    fi
    
    create_missing_resources
    
    # Build APK
    if build_apk "$build_type"; then
        cleanup
        log_success "🎉 Build completed successfully!"
        echo
        echo "Next steps:"
        echo "  1. Transfer the APK to your Android device"
        echo "  2. Enable 'Install from Unknown Sources' in device settings"
        echo "  3. Install and test the emergency functionality"
        echo "  4. Verify GPS permissions and emergency contacts work"
    else
        cleanup
        log_error "Build failed!"
        exit 1
    fi
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run main function with all arguments
main "$@"