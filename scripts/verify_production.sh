#!/bin/bash
set -e

echo "Running lint..."
./gradlew lintRelease

echo "Checking for hardcoded secrets..."
if grep -r "AIza[a-zA-Z0-9_\\-]{35}" app-child/src/ app-parent/src/ core/src/ ; then
    echo "ERROR: Hardcoded API key found!"
    exit 1
fi
if [ -f "app-child/google-services.json" ] || [ -f "app-parent/google-services.json" ]; then
    echo "ERROR: google-services.json should not be in the repository."
    exit 1
fi

echo "Verifying ProGuard rules..."
if ! grep -q "ScreenshotRequest" core/security/proguard-rules.pro ; then
    echo "WARNING: Check if ProGuard rules for serialization are fully set."
fi

echo "Building release APKs..."
./gradlew assembleRelease

echo "Production verification complete!"
