<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Minimalist Calculator app icon" width="96" height="96">
</p>

<h1 align="center">Minimalist Calculator</h1>

<p align="center">
  A native Android calculator with a hidden, encrypted photo and video vault.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white">
  <img alt="Language" src="https://img.shields.io/badge/language-Java-007396?style=for-the-badge&logo=openjdk&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue?style=for-the-badge">
  <img alt="Dependencies" src="https://img.shields.io/badge/dependencies-none-lightgrey?style=for-the-badge">
</p>

## Overview

Minimalist Calculator is a clean native Android app built from scratch in Java. On the surface, it behaves like a simple iPhone-inspired calculator. Behind the calculator interface, it includes a private media vault that unlocks only when the user enters a custom sequence of calculator button taps.

The project focuses on three priorities:

- **Simplicity:** A lightweight native implementation with no third-party app dependencies.
- **Privacy:** User-selected media is encrypted locally before being stored.
- **Discretion:** The vault is accessed through a calculator tap sequence instead of a visible login screen.

## Key features

- **Minimal calculator UI:** Dark, calculator-first interface inspired by the iPhone calculator layout.
- **Hidden tap-sequence unlock:** The vault opens only after the correct private calculator button sequence is entered.
- **First-launch setup:** Users create and confirm their tap sequence the first time they open the app.
- **No reset from calculator screen:** The passcode can only be changed from inside the unlocked vault.
- **Encrypted vault storage:** Imported images and videos are encrypted before being stored in app-private storage.
- **Android Keystore integration:** The media encryption key is generated and protected through Android Keystore.
- **No broad storage permission:** Imports use Android's document picker rather than all-files or external-storage access.
- **Screenshot protection:** `FLAG_SECURE` is enabled to reduce screenshots and recent-app previews where supported.
- **Backup exclusion:** Android backup and data extraction rules exclude app data where supported.
- **Small footprint:** Native Java + Android SDK only.

## How it works

```text
Launch app
   |
   v
Calculator interface
   |
   +-- Normal calculator input
   |
   +-- Correct hidden tap sequence
          |
          v
      Encrypted media vault
          |
          +-- Import photos/videos
          +-- Preview vault items
          +-- Delete vault items
          +-- Change passcode
```

## Security model

This app is designed to reduce casual access to private media on a normal Android device.

Implemented protections:

- **AES-GCM encryption at rest:** Vault media is encrypted before being written to disk.
- **Android Keystore:** The encryption key is generated through Android Keystore instead of being hardcoded.
- **Private app sandbox:** Encrypted vault files are stored in internal app-private storage.
- **No broad file permissions:** Media selection is handled through the Android system picker.
- **Backup disabled:** Backup and transfer extraction rules are configured to exclude app data.
- **Secure window mode:** Screenshots and recent-app thumbnails are blocked where Android supports it.

## Security limitations

No mobile app can honestly guarantee absolute protection in every scenario.

Important limitations:

- **Rooted or compromised devices:** Root access, malware, or a compromised OS can weaken Android sandbox protections.
- **Unlocked-device access:** Anyone with the unlocked device and the correct tap sequence can access the vault.
- **No passcode recovery:** Forgetting the tap sequence requires clearing app data, which also removes vault media.
- **Preview cache:** Media may be temporarily decrypted into app cache for preview.
- **Manufacturer differences:** Keystore behavior and screenshot blocking can vary across Android versions and OEMs.

## Tech stack

| Area | Technology |
| --- | --- |
| App platform | Native Android |
| Language | Java |
| Build system | Gradle |
| Android Gradle Plugin | 8.5.2 |
| Compile SDK | 35 |
| Minimum SDK | 23 |
| Dependencies | Android SDK only |
| Encryption | AES-GCM |
| Key storage | Android Keystore |

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
│           ├── drawable/
│           ├── mipmap-*/
│           ├── mipmap-anydpi-v26/
│           ├── values/
│           └── xml/
├── gradle/
├── logo/
├── LICENSE
├── PRIVACY_POLICY.md
├── README.md
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## Build locally

### Android Studio

1. Clone the repository.
2. Open the project folder in Android Studio.
3. Let Gradle sync.
4. Run the `app` module on an emulator or physical Android device.

### Command line

```bash
./gradlew :app:assembleDebug
```

The generated debug APK will be available at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To generate a release Android App Bundle:

```bash
./gradlew :app:bundleRelease
```

The generated release bundle will be available at:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Google Play notes

The Play Store package name is configured as:

```text
com.socialsure.calculator
```

Before publishing a release:

- **Use a signed release bundle:** Upload a signed `.aab`, not a debug APK.
- **Protect signing keys:** Never commit keystores or signing passwords.
- **Test on real devices:** Verify calculator behavior, vault unlock, import, preview, delete, and passcode change flows.
- **Use accurate store copy:** Describe the app as an on-device privacy tool and avoid absolute security claims.
- **Complete Data safety carefully:** The app handles selected media locally and does not upload media to a backend service.

## Privacy policy

The privacy policy is available in this repository:

```text
PRIVACY_POLICY.md
```

## Repository hygiene

The repository excludes local, generated, and sensitive files such as:

- Gradle build outputs
- Android Studio workspace state
- `local.properties`
- APK/AAB artifacts
- Keystore files
- Signing credentials

## Author

Built from scratch by **Shayan Mohd**.

## License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
