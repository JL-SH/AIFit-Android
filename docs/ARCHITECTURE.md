# Architecture

**English** · [Español](ARCHITECTURE.es.md)

AIFit Android follows **Clean Architecture** with **MVVM** on the presentation layer. Business rules stay testable and independent of Android frameworks, while Compose screens remain thin orchestrators.

---

## Layer overview

```
┌─────────────────────────────────────────────────────────┐
│  ui (Compose + ViewModel)                               │
│  Screens, UiState, UiEvent, theme, navigation hooks     │
└──────────────────────────┬──────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────┐
│  domain                                                  │
│  Models, Repository interfaces, UseCases                 │
└──────────────────────────┬──────────────────────────────┘
                           │ implemented by
┌──────────────────────────▼──────────────────────────────┐
│  data                                                    │
│  ApiService, DTOs, Room DAOs, Mappers, RepositoryImpl    │
└──────────────────────────┬──────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
        Backend REST API            Room + DataStore
```

| Layer | Package (per feature) | Depends on |
|-------|-------------------------|------------|
| **ui** | `feature.<name>.ui` | domain, core UI |
| **domain** | `feature.<name>.domain` | core `Result`, models only |
| **data** | `feature.<name>.data` | domain contracts, core network/DB |

The **domain** layer never imports Retrofit, Room, or Compose.

---

## MVVM responsibilities

| Piece | Role |
|-------|------|
| **Screen (Composable)** | Renders `UiState`, forwards user actions to the ViewModel, collects `UiEvent` for navigation/snackbars |
| **ViewModel** | Holds screen state, calls use cases, maps `Result` → `UiState`, emits one-off events |
| **UseCase** | Single application action (e.g. `GenerateTrainingPlanUseCase`); thin wrapper over repository |
| **Repository** | Abstracts data sources; interface in domain, implementation in data |

Typical call chain:

```
Screen → ViewModel → UseCase → Repository → ApiService / DAO
```

---

## Feature modules

Each capability lives under `app/src/main/java/com/jlsh/aifit/feature/<name>/`:

```
feature/<name>/
├── data/
│   ├── api/          # Retrofit interfaces
│   ├── dto/          # kotlinx-serialization models
│   ├── local/        # Room entities & DAOs
│   ├── mapper/       # DTO/entity → domain
│   └── repository/   # *RepositoryImpl
├── domain/
│   ├── model/
│   ├── repository/   # interfaces
│   └── usecase/
├── ui/
│   ├── *Screen.kt
│   ├── *ViewModel.kt
│   └── state/        # UiState, UiEvent
└── di/               # Hilt @Module
```

**Shared `core/`** provides cross-cutting concerns:

| Package | Contents |
|---------|----------|
| `core/network` | Retrofit, `AuthInterceptor`, `TokenAuthenticator`, `BaseRemoteDataSource` |
| `core/local` | `AiFitDatabase`, type converters |
| `core/datastore` | JWT and preferences (`AuthDataStore`) |
| `core/session` | `SessionManager`, `LocalDataCleaner` |
| `core/di` | `NetworkModule`, `DatabaseModule`, `DataStoreModule` |
| `core/ui` | Theme, reusable Compose components |
| `core/common` | `Result`, `AppException` |

---

## Dependency injection (Hilt)

- `@HiltAndroidApp` on the `Application` class
- `@HiltViewModel` on ViewModels; screens use `hiltViewModel()`
- Per-feature `@Module` in `feature/<name>/di/` binds repositories and API services
- `NetworkModule` provides singleton `OkHttpClient` and `Retrofit`

Constructor injection is used throughout; there is no service locator.

---

## Data strategies

### Remote

All HTTP calls go through Retrofit services returning `ApiResponse<T>`:

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
```

`BaseRemoteDataSource.safeApiCall` maps responses to `Result<T>` and maps exceptions to `AppException`.

### Local cache

Repositories often use a **cache-first** `Flow`:

1. Emit `Result.Loading`
2. Emit cached Room data if present
3. Fetch from network, upsert cache, emit fresh `Result.Success` or `Result.Error`

Example: `TrainingRepositoryImpl.getTrainingPlans()`.

### Session

`SessionManager` owns login state, coordinates logout, and clears Room/DataStore via `LocalDataCleaner` on session invalidation (401).

---

## Error model

`Result<out T>` is a sealed class: `Success`, `Error(AppException)`, `Loading`.

ViewModels branch on `Result` instead of throwing. `AppException` subtypes include network, auth, not-found, and server errors (`core/common/AppException.kt`).

---

## Related docs

- [State management](STATE_MANAGEMENT.md)
- [Navigation](NAVIGATION.md)
- [API integration](API_INTEGRATION.md)
- [Features & use cases](FEATURES.md)
