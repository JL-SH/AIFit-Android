# Testing

**English** · [Español](TESTING.es.md)

AIFit uses **unit tests** on the JVM (Robolectric where Android APIs are needed), **Compose UI tests** for screens, and **Roborazzi** for screenshot regression of key UI states.

---

## Test source sets

| Location | Purpose |
|----------|---------|
| `app/src/test/` | Unit tests, ViewModel tests, DAO tests, screenshot tests (~130 files) |
| `app/src/androidTest/` | Instrumented tests (Espresso + Compose test harness) |

Most coverage lives in `src/test` for fast feedback in CI and IDE.

---

## Testing pyramid

```
        ┌─────────────────┐
        │  Screenshots    │  Roborazzi — visual regression (selected screens)
        ├─────────────────┤
        │  UI / Compose   │  Screen tests, component tests
        ├─────────────────┤
        │  ViewModel      │  MockK use cases, Turbine on flows
        ├─────────────────┤
        │  UseCase        │  MockK repositories
        ├─────────────────┤
        │  Repository     │  MockK API + in-memory / Robolectric Room
        ├─────────────────┤
        │  Mapper / DAO   │  Pure logic & Room (Robolectric)
        └─────────────────┘
```

---

## Libraries

| Library | Usage |
|---------|--------|
| **JUnit 4** | Test runner |
| **MockK** | Mocking repositories, use cases, APIs |
| **kotlinx-coroutines-test** | `runTest`, test dispatchers |
| **Turbine** | Asserting `Flow` emissions |
| **Robolectric** | Android framework on JVM (`@Config`, `ApplicationProvider`) |
| **Compose UI Test** | `createComposeRule()`, semantics, interactions |
| **Room Testing** | In-memory DB or Robolectric-backed DB |
| **Roborazzi** | `captureRoboImage()` → PNG under `src/test/screenshots/` |

---

## Running tests

**All unit tests**

```bash
./gradlew testDebugUnitTest
```

**Single test class**

```bash
./gradlew testDebugUnitTest --tests "com.jlsh.aifit.feature.auth.domain.usecase.LoginUseCaseTest"
```

**Update Roborazzi screenshots** (record golden images)

```bash
./gradlew recordRoborazziDebug
```

**Verify screenshots** (CI / local check)

```bash
./gradlew verifyRoborazziDebug
```

**Instrumented tests** (requires device/emulator)

```bash
./gradlew connectedDebugAndroidTest
```

---

## What we test

### Domain (`*UseCaseTest`)
- Success and error paths from mocked repositories
- Input validation where applicable

### Data (`*RepositoryImplTest`, `*DaoTest`, `*MapperTest`)
- Cache-first flow behavior
- DTO → domain mapping edge cases
- Room CRUD and queries

### Network (`AuthInterceptorTest`, `BaseRemoteDataSourceTest`, `NetworkErrorMapperTest`)
- Header injection, envelope parsing, exception mapping

### ViewModel (`*ViewModelTest`)
- Initial state, user actions, `UiState` transitions
- `UiEvent` emission (navigation, snackbars) via Turbine

### UI (`*ScreenTest`, `screenshots/*`)
- Compose trees with mocked ViewModels
- Roborazzi captures for light/dark and loading/error/success states

Example screenshot output path:

```
app/src/test/screenshots/LoginScreen_idle_light.png
```

---

## Conventions

- Test class name: `<ClassUnderTest>Test`
- Package mirrors production: `com.jlsh.aifit.feature.<name>.…`
- ViewModels: inject dependencies via constructor; use `runTest` for coroutines
- Flows: `test { awaitItem() … cancelAndIgnoreRemainingEvents() }` with Turbine

---

## CI recommendations

1. `./gradlew testDebugUnitTest` on every PR
2. `./gradlew verifyRoborazziDebug` when UI screenshots change
3. Optional: `connectedDebugAndroidTest` on merge to main

---

## Related docs

- [Architecture](ARCHITECTURE.md)
- [State management](STATE_MANAGEMENT.md)
- [Setup](SETUP.md)
