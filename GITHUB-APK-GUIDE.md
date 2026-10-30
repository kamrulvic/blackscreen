# BlackScreen Lock: build APK on GitHub

1. On github.com create a NEW **private** repository, e.g. `blackscreen-lock-android` (do not select a README template).
2. Extract this ZIP. Upload **all contents** of the extracted folder to the repository root, including `.github/workflows/build-apk.yml` (hidden `.github` folder). Use **Add file > Upload files**, then **Commit changes** to `main`.
3. Go to **Actions**. If prompted, enable workflows. Select **Build Android APK** and click **Run workflow** (or wait for the automatic push build).
4. Wait until the run has a green check. Open that run. At the bottom under **Artifacts**, download **BlackScreenLock-Android-APK**.
5. Unzip the downloaded artifact, then copy `app-debug.apk` to your Android phone and install it. Android may ask you to allow installing from that file manager/browser.
6. Launch BlackScreen Lock, grant **Display over other apps** permission and press **Start floating lock**.

This builds a **debug-signed APK**, appropriate for personal testing, not a Google Play release. Never upload signing passwords or private keys to a public repository. The APK has NOT been tested on a real phone. It may not work over protected/system screens; media playback remains controlled by the media app. The app does not physically turn off the display.

If a build fails, open the failed Actions job and share the error log for correction.
