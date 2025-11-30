Per-App Launcher - Full Source

This Android Studio project builds a launcher app that lists installed apps and attempts to run
a Magisk module wrapper to apply per-app spoofing.

Key files:
- MainActivity.java : app list + launch logic
- AppAdapter.java, AppEntry.java : UI adapter and model
- layouts : activity_main.xml, row_app.xml

Build instructions:
1. Open this project (PerAppLauncher folder) in Android Studio.
2. Let Gradle sync.
3. Build -> Build APK(s) -> Build APK
4. Install app on device and grant root when prompted.

Notes:
- The app tries to execute: su -c /data/adb/modules/perapp-spoofer/scripts/launchapp <package>
  Ensure your Magisk module path and script match this path.
- Root is required only to call the wrapper; if root is denied, the app falls back to normal launching.
- You can customize UI and add filtering/search as needed.
