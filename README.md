# Minimalist Calculator

Minimalist Calculator is a lightweight native Android calculator with a hidden encrypted photo and video vault. The app looks and behaves like a simple calculator, while the private vault is unlocked only by entering a custom calculator-button tap sequence.

The project is built with plain Java and the Android SDK, with no third-party app dependencies.

## Highlights

- **Calculator-first interface:** A clean dark calculator UI inspired by the iPhone calculator layout.
- **Hidden vault unlock:** The vault opens only after the correct private tap sequence is entered on the calculator.
- **Initial setup flow:** Users create and confirm their tap sequence when the app is first opened.
- **No reset from calculator screen:** The passcode can only be changed from inside the unlocked vault.
- **Encrypted media vault:** Imported images and videos are encrypted before being stored.
- **Android Keystore integration:** The AES key is generated and protected through Android Keystore.
- **No broad storage permission:** Media import uses Android's document picker instead of requesting external storage access.
- **App-private storage:** Encrypted vault files are stored inside the app sandbox.
- **Backup disabled:** Android backup and device-transfer extraction rules are configured to exclude app data.
- **Screen capture protection:** `FLAG_SECURE` blocks screenshots and recent-app thumbnails where supported.
- **Lightweight implementation:** Native Android Java app with a small codebase and no heavy frameworks.

## Screens and flow

1. Open the app and use it like a normal calculator.
2. On first launch, create a private calculator-button tap sequence.
3. Re-enter the same sequence on the calculator screen to unlock the vault.
4. Import images or videos using Android's file picker.
5. Preview or delete encrypted vault items from inside the vault.
6. Change the passcode only from inside the unlocked vault.

## Security model

This app is designed to reduce casual access to private media on a normal Android device.

Security measures include:

- **AES-GCM encryption at rest:** Vault files are encrypted before being written to disk.
- **Android Keystore key management:** The media encryption key is generated in Android Keystore rather than hardcoded.
- **Sandboxed storage:** Encrypted vault files are stored in app-private internal storage.
- **No public media copies:** The app does not intentionally save vault files to public device folders.
- **No broad file permissions:** The app uses the system picker and does not request all-files or external-storage permissions.
- **Backup exclusion:** Backup and data extraction rules exclude app data from normal Android backup flows.
- **Secure window flag:** Screenshot and recent-app preview blocking is enabled where Android supports it.

## Security limitations

No mobile app can guarantee absolute protection in every scenario. Avoid claiming that this app is impossible to bypass.

Known limitations:

- **Rooted or compromised devices:** A rooted device, malware, or compromised OS can weaken app sandbox guarantees.
- **Unlocked-device risk:** Anyone with the unlocked device and the passcode sequence can access the vault.
- **No password recovery:** Forgetting the tap sequence requires clearing app data, which also deletes vault media.
- **Preview cache:** Media is temporarily decrypted into app cache for preview and cache files are cleaned by the app.
- **Device-specific behavior:** Keystore hardware backing and screenshot blocking vary by Android version and manufacturer.

## Tech stack

- **Language:** Java
- **Platform:** Native Android
- **Build system:** Gradle
- **Android Gradle Plugin:** 8.5.2
- **Compile SDK:** 35
- **Minimum SDK:** 23
- **Dependencies:** Android SDK only

## Project structure

```text
.
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/minimalist/calculator/
│       │   ├── MainActivity.java
│       │   ├── Crypto.java
│       │   └── PasscodeManager.java
│       └── res/
│           ├── mipmap-*/
│           ├── mipmap-anydpi-v26/
│           ├── values/
│           └── xml/
├── build.gradle
├── settings.gradle
└── README.md
```

## Build locally

### Android Studio

1. Clone the repository.
2. Open the project folder in Android Studio.
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or physical Android device.

### Command line

```bash
gradle :app:assembleDebug
```

The debug APK will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Preparing for Google Play

Before uploading to Google Play Console:

1. **Update package identity:** Confirm the `namespace` and `applicationId` are final.
2. **Set versioning:** Increment `versionCode` and set a production `versionName`.
3. **Create a release keystore:** Do not commit keystore files or passwords to GitHub.
4. **Build a release artifact:** Generate a signed Android App Bundle (`.aab`) from Android Studio.
5. **Test on real devices:** Verify calculator performance, vault import, preview, delete, and passcode change flows.
6. **Write accurate store copy:** Describe encryption and privacy protections carefully without claiming absolute security.
7. **Complete Data safety:** Disclose that imported media remains on-device inside the app and is not uploaded by this app.

## Recommended Play Store wording

Safe claims:

- Media added to the vault is encrypted before being stored by the app.
- The app uses Android Keystore for encryption key management.
- Vault data is stored in app-private storage.
- The app does not request broad external storage access.
- Backup exclusion and screen capture protection are enabled where supported.

Avoid claims such as:

- "Impossible to hack"
- "100% secure"
- "Forensic-proof"
- "Cannot be accessed on rooted devices"

## Repository hygiene

The `.gitignore` excludes local and generated files such as:

- Gradle build outputs
- Android Studio project state
- `local.properties`
- APK/AAB generated artifacts
- Keystore files

Do not commit:

- Signing keys
- Keystore passwords
- Play Console credentials
- Private test media

## License

Add your preferred license before publishing publicly. If unsure, use `MIT` for a permissive open-source license or keep the repository private until you decide.
