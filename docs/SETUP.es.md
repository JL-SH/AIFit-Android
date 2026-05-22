# Instalación y desarrollo local

**Español** · [English](SETUP.md)

Guía para clonar, configurar y ejecutar el cliente Android de AIFit en emulador o dispositivo físico.

---

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| Android Studio | Ladybug (2024.2) o superior |
| JDK | 17 |
| Android SDK | API 35 (`compileSdk`), mínimo API 26 |
| Backend | [AIFit-API](https://github.com/JL-SH/AIFit-API) en ejecución y accesible |

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/JL-SH/AIFit-Android.git
cd AIFit-Android
```

---

## 2. Configurar `local.properties`

En la raíz del proyecto (no versionado):

```properties
sdk.dir=/ruta/a/Android/sdk

# Google Sign-In — OAuth 2.0 Web client ID
GOOGLE_WEB_CLIENT_ID=tu-id.apps.googleusercontent.com
```

Se lee en [`app/build.gradle.kts`](../app/build.gradle.kts) como `BuildConfig.GOOGLE_WEB_CLIENT_ID`.

---

## 3. URL del backend

`BuildConfig.API_BASE_URL` se define en [`app/build.gradle.kts`](../app/build.gradle.kts).

| Entorno | URL típica |
|---------|------------|
| Emulador → PC host | `http://10.0.2.2:8080/api/v1/` |
| Dispositivo físico → LAN | `http://<ip-de-tu-pc>:8080/api/v1/` |
| Producción (Railway) | `https://aifit-api-production.up.railway.app/api/v1/` |

En desarrollo, modifica el `buildConfigField` de `API_BASE_URL` en el bloque **debug**:

```kotlin
debug {
    buildConfigField(
        "String",
        "API_BASE_URL",
        "\"http://10.0.2.2:8080/api/v1/\""
    )
}
```

> En emulador usa **`10.0.2.2`**, no `localhost`.

Arranca el backend antes que la app ([AIFit-API](https://github.com/JL-SH/AIFit-API)).

---

## 4. Google Sign-In (opcional)

1. Crea un cliente OAuth **Web** en Google Cloud Console.
2. Añade el SHA-1 del keystore de debug si hace falta.
3. Configura `GOOGLE_WEB_CLIENT_ID` en `local.properties`.

El login con email/contraseña no requiere Google.

---

## 5. Compilar y ejecutar

**Android Studio:** abrir proyecto → Sync Gradle → dispositivo API 26+ → Run **app**.

**Línea de comandos:**

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/`.

---

## 6. Comprobar conectividad

1. Registro o login (backend con BD migrada).
2. Completar onboarding si aplica.
3. Si fallan las peticiones: backend activo, `API_BASE_URL` correcta, emulador con `10.0.2.2`.

En debug, los logs HTTP van a nivel BODY (`RetrofitClient`).

---

## Variantes de build

| Variante | Notas |
|----------|-------|
| **debug** | Logs HTTP, `API_BASE_URL` configurable |
| **release** | Minify + shrink; URL de producción por defecto |

---

## Documentación relacionada

- [Integración API](API_INTEGRATION.es.md)
- [Testing](TESTING.es.md)
