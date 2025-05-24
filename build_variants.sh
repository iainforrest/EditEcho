#!/bin/bash

echo "🔨 EditEcho Build Variants Helper"
echo "================================="
echo ""
echo "Available build variants:"
echo "1. Stable Debug   (com.editecho.debug)"
echo "2. Stable Release (com.editecho)"
echo "3. Dev Debug      (com.editecho.dev.debug)"
echo "4. Dev Release    (com.editecho.dev)"
echo ""

read -p "Which variant would you like to build? (1-4): " choice

case $choice in
    1)
        echo "🔨 Building Stable Debug..."
        ./gradlew assembleStableDebug
        echo "📱 Installing Stable Debug..."
        ./gradlew installStableDebug
        ;;
    2)
        echo "🔨 Building Stable Release..."
        ./gradlew assembleStableRelease
        ;;
    3)
        echo "🔨 Building Dev Debug..."
        ./gradlew assembleDevDebug
        echo "📱 Installing Dev Debug..."
        ./gradlew installDevDebug
        ;;
    4)
        echo "🔨 Building Dev Release..."
        ./gradlew assembleDevRelease
        ;;
    *)
        echo "❌ Invalid choice"
        exit 1
        ;;
esac

echo "✅ Build complete!" 