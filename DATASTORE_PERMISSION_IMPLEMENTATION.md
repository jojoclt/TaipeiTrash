# DataStore Implementation for Permission Tracking

## Overview
Implemented DataStore Preferences to persist permission denial state across app launches, ensuring the "permanently denied" dialog works correctly even after the app is restarted.

## Files Created/Modified

### 1. **NEW: `PermissionPreferences.kt`**
Located at: `app/src/main/java/com/jojodev/taipeitrash/core/data/PermissionPreferences.kt`

**Purpose:** Manages persistent storage of permission denial state using DataStore Preferences.

**Features:**
- Stores whether permission was denied at least once
- Provides Flow-based reactive data access
- Supports clearing the denial flag when permission is granted

**Key Methods:**
```kotlin
val wasPermissionDenied: Flow<Boolean> // Reactive state
suspend fun setPermissionDenied(denied: Boolean) // Mark as denied
suspend fun clearPermissionDenied() // Clear when granted
```

### 2. **UPDATED: `PermissionViewModel.kt`**

**Changes:**
- Added `PermissionPreferences` instance
- Exposed `wasPermissionDenied` as StateFlow from DataStore
- Updated `setPermissionGranted()` to:
  - Persist denial to DataStore when permission is denied
  - Clear denial flag when permission is granted

**Key Code:**
```kotlin
val wasPermissionDenied = permissionPreferences.wasPermissionDenied.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    false
)

fun setPermissionGranted(granted: Boolean) {
    _permissionGranted.value = granted
    
    if (!granted) {
        viewModelScope.launch {
            permissionPreferences.setPermissionDenied(true)
        }
    } else {
        viewModelScope.launch {
            permissionPreferences.clearPermissionDenied()
        }
    }
}
```

### 3. **UPDATED: `App.kt`**

**Changes:**
- Collect `wasPermissionDenied` from PermissionViewModel
- Updated permission check logic to use persisted state

**Permission Flow Logic:**
```kotlin
val isPermanentlyDenied = wasPermissionDenied && 
    activity?.let { act ->
        !shouldShowRequestPermissionRationale(act, permissionViewModel.permission)
    } == true
```

## How It Works

### First App Launch (Permission Never Requested)
1. `wasPermissionDenied` = `false` (default)
2. User clicks location button → Permission dialog shows
3. User denies → `setPermissionDenied(true)` persists to DataStore

### Second App Launch (After Denial)
1. `wasPermissionDenied` = `true` (loaded from DataStore)
2. User clicks location button
3. Check: `wasPermissionDenied && !shouldShowRequestPermissionRationale`
4. If "Don't ask again" was checked → Shows settings dialog
5. Otherwise → Shows permission dialog again

### After Granting Permission
1. User grants permission → `clearPermissionDenied()` called
2. DataStore cleared, `wasPermissionDenied` = `false`
3. Permission button shows secondary color (blue)
4. Clicking button centers map on user location

## Permission States

| State | wasPermissionDenied | hasLocationPermission | Button Color | On Click |
|-------|---------------------|----------------------|--------------|----------|
| Never asked | false | null | Red | Request permission |
| Just denied | true | false | Red | Request permission |
| Permanently denied | true | false | Red | Show settings dialog |
| Granted | false | true | Secondary (blue) | Center to location |

## Data Persistence

**Storage:** DataStore Preferences (survives app restarts and process death)
**File Location:** `/data/data/com.jojodev.taipeitrash/files/datastore/permission_preferences.preferences_pb`
**Key:** `was_permission_denied` (Boolean)

## Benefits

✅ **Persists across app launches** - DataStore survives app restarts  
✅ **Automatic cleanup** - Denial flag cleared when permission is granted  
✅ **Reactive** - UI updates automatically when state changes  
✅ **Type-safe** - Using Kotlin Flows and StateFlows  
✅ **Efficient** - DataStore reads cached in StateFlow with 5-second timeout  

## Testing Scenarios

### Test 1: First Time User
1. Launch app → No permission request
2. Click location button → System permission dialog
3. Deny → Button stays red
4. Close and reopen app
5. Click location button → System permission dialog (not settings dialog)

### Test 2: Permanently Denied
1. Launch app
2. Click location button → Deny + check "Don't ask again"
3. Click again → Custom settings dialog appears
4. Click "Open Settings" → App settings page opens
5. Grant permission → Return to app
6. Button turns blue → Clicking centers to location

### Test 3: Grant Then Revoke
1. Grant permission → Button blue
2. Go to settings → Revoke permission
3. Return to app → Button red
4. Click button → Permission dialog (not settings dialog)
5. Grant again → DataStore cleared, button blue

## Dependencies

Already included in `build.gradle.kts`:
```kotlin
implementation(libs.androidx.datastore.preferences)
```

No additional dependencies needed!

