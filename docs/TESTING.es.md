# Testing

**Español** · [English](TESTING.md)

AIFit usa **tests unitarios** en JVM (Robolectric cuando hace falta el framework Android), **tests de UI Compose** y **Roborazzi** para regresión visual de pantallas clave.

---

## Conjuntos de código

| Ubicación | Propósito |
|-----------|-----------|
| `app/src/test/` | Unitarios, ViewModels, DAOs, screenshots (~130 ficheros) |
| `app/src/androidTest/` | Tests instrumentados (Espresso + Compose) |

---

## Pirámide de tests

```
        Screenshots (Roborazzi)
        UI / Compose
        ViewModel (MockK + Turbine)
        UseCase
        Repository / Mapper / DAO
```

---

## Librerías

| Librería | Uso |
|----------|-----|
| JUnit 4 | Runner |
| MockK | Mocks |
| kotlinx-coroutines-test | `runTest` |
| Turbine | Aserciones sobre `Flow` |
| Robolectric | Android en JVM |
| Compose UI Test | Pantallas y componentes |
| Room Testing | BD en memoria |
| Roborazzi | Capturas PNG en `src/test/screenshots/` |

---

## Ejecutar tests

**Todos los unitarios**

```bash
./gradlew testDebugUnitTest
```

**Una clase**

```bash
./gradlew testDebugUnitTest --tests "com.jlsh.aifit.feature.auth.domain.usecase.LoginUseCaseTest"
```

**Grabar screenshots Roborazzi**

```bash
./gradlew recordRoborazziDebug
```

**Verificar screenshots**

```bash
./gradlew verifyRoborazziDebug
```

**Instrumentados** (emulador/dispositivo)

```bash
./gradlew connectedDebugAndroidTest
```

---

## Qué se testea

- **Domain:** use cases con repositorios mock.
- **Data:** repositorios, DAOs Room, mappers.
- **Network:** interceptores, `ApiResponse`, mapeo de errores.
- **ViewModel:** transiciones de `UiState` y emisión de `UiEvent`.
- **UI:** Compose con ViewModels mock; Roborazzi en estados loading/error/success.

Ejemplo de salida: `app/src/test/screenshots/LoginScreen_idle_light.png`

---

## Convenciones

- Nombre: `<Clase>BajoTest>Test`
- Paquete espejo de producción
- ViewModels: `runTest` + MockK
- Flows: Turbine con `awaitItem()`

---

## CI recomendado

1. `testDebugUnitTest` en cada PR
2. `verifyRoborazziDebug` si cambia UI
3. Opcional: `connectedDebugAndroidTest` en main

---

## Documentación relacionada

- [Arquitectura](ARCHITECTURE.es.md)
- [Gestión de estado](STATE_MANAGEMENT.es.md)
- [Instalación](SETUP.es.md)
