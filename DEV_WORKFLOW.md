# EditEcho Development Workflow

This document explains how to work with both stable and development versions of EditEcho simultaneously.

## 🏗️ Build Variants

### Available Variants
- **stableDebug**: `com.editecho.debug` - Your current working version (debug)
- **stableRelease**: `com.editecho` - Production stable version
- **devDebug**: `com.editecho.dev.debug` - Development version (debug)
- **devRelease**: `com.editecho.dev` - Development version (release)

### App Names on Device
- **Edit Echo** - Stable version
- **Edit Echo DEV** - Development version

## 🔨 Building & Installing

### Quick Commands
```bash
# Build and install stable version (your current working app)
./gradlew installStableDebug

# Build and install development version (for testing refactors)
./gradlew installDevDebug

# Build both versions
./gradlew assembleStableDebug assembleDevDebug
```

### Using the Helper Script
```bash
# Run the interactive build script
./build_variants.sh
```

## 🌿 Git Workflow

### Branch Structure
- **main**: Stable working version (v0.1.0)
- **refactor/architecture-v2**: Development branch for refactoring

### Switching Between Versions
```bash
# Work on stable version
git checkout main
./gradlew installStableDebug

# Work on refactors
git checkout refactor/architecture-v2
./gradlew installDevDebug
```

### Safety Net
- Tagged backup: `v0.1.0-working`
- Both apps can be installed simultaneously
- Easy rollback: `git checkout main`

## 📱 Testing Strategy

1. **Keep stable version installed** - Your working app stays functional
2. **Install dev version alongside** - Test refactors without losing stable app
3. **Compare behavior** - Run both versions to ensure refactors work correctly
4. **Independent data** - Each version has separate app data due to different package IDs

## 🔄 Development Process

1. **Start refactoring**:
   ```bash
   git checkout refactor/architecture-v2
   # Make your changes
   ./gradlew installDevDebug
   ```

2. **Test changes**:
   - Use "Edit Echo DEV" app to test new features
   - Keep "Edit Echo" app as reference

3. **Commit progress**:
   ```bash
   git add .
   git commit -m "Refactor: description of changes"
   ```

4. **When ready to merge**:
   ```bash
   git checkout main
   git merge refactor/architecture-v2
   ```

## 🚨 Emergency Rollback

If something goes wrong:
```bash
# Return to last working version
git checkout main
git reset --hard v0.1.0-working
./gradlew installStableDebug
```

## 📋 Version Information

- **Stable**: v0.1.0 (versionCode: 1)
- **Dev**: v0.2.0-dev (versionCode: 1002)

The dev version has a higher version code to ensure proper updates during development. 