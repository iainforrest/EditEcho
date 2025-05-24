#!/bin/bash

echo "🚀 Setting up EditEcho development workflow..."

# Make sure we're in a clean state
echo "📋 Checking git status..."
git status

# Commit current working version if there are changes
echo "💾 Committing current working version..."
git add .
git commit -m "Working version v0.1.0 - before refactor setup" || echo "No changes to commit"

# Create a backup tag
echo "🏷️  Creating backup tag..."
git tag v0.1.0-working
echo "✅ Created tag: v0.1.0-working"

# Create and switch to development branch
echo "🌿 Creating development branch..."
git checkout -b refactor/architecture-v2

echo "✅ Development workflow setup complete!"
echo ""
echo "📱 Build variants available:"
echo "  • stableDebug    - Your working version (com.editecho.debug)"
echo "  • stableRelease  - Production stable (com.editecho)"
echo "  • devDebug       - Development version (com.editecho.dev.debug)"
echo "  • devRelease     - Development release (com.editecho.dev)"
echo ""
echo "🔨 Build commands:"
echo "  ./gradlew assembleStableDebug    # Build stable version"
echo "  ./gradlew assembleDevDebug       # Build dev version"
echo "  ./gradlew installStableDebug     # Install stable version"
echo "  ./gradlew installDevDebug        # Install dev version"
echo ""
echo "🔄 Git workflow:"
echo "  git checkout main                # Switch to stable version"
echo "  git checkout refactor/architecture-v2  # Switch to dev branch" 