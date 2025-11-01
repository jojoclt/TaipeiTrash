# ⚠️ IMPORTANT: After Pulling These Changes

## Step 1: Sync Gradle Dependencies

The changes include new dependencies that need to be downloaded. You **MUST** sync Gradle:

### In Android Studio:
1. Click **File → Sync Project with Gradle Files**
   OR
2. Click the **Sync Now** link in the notification bar
   OR
3. Click the elephant icon (🐘) in the toolbar

### From Terminal:
```bash
./gradlew --refresh-dependencies
```

## Step 2: Invalidate Caches (If Needed)

If you see errors about "sealed interface" or "unresolved reference", the IDE is caching old data:

1. **File → Invalidate Caches...**
2. Check **"Invalidate and Restart"**
3. Click **"Invalidate and Restart"**

## Step 3: Clean and Rebuild

```bash
./gradlew clean build
```

Or in Android Studio:
1. **Build → Clean Project**
2. **Build → Rebuild Project**

## Expected Errors Before Sync

You may see these errors until Gradle sync completes:

- ✗ "Unresolved reference 'datastore'" - Fixed after sync
- ✗ "A class can only extend a sealed class..." - Fixed after cache invalidation
- ✗ Material icons errors - Fixed after sync

## After Sync - Should Be Clean

All errors should disappear after:
1. ✅ Gradle sync
2. ✅ Cache invalidation (if needed)
3. ✅ Clean rebuild

## New Dependencies Added

```kotlin
// DataStore for preferences
implementation("androidx.datastore:datastore-preferences:1.1.1")

// Material Icons Extended for trash/truck icons
implementation("androidx.compose.material:material-icons-extended:1.7.6")
```

## Test Your Build

```bash
./gradlew assembleDebug
```

Should complete successfully with **BUILD SUCCESSFUL**.

## If Still Having Issues

1. Delete `.gradle` and `.idea` folders
2. Restart Android Studio
3. Let it re-index the project
4. Sync Gradle again

---

**Once sync is complete, refer to QUICK_START.md for testing instructions!**

