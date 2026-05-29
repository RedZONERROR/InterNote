# Inter Note App

An enterprise-grade, multi-platform design compliant, high-density privacy-focused note-taking application for Android. Fully offline-first and secure, **Inter Note App** is built with modern Jetpack Compose and encrypted with **SQLCipher** at the database layer (making it fully compliant with GDPR data privacy requirements).

---

## 🎨 Design Theme & Core UX

The application implements a custom **High Density Modern Theme** with the following distinctive characteristics:
- **High-Density Spacing**: Form factor layouts and padding are tightly integrated into an 8dp Material Design 3 grid, maximizing negative space and readability.
- **Visual Categorization Highlights**: Interactive folder and filter tabs matching custom curated pastel tokens.
- **Local-First Security Status Banner**: Real-time awareness of state security and GDPR SQLCipher database status.
- **Markdown-Rich Text Processing**: Fully integrated custom Markdown parser and transformation engine for formatted lists, highlights, code blocks, and formatting highlights on-the-fly.

---

## 🏗️ Technical Architecture & Stack

The mobile codebase strictly adheres to standard **MVVM (Model-View-ViewModel)** guidelines and **Clean Architecture** patterns:

*   **UI Layer**: 100% Declarative UI with **Jetpack Compose** and **Material 3 (M3)** using modern dynamic dark/light schemes and single-activity navigation keys.
*   **Local Database State**: **Room Database** layered with **SQLCipher (net.sqlcipher)** for hardware-accelerated 256-bit AES encryption on the database file.
*   **Security & Compliance Engine**: Encryption keys are derived safely and kept securely in sandboxed keystorage.
*   **Extensibility Widgets**: Includes dynamic home widgets (`NoteQuickCapture` & `NoteListWidget`) and a convenient Android **Quick Settings Pull-Down Tile** for lightning-fast note taking anytime.
*   **Verification & Test Suites**: Fully integrated unit testing, local JVM-level UI tests with **Robolectric**, and visual regressions through screenshot verification powered by **Roborazzi** with zero physical emulator dependencies.

---

## 💻 Tech Stack Specification

-   **Development Language**: 100% Kotlin
-   **Minimum SDK Supported**: SDK 23 (Android 6.0+)
-   **Compile & Target SDK**: SDK 36 (Android 15 Q / 16 Preview)
-   **Dependency Injection**: Constructor injection managed via explicit Factories.
-   **Asynchronous Engine**: Kotlin Coroutines & Flow (StateIn/CollectAsStateWithLifecycle)
-   **Key Dependencies**:
    -   `androidx.room` (+ KSP compilation)
    -   `net.sqlcipher` (Custom Room SQLite support layer)
    -   `androidx.navigation:navigation-compose`
    -   `io.coil-kt:coil-compose`

---

## 🛠️ Local Development & Quick Start

### 1. Requirements
Ensure your development environment meets the following specifications:
- **Android Studio Ladybug (or higher / IntelliJ IDEA)**
- **JDK 17** installed and configured as your Gradle JVM setting.

### 2. Environment Configurations (`.env`)
The project utilizes the **Secrets Gradle Plugin** to load key credentials from `.env` files safely into Gradle `BuildConfig`. Create a `.env` file at the project root matching the template below:

```bash
# Template for local environment configs (.env)
# Do NOT check actual secrets into version control.
DATABASE_PASSPHRASE="your_super_secure_sqlcipher_passphrase_here"
```

### 3. Build & Run Command Line
To compile and build the Android Debug package on JVM:
```bash
# Assemble debug package
gradle assembleDebug

# Run unit tests & Robolectric criteria
gradle :app:testDebugUnitTest

# Review visual screenshots via Roborazzi
gradle :app:verifyRoborazziDebug
```

---

## 🚀 GitHub Actions Continuous Integration (CI/CD)

The project includes an enterprise-grade automated CI/CD pipeline inside `.github/workflows/build-release.yml`. When you publish a version tag (e.g., `v1.1.0`), the workflow triggers automatically to:
1. Verify coding standards, linting rules, and compilation parameters.
2. Run unit & JVM UI (Robolectric) testing frameworks.
3. Decrypt and setup signing credentials.
4. Export and package production distribution APK and AAB (Android App Bundle).
5. Compile dynamic Release text notes and draft the formal output inside tag drafts under **GitHub Releases**.

### Setting Up CD Production Signing Secrets
To activate signing on production releases automatically, upload the following secrets inside your GitHub Repository setting under `Settings > Secrets and Variables > Actions`:

| Secret Name | Type | Description |
| :--- | :--- | :--- |
| `PLAY_KEYSTORE_BASE64` | Secret | The Base64 encoded private key distribution `.jks` file. |
| `STORE_PASSWORD` | Secret | The password protecting your keystore store file. |
| `KEY_ALIAS` | Secret | Key alias identifier (e.g., `upload` or your custom release alias). |
| `KEY_PASSWORD` | Secret | The password protecting your specific private key entry. |

#### How to generate Base64 Keystore:
Execute the following on your terminal to encode your secure local production keystore to pass it safely under GitHub Actions configurations:
```bash
base64 -w 0 my-upload-key.jks > keystore_base64.txt
```
Copy content from `keystore_base64.txt` and save it to the `PLAY_KEYSTORE_BASE64` GitHub secret parameter.

---

## 📂 Project Directory Map

```text
├── .github/workflows/      # Automated CI/CD build & release YAMLs
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/            # Room Database, SQLCipher settings, and Seeding Repository
│   │   │   │   ├── ui/              # M3 High-Density Theme, Compose views & state models
│   │   │   │   ├── widget/          # Quick Settings pulls and Home screen AppWidgets templates
│   │   │   │   └── MainActivity.kt  # Root Entry Point Activity
│   │   │   └── res/                 # Graphics vector assets, string dictionaries, and layout resources
│   │   └── test/                    # Full Roborazzi visual layouts & test verification cases
│   └── build.gradle.kts             # App-level package configurations & scripts
├── gradle/                          # Central Version Catalogs configuration schemas & settings
├── build.gradle.kts                 # Root-level configuration script
└── metadata.json                    # Application identifiers metadata
```

---

## ⚖️ GDPR Portability, Compliance & Encryption details

1. **SQLCipher Storage Engine**: All SQLite data write/read transactions are securely intercepted and encrypted before writing to disk using a passphrase configured through the app's `AppDatabase` factory, matching custom local requirements.
2. **Local-First Sandboxing**: Zero information is ever shared with external databases unless a user chooses to manually back up or export records.
3. **GDPR ZIP Export**: Includes automated client-side processing to package user records (Markdown notes, category layouts, configurations) directly into local standard Portable ZIP documents for standard data transfer and full EU compliance options.
