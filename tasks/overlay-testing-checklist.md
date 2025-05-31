# EditEcho Overlay Testing Checklist

## Test Environment Setup
- [ ] Device/Emulator: _________________
- [ ] Android Version: _________________
- [ ] Build Variant: Dev Debug
- [ ] Date: _________________
- [ ] Tester: _________________

## Pre-Test Requirements
- [ ] Ensure SYSTEM_ALERT_WINDOW permission is granted
- [ ] Ensure microphone permission is granted
- [ ] Ensure notification permission is granted
- [ ] Clear app data/cache before testing

## 5.2 Functionality Testing

### Recording Functionality
- [ ] Tap record button starts recording
- [ ] Recording indicator shows active state
- [ ] Tap stop button stops recording
- [ ] Audio is captured correctly
- [ ] No crashes during recording

### Transcription Display and Updates
- [ ] Transcribed text appears after recording
- [ ] Text is displayed in the correct field
- [ ] Transcription is accurate
- [ ] Loading state shown during transcription
- [ ] Error handling for failed transcription

### Text Editing Capability
- [ ] Can tap on transcribed text field
- [ ] Keyboard appears for editing
- [ ] Can edit the transcribed text
- [ ] Changes are preserved
- [ ] Text field scrolls for long content

### Copy to Clipboard Functionality
- [ ] Copy button is visible and enabled
- [ ] Tapping copy button copies text
- [ ] Toast message confirms copy
- [ ] Can paste copied text in other apps
- [ ] Copy works with edited text

### Formality and Polish Sliders (VoiceSliders)
- [ ] Formality slider is visible
- [ ] Polish slider is visible
- [ ] Sliders show current values
- [ ] Can adjust formality level (0-100)
- [ ] Can adjust polish level (0-100)
- [ ] Values persist during session
- [ ] Text refinement updates with slider changes

### Settings Button
- [ ] Settings button is visible
- [ ] Settings button appears inactive/disabled
- [ ] Tapping settings button has no effect
- [ ] Visual state indicates it's not functional

### Close Button (X)
- [ ] X button is visible and accessible
- [ ] Tapping X closes the overlay
- [ ] Service stops properly
- [ ] No memory leaks after closing

## 5.3 Functional Requirements Testing (from PRD)

### FR1: Overlay Appearance
- [ ] Overlay appears when notification is tapped
- [ ] Height is approximately 250dp (keyboard-like)
- [ ] Positioned at bottom of screen
- [ ] Full width of screen
- [ ] Rounded top corners (16dp radius)

### FR2: System Integration
- [ ] Underlying app content resizes/pans up
- [ ] Overlay stays above system keyboard if shown
- [ ] Works without focused text field
- [ ] Proper behavior in landscape mode

### FR3: Touch Handling
- [ ] Touches outside overlay pass through
- [ ] Overlay doesn't dismiss on outside tap
- [ ] All buttons within overlay responsive
- [ ] No dead zones in overlay

### FR4: Permission Handling
- [ ] Shows notification if permission missing
- [ ] Notification leads to permission settings
- [ ] Service stops if permission denied
- [ ] Retry works after granting permission

## 5.4 User Story Testing

### Story 1: Quick Voice Input
- [ ] Open messaging app
- [ ] Tap EditEcho notification
- [ ] Overlay appears over keyboard area
- [ ] Record message
- [ ] See transcription
- [ ] Copy and paste into message field

### Story 2: Email Composition
- [ ] Open email app
- [ ] Start composing email
- [ ] Activate EditEcho overlay
- [ ] Record thoughts
- [ ] Adjust formality to professional
- [ ] Copy refined text to email

### Story 3: Social Media Post
- [ ] Open social media app
- [ ] Start creating post
- [ ] Use EditEcho overlay
- [ ] Record casual message
- [ ] Keep formality low
- [ ] Edit and copy text

## 5.5 Device/API Testing

### API Level Testing
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)

### Device Type Testing
- [ ] Phone (standard size)
- [ ] Phone (large/XL)
- [ ] Tablet
- [ ] Foldable (if available)

## 5.6 Orientation Testing
- [ ] Portrait mode functionality
- [ ] Landscape mode functionality
- [ ] Rotation during recording
- [ ] Rotation with overlay open
- [ ] Layout adjusts properly

## 5.7 Split-Screen Testing
- [ ] Open app in split-screen
- [ ] Activate overlay
- [ ] Test all functionality
- [ ] Switch focus between apps
- [ ] Resize split-screen

## 5.8 Host App Compatibility

### Messaging Apps
- [ ] WhatsApp
- [ ] Telegram
- [ ] SMS/Messages
- [ ] Discord

### Email Apps
- [ ] Gmail
- [ ] Outlook
- [ ] Default Email

### Browsers
- [ ] Chrome
- [ ] Firefox
- [ ] Edge

### Note-Taking Apps
- [ ] Google Keep
- [ ] OneNote
- [ ] Default Notes

### Social Media
- [ ] Twitter/X
- [ ] Facebook
- [ ] Instagram
- [ ] LinkedIn

## 5.9 Performance Testing

### Overlay Appearance Time
- [ ] Measures time from tap to visible
- [ ] Target: <500ms
- [ ] Actual: _______ms
- [ ] Smooth animation
- [ ] No jank/stutter

### Responsiveness
- [ ] Button taps respond immediately
- [ ] No lag in slider adjustments
- [ ] Smooth text field interactions
- [ ] Recording starts/stops quickly

## 5.10 Keyboard Height Testing

### Different Keyboards
- [ ] Gboard
- [ ] SwiftKey
- [ ] Samsung Keyboard
- [ ] Default AOSP Keyboard

### Overlay Height Consistency
- [ ] Maintains 250dp height
- [ ] Doesn't resize with keyboard
- [ ] Proper positioning above keyboard

## 5.11 Bug Tracking

### Known Issues
1. _________________________________
2. _________________________________
3. _________________________________
4. _________________________________
5. _________________________________

### Crash Reports
- [ ] No crashes during testing
- [ ] If crashed, note reproduction steps

## 5.12 Memory Leak Testing

### Service Lifecycle
- [ ] Start/stop service 10 times
- [ ] Check memory usage trend
- [ ] No increasing memory consumption
- [ ] Proper cleanup on destroy

### Long Session Testing
- [ ] Keep overlay open for 5 minutes
- [ ] Perform multiple recordings
- [ ] Monitor memory usage
- [ ] Check for leaks in profiler

## Post-Test Checklist
- [ ] All critical functionality working
- [ ] No blocking bugs found
- [ ] Performance meets requirements
- [ ] Document any issues found
- [ ] Update bug list in tasks file

## Sign-off
- [ ] Testing completed successfully
- [ ] Ready for next phase

Tester Signature: _________________
Date: _________________ 