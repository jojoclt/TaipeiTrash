# City Default Location Feature - Complete ✅

## What Was Added

### City.getDefaultLocation() Function
Added a function to the `City` enum that returns the default coordinates for each city:

```kotlin
enum class City(val displayName: String) {
    TAIPEI("Taipei City"),
    HSINCHU("Hsinchu City");

    fun getDefaultLocation(): LatLng {
        return when (this) {
            TAIPEI -> LatLng(25.0330, 121.5654)  // Taipei Main Station
            HSINCHU -> LatLng(24.8138, 120.9675) // Hsinchu City Hall
        }
    }
}
```

### Default Locations
- **Taipei City**: 25.0330°N, 121.5654°E (Taipei Main Station)
- **Hsinchu City**: 24.8138°N, 120.9675°E (Hsinchu City Hall)

### UI Enhancement
Added a "Go to [City Name]" button in the zoom prompt that appears when zoom level is below 16:

```
┌───────────────────────────────────┐
│         🔍                        │
│   Zoom in to see markers          │
│   Pinch to zoom or double tap     │
│                                   │
│   [Go to Taipei City]             │
└───────────────────────────────────┘
```

## How It Works

### 1. Initial Map Load
- Camera starts at selected city's default location
- Zoom level: 17 (street level)

### 2. User Gets Lost
- If user pans to different location
- Zooms out too far (< zoom 16)
- Prompt appears with button

### 3. Click Button
- Animates camera to city center
- Zooms to level 17
- Duration: 1 second smooth animation

### 4. City Switching
- When user switches city in Settings
- Map automatically moves to new city's default location

## Use Cases

### Tourist/New User
```
Scenario: User from Japan opens the app
1. App shows Taipei by default
2. User is physically in Tokyo
3. Zoom prompt appears
4. Click "Go to Taipei City"
5. Map jumps to Taipei Main Station
6. User can explore trash collection points
```

### Demonstrate to Friend
```
Scenario: Show app capability to someone abroad
1. Friend is in USA
2. Open app → Shows Taipei
3. Click button → Instantly at Taipei
4. Show markers and features
5. Switch to Hsinchu in Settings
6. Button now says "Go to Hsinchu City"
```

### Lost in Map
```
Scenario: User accidentally panned too far
1. Exploring Taipei suburbs
2. Pan too far → Outside city
3. Zoom out to see where they are
4. Prompt appears
5. Click button → Back to city center
```

## Code Changes

### Files Modified
1. ✅ `core/model/City.kt` - Added `getDefaultLocation()`
2. ✅ `App.kt` - Updated camera initialization and zoom prompt

### Implementation Details

**Camera Initialization:**
```kotlin
val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(
        selectedCity.getDefaultLocation(), 
        17f
    )
}
```

**Button Action:**
```kotlin
Button(onClick = {
    scope.launch {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                selectedCity.getDefaultLocation(),
                17f
            ),
            durationMs = 1000
        )
    }
}) {
    Text("Go to ${selectedCity.displayName}")
}
```

## Testing

### Test Taipei
1. Open app (defaults to Taipei)
2. Zoom out below level 16
3. Prompt appears
4. Verify button says "Go to Taipei City"
5. Click button
6. Camera animates to Taipei Main Station (25.0330, 121.5654)

### Test Hsinchu
1. Open Settings → Select Hsinchu
2. Zoom out below level 16
3. Verify button says "Go to Hsinchu City"
4. Click button
5. Camera animates to Hsinchu City Hall (24.8138, 120.9675)

### Test City Switching
1. Start in Taipei
2. Zoom in to see markers
3. Switch to Hsinchu in Settings
4. Map automatically moves to Hsinchu
5. Zoom out → Button shows "Go to Hsinchu City"

## Benefits

✅ **User-Friendly**: Easy way to return to city center
✅ **Educational**: Tourists can explore app before visiting
✅ **Demo-Friendly**: Show app to anyone, anywhere
✅ **Context-Aware**: Button text changes with selected city
✅ **Smooth UX**: 1-second animation feels natural

## Edge Cases Handled

1. **User in Different Country**: Can still explore Taiwan cities
2. **Lost in Map**: Quick return to known location
3. **City Switching**: Automatic repositioning
4. **Zoom < 16**: Prompt appears with helpful button

## Future Enhancements

Potential improvements:
1. Add more cities with their default locations
2. Remember last known position per city
3. Add "Return to My Location" button when GPS is available
4. Show distance to city center in prompt

## Summary

Users can now:
- 🗺️ Easily find the city center when lost
- 🌍 Explore app from anywhere in the world
- 🔄 Quick return after zooming out
- 🏙️ Context-aware navigation per city

The feature is **complete**, **tested** (compilation), and **ready to use**! 🎉

