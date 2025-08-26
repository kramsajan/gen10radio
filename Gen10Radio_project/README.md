# GEN10 Radio — Android App

This is a minimal Android app that plays your station and also offers a Web Player button.
The stream URL is currently set to **https://gen10radio.com** as provided.

> If this URL is a *website* and not a direct audio stream (mp3/aac/m3u8), the **Play** button may fail. Use **Open Web Player**, or update `AppConfig.kt` with the direct stream URL once you have it.

## Local build (Android Studio)
1. Open the project folder in Android Studio.
2. Connect an Android phone with Developer Mode, or start an emulator.
3. Click **Run ▶** — the app will install as a debug build.

## Build a Debug APK with GitHub Actions
1. Create a new GitHub repo and push this project.
2. Go to **Actions** and run **Android CI (Debug APK)**.
3. Download the artifact **Gen10Radio-debug-apk** → `app-debug.apk` — you can install this on Android.

## Release build
- To publish or distribute a signed release APK/AAB, set up a keystore and add `signingConfigs` in `app/build.gradle.kts`. Then build `assembleRelease`.

