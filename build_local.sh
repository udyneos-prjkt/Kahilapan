#!/bin/bash

# ============================================
# BUILD SCRIPT ANDROID UNTUK UBUNTU 24.04
# ============================================

set -e  # Menghentikan script jika ada error

# 1. Input variable (sesuai GitHub Actions)
BUILD_TYPE="${1:-debug}"  # Default ke 'debug' jika tidak ada argumen
echo ">>> Mode Build: $BUILD_TYPE"

# 2. Update sistem & Install dependencies dasar (Ubuntu 24)
echo ">>> Menginstall dependencies (git, unzip, wget)..."
sudo apt-get update
sudo apt-get install -y git unzip wget

# 3. Install JDK 17 (Temurin)
echo ">>> Menginstall JDK 17 (Temurin)..."
# Hapus OpenJDK lama jika ada (untuk mencegah konflik)
sudo apt-get remove -y openjdk-* || true
wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
sudo add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
sudo apt-get update
sudo apt-get install -y temurin-17-jdk
# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc

# 4. Setup Android SDK (Command Line Tools)
echo ">>> Menginstall Android SDK..."
ANDROID_HOME=$HOME/android-sdk
export ANDROID_HOME
echo "export ANDROID_HOME=$ANDROID_HOME" >> ~/.bashrc

mkdir -p $ANDROID_HOME/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O commandlinetools.zip
unzip commandlinetools.zip -d $ANDROID_HOME/cmdline-tools
mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest
rm commandlinetools.zip

export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools" >> ~/.bashrc

# 5. Setup Android SDK (Accept Licenses & Install Build Tools)
echo ">>> Menerima lisensi SDK & menginstall platform-tools..."
yes | sdkmanager --licenses > /dev/null 2>&1
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 6. Beri izin eksekusi script Gradle & Keystore
echo ">>> Memberi izin eksekusi pada script..."
chmod +x gradlew
chmod +x generate_keystore.sh

# 7. Generate Keystore (sesuai YAML Anda)
echo ">>> Menjalankan generate_keystore.sh..."
./generate_keystore.sh

# 8. Build APK
echo ">>> Memulai proses build APK..."
if [ "$BUILD_TYPE" == "debug" ]; then
    echo ">> Building Debug APK..."
    ./gradlew assembleDebug
else
    echo ">> Cleaning project..."
    ./gradlew clean
    echo ">> Building Release APK..."
    ./gradlew assembleRelease
fi

# 9. Output hasil build
if [ "$BUILD_TYPE" == "debug" ]; then
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
else
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
fi

echo "=========================================="
echo "✅ BUILD SELESAI!"
echo "📁 Lokasi APK: $APK_PATH"
echo "=========================================="