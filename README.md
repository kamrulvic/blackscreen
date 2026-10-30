# BlackScreen Lock - Native Android source

Android Studio project (Java, no third-party libraries). Open this folder in Android Studio, allow Gradle sync, then **Build > Build APK(s)**. The APK appears under `app/build/outputs/apk/debug/` when built. An APK is NOT included because the Android SDK/Gradle build tools are not installed in this environment.

## Usage
1. Install the generated APK on an Android device (Android 8.0+).
2. Open BlackScreen Lock and grant **Display over other apps**.
3. Tap **Start floating lock** and return to your podcast/video app.
4. Tap floating lock to show a black touch guard and clock. **Long press** UNLOCK to return.
5. Long press floating lock and tap **STOP APP**, or tap Stop in the ongoing notification.

## Important limitations
- The black overlay does **not** turn off the screen or bypass Android's power management.
- Audio/video playback depends on the other app's own policies, background playback permissions, and Android version. For example, YouTube may pause background playback depending on its subscription and settings.
- This overlay blocks ordinary touches within its covered area, but **cannot reliably block system navigation, notifications, hardware buttons, lock screen, emergency UI, or all gestures**.
- The overlay is visible only while the foreground service is running. Android may terminate it under resource restrictions; it is not guaranteed to persist indefinitely.
- Notification permission is requested on Android 13+. Foreground service is declared `specialUse` for Android 14+, which may be subject to app-store policy review. Distribution through Play Store may require additional compliance.
- No network, account, storage access, ads, analytics, accessibility service, device admin, or root permissions are used.
- Test on your own device before depending on it. You can always disable overlay permission via Android Settings > Apps > Special app access > Display over other apps.
