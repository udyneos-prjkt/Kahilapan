#!/bin/bash

echo "📱 INSTALL RELEASE APK"
echo "======================"

# Cari APK terbaru
LATEST_APK=$(ls -t Kahilapan-*.apk 2>/dev/null | head -n1)

if [ -z "$LATEST_APK" ]; then
    echo "❌ Tidak ada APK ditemukan!"
    echo "Jalankan ./build_release.sh dulu"
    exit 1
fi

echo "APK: $LATEST_APK"
echo ""

