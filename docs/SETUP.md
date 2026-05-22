# Setup & Local Development

**English** · [Español](SETUP.es.md)

Guide to clone, configure, and run the AIFit Android client on an emulator or physical device.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Ladybug (2024.2) or newer |
| JDK | 17 |
| Android SDK | API 35 (`compileSdk`), min API 26 |
| Backend | [AIFit-API](https://github.com/JL-SH/AIFit-API) running and reachable |

---

## 1. Clone the repository

```bash
git clone https://github.com/JL-SH/AIFit-Android.git
cd AIFit-Android
```

---

## 2. Configure `local.properties`

Create or edit `local.properties` at the project root (gitignored):

```properties
sdk.dir=/path/to/Android/sdk

# Google Sign-In — OAuth 2.0 Web client ID (required for Google login)
GOOGLE_WEB_CLIENT_ID=your-id.apps.googleusercontent.com
```

`GOOGLE_WEB_CLIENT_ID` is read in [`app/build.gradle.kts`](../app/build.gradle.kts) and exposed as `BuildConfig.GOOGLE_WEB_CLIENT_ID`.

---

## 3. Point the app at the backend

The API base URL is defined as `BuildConfig.API_BASE_URL` in [`app/build.gradle.kts`](../app/build.gradle.kts).

| Environment | Typical URL |
|-------------|-------------|
| Android Emulator → host machine | `http://10.0.2.2:8080/api/v1/` |
| Physical device → LAN | `http://<your-pc-ip>:8080/api/v1/` |
| Production (Railway) | `https://aifit-api-production.up.railway.app/api/v1/` |

For local development, change the `debug` `buildConfigField` for `API_BASE_URL`:

```kotlin
debug {
    buildConfigField(
        "String",
        "API_BASE_URL",
        "\"http://10.0.2.2:8080/api/v1/\""
    )
}
```

> **Note:** Use `10.0.2.2`, not `localhost`, when testing on the emulator.

Start the backend first (see the [AIFit-API](https://github.com/JL-SH/AIFit-API) repository). The app will not work without it.

---

## 4. Google Sign-In (optional)

1. Create an OAuth 2.0 **Web** client in [Google Cloud Console](https://console.cloud.google.com/).
2. Add the SHA-1 of your debug keystore to the Android OAuth client if required.
3. Put the Web client ID in `local.properties` as `GOOGLE_WEB_CLIENT_ID`.

Email/password login works without Google configuration.

---

## 5. Build and run

**Android Studio**

1. Open the project folder.
2. **File → Sync Project with Gradle Files**.
3. Select an emulator or device (API 26+).
4. Run the **app** configuration.

**Command line**

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/`.

---

## 6. Verify connectivity

1. Log in or register a test user (backend must have DB migrated).
2. Complete onboarding if prompted.
3. If requests fail immediately, check:
   - Backend is running on the expected port
   - `API_BASE_URL` matches your environment
   - Emulator uses `10.0.2.2` for host `localhost`

Enable HTTP logging in debug builds (`HttpLoggingInterceptor.Level.BODY` in `RetrofitClient`).

---

## Build variants

| Variant | Notes |
|---------|-------|
| **debug** | Debuggable, verbose HTTP logs, configurable `API_BASE_URL` |
| **release** | Minify + shrink resources enabled; uses default production `API_BASE_URL` |

---

## Related docs

- [API integration](API_INTEGRATION.md)
- [Testing](TESTING.md)
