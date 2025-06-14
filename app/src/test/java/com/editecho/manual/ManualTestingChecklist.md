# Voice Engine 3.0 Settings Persistence - Manual Testing Checklist

## Overview
This checklist verifies that Voice Engine 3.0 settings persistence works correctly in the EditEcho app. All tests should be performed on a physical device or emulator.

## Prerequisites
- EditEcho app installed (dev build recommended for testing)
- Device with overlay permission granted
- Clean app state (clear app data before starting)

---

## Test Group 1: Default Values and First Launch

### Test 1.1: First Launch Defaults
**Objective:** Verify correct default values on first app launch

**Steps:**
1. Clear app data (Settings > Apps > EditEcho > Storage > Clear Data)
2. Launch EditEcho app
3. Tap notification to open overlay
4. Observe tone picker and polish slider

**Expected Results:**
- ✅ Tone picker shows "Neutral" selected
- ✅ Polish slider shows 50% position
- ✅ Micro-labels show "simple" ↔ "structured" (Neutral's labels)

**Status:** [ ] Pass [ ] Fail

**Notes:**
_Record any deviations from expected behavior_

---

## Test Group 2: Tone Selection and Persistence

### Test 2.1: Tone Selection Updates UI Immediately
**Objective:** Verify tone selection updates UI components immediately

**Steps:**
1. Open EditEcho overlay
2. Change tone from "Neutral" to "Casual"
3. Observe micro-labels on polish slider
4. Change tone to "Informative"
5. Observe micro-labels again

**Expected Results:**
- ✅ Tone picker immediately shows "Casual" selected
- ✅ Micro-labels change to "relaxed" ↔ "clear"
- ✅ Tone picker immediately shows "Informative" selected  
- ✅ Micro-labels change to "empathetic" ↔ "direct"

**Status:** [ ] Pass [ ] Fail

### Test 2.2: Tone Persistence Across Overlay Sessions
**Objective:** Verify tone selection persists when overlay is closed and reopened

**Steps:**
1. Open EditEcho overlay
2. Select "Thoughtful" tone
3. Close overlay (X button)
4. Reopen overlay via notification
5. Observe tone picker

**Expected Results:**
- ✅ Tone picker shows "Thoughtful" selected
- ✅ Micro-labels show "brief" ↔ "structured"

**Status:** [ ] Pass [ ] Fail

### Test 2.3: Tone Persistence Across App Restarts
**Objective:** Verify tone selection persists when app is killed and restarted

**Steps:**
1. Open EditEcho overlay
2. Select "Supportive" tone
3. Close overlay
4. Kill EditEcho app (Recent apps > Swipe away)
5. Launch EditEcho again
6. Open overlay via notification
7. Observe tone picker

**Expected Results:**
- ✅ Tone picker shows "Supportive" selected
- ✅ Micro-labels show "empathetic" ↔ "reassuring"

**Status:** [ ] Pass [ ] Fail

---

## Test Group 3: Polish Level and Persistence

### Test 3.1: Polish Slider Updates Immediately
**Objective:** Verify polish slider changes are reflected immediately

**Steps:**
1. Open EditEcho overlay
2. Move polish slider to 25%
3. Observe slider position
4. Move slider to 75%
5. Observe slider position

**Expected Results:**
- ✅ Slider immediately shows 25% position
- ✅ Slider immediately shows 75% position
- ✅ Slider movement is smooth and responsive

**Status:** [ ] Pass [ ] Fail

### Test 3.2: Polish Level Persistence Across Overlay Sessions
**Objective:** Verify polish level persists when overlay is closed and reopened

**Steps:**
1. Open EditEcho overlay
2. Set polish slider to 80%
3. Close overlay (X button)
4. Reopen overlay via notification
5. Observe polish slider position

**Expected Results:**
- ✅ Polish slider shows 80% position

**Status:** [ ] Pass [ ] Fail

### Test 3.3: Polish Level Persistence Across App Restarts
**Objective:** Verify polish level persists when app is killed and restarted

**Steps:**
1. Open EditEcho overlay
2. Set polish slider to 15%
3. Close overlay
4. Kill EditEcho app (Recent apps > Swipe away)
5. Launch EditEcho again
6. Open overlay via notification
7. Observe polish slider position

**Expected Results:**
- ✅ Polish slider shows 15% position

**Status:** [ ] Pass [ ] Fail

---

## Test Group 4: Combined Settings Persistence

### Test 4.1: Multiple Settings Changes Persist Together
**Objective:** Verify both tone and polish settings persist together

**Steps:**
1. Open EditEcho overlay
2. Select "Casual" tone
3. Set polish slider to 90%
4. Close overlay
5. Kill and restart app
6. Open overlay
7. Observe both settings

**Expected Results:**
- ✅ Tone picker shows "Casual" selected
- ✅ Polish slider shows 90% position
- ✅ Micro-labels show "relaxed" ↔ "clear"

**Status:** [ ] Pass [ ] Fail

### Test 4.2: Rapid Settings Changes Persist Correctly
**Objective:** Verify rapid changes don't cause persistence issues

**Steps:**
1. Open EditEcho overlay
2. Rapidly change tone: Neutral → Casual → Informative → Thoughtful → Supportive
3. Rapidly move polish slider: 50% → 10% → 90% → 30% → 70%
4. Wait 2 seconds
5. Close overlay
6. Reopen overlay
7. Observe final settings

**Expected Results:**
- ✅ Tone picker shows "Supportive" (last selected)
- ✅ Polish slider shows 70% (last position)
- ✅ Micro-labels show "empathetic" ↔ "reassuring"

**Status:** [ ] Pass [ ] Fail

---

## Test Group 5: Micro-Label Updates

### Test 5.1: Micro-Labels Update When Tone Changes
**Objective:** Verify micro-labels update correctly for each tone

**Steps:**
1. Open EditEcho overlay
2. Test each tone and record micro-labels:

| Tone | Expected Low Label | Expected High Label | Actual Low | Actual High | Pass/Fail |
|------|-------------------|-------------------|------------|-------------|-----------|
| Casual | relaxed | clear | | | |
| Neutral | simple | structured | | | |
| Informative | empathetic | direct | | | |
| Supportive | empathetic | reassuring | | | |
| Thoughtful | brief | structured | | | |

**Expected Results:**
- ✅ All micro-labels match expected values
- ✅ Labels update immediately when tone changes

**Status:** [ ] Pass [ ] Fail

---

## Test Group 6: Performance and Responsiveness

### Test 6.1: Settings Load Time
**Objective:** Verify settings load within acceptable time (< 50ms per PRD)

**Steps:**
1. Set tone to "Informative" and polish to 85%
2. Close overlay
3. Kill and restart app
4. Time how long it takes for overlay to show correct settings when opened

**Expected Results:**
- ✅ Overlay opens with correct settings immediately (< 50ms)
- ✅ No visible delay or "flicker" of default values

**Status:** [ ] Pass [ ] Fail

### Test 6.2: Settings Save Time
**Objective:** Verify settings save within acceptable time (< 100ms per PRD)

**Steps:**
1. Open EditEcho overlay
2. Change tone to "Casual"
3. Immediately close overlay (within 1 second)
4. Reopen overlay
5. Verify tone persisted

**Expected Results:**
- ✅ Tone change persisted despite quick close
- ✅ No data loss from rapid save/close

**Status:** [ ] Pass [ ] Fail

---

## Test Group 7: Error Scenarios and Recovery

### Test 7.1: Recovery from Invalid State
**Objective:** Verify app recovers gracefully from invalid settings

**Steps:**
1. Open EditEcho overlay
2. Set valid settings (e.g., "Casual", 75%)
3. Force-stop app during settings save (if possible)
4. Restart app
5. Open overlay

**Expected Results:**
- ✅ App opens without crashes
- ✅ Settings show either saved values or safe defaults
- ✅ No corrupted UI state

**Status:** [ ] Pass [ ] Fail

### Test 7.2: Device Restart Persistence
**Objective:** Verify settings persist across device restarts

**Steps:**
1. Set tone to "Thoughtful" and polish to 25%
2. Close overlay
3. Restart device
4. Launch EditEcho
5. Open overlay

**Expected Results:**
- ✅ Tone picker shows "Thoughtful"
- ✅ Polish slider shows 25%
- ✅ Micro-labels show "brief" ↔ "structured"

**Status:** [ ] Pass [ ] Fail

---

## Test Group 8: UI/UX Validation

### Test 8.1: Visual Feedback and Consistency
**Objective:** Verify UI provides clear visual feedback

**Steps:**
1. Open EditEcho overlay
2. Interact with tone picker and polish slider
3. Observe visual states and transitions

**Expected Results:**
- ✅ Selected tone is clearly highlighted
- ✅ Polish slider position is clearly visible
- ✅ Micro-labels are readable and positioned correctly
- ✅ No UI elements overlap or appear corrupted

**Status:** [ ] Pass [ ] Fail

### Test 8.2: Accessibility and Usability
**Objective:** Verify settings are accessible and easy to use

**Steps:**
1. Open EditEcho overlay
2. Test tone picker dropdown functionality
3. Test polish slider drag/touch interaction
4. Verify text readability

**Expected Results:**
- ✅ Tone dropdown opens and closes smoothly
- ✅ All tone options are visible and selectable
- ✅ Polish slider responds to touch/drag accurately
- ✅ All text is readable at normal viewing distance

**Status:** [ ] Pass [ ] Fail

---

## Test Summary

### Overall Results
- **Total Tests:** 16
- **Passed:** ___
- **Failed:** ___
- **Pass Rate:** ___%

### Critical Issues Found
_List any critical issues that prevent core functionality_

### Minor Issues Found
_List any minor issues that don't prevent functionality but should be addressed_

### Recommendations
_Provide recommendations for any issues found_

---

## Test Environment
- **Device:** _______________
- **Android Version:** _______________
- **EditEcho Version:** _______________
- **Test Date:** _______________
- **Tester:** _______________

## Sign-off
- **Developer:** _________________ Date: _______
- **QA:** _________________ Date: _______
- **Product Owner:** _________________ Date: _______ 