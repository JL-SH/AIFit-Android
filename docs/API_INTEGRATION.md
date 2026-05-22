# API Integration

**English** · [Español](API_INTEGRATION.es.md)

The Android app is a **REST client** for [AIFit-API](https://github.com/JL-SH/AIFit-API). All business logic and AI orchestration run on the server; the client serializes requests, caches responses, and handles auth/session lifecycle.

---

## Base URL

Configured in `BuildConfig.API_BASE_URL` (see [Setup](SETUP.md)).

Default production value:

```
https://aifit-api-production.up.railway.app/api/v1/
```

Retrofit is built in `RetrofitClient` with kotlinx-serialization and 180s timeouts (long AI generation calls).

---

## Response envelope

Every JSON body follows:

```json
{
  "success": true,
  "data": { },
  "message": null
}
```

Kotlin model: `ApiResponse<T>` in `core/network/BaseRemoteDataSource.kt`.

Repositories extend `BaseRemoteDataSource` and use:

- `safeApiCall { api.someEndpoint() }` → `Result<T>`
- `safeEmptyApiCall { api.deleteSomething() }` → `Result<Unit>` for HTTP 204

On failure, exceptions are mapped to `AppException` via `NetworkErrorMapper`.

---

## Authentication

| Step | Component |
|------|-----------|
| Login / register | `AuthApiService` → JWT in response |
| Persist token | `SessionManager.saveSession()` → `AuthDataStore` |
| Attach to requests | `AuthInterceptor` adds `Authorization: Bearer <token>` |
| 401 handling | `TokenAuthenticator` invalidates session (no refresh endpoint) |
| Forced logout | `SessionManager.logoutEvent` → `AppNavViewModel` navigates to auth |

Public endpoints (login, register) work without a token; the interceptor skips the header when none is stored.

---

## API services

One Retrofit interface per domain, provided by Hilt modules:

| Service | Domain |
|---------|--------|
| `AuthApiService` | Login, register, Google token exchange |
| `UserApiService` | Profile, onboarding, photo upload |
| `TrainingApiService` | Plans, generation, substitutions, warm-up |
| `WorkoutApiService` | Session logs, sets, finalize |
| `DietApiService` | Diet plans |
| `NutritionLogApiService` | Meal logs, text analysis |
| `NutritionTargetApiService` | Calorie/macro targets |
| `VisionApiService` | Food photo analysis (multipart) |
| `ChatApiService` | Sessions, messages, titles |
| `ProgressDashboardApiService` | Aggregated progress |
| `BodyWeightApiService` | Weight logs |
| `MetabolicApiService` | Analysis and adjustments |
| `ProgressionApiService` | Progression recommendations |
| `EducationApiService` | Explanations, glossary |
| `ShoppingApiService` | Shopping lists |
| `GamificationApiService` | Achievements, streaks, export |

Interfaces live in `feature/<name>/data/api/`. DTOs use `@Serializable` and map to domain models in `data/mapper/`.

---

## Offline cache

After a successful network read, many repositories **upsert Room entities** keyed by `userId`. Subsequent reads emit cache first, then refresh from the network.

Session invalidation triggers `LocalDataCleaner`, which wipes user-scoped tables so the next login does not leak prior user data.

---

## Special cases

| Case | Behavior |
|------|----------|
| **AI generation** | Long-running POST; 180s OkHttp timeout |
| **Food vision** | Multipart upload to vision endpoint; result prefills track-meal navigation |
| **Workout session** | Log created on first set; sets appended incrementally; finalize sends fatigue payload |
| **New chat** | Session may be created on first message, not on opening the screen |
| **Cloudinary photos** | Backend stores URLs; client prefers Cloudinary URL over Google avatar in `UserMapper` |

---

## Error handling flow

```
HTTP / parse error
    → NetworkErrorMapper → AppException
    → Result.Error
    → ViewModel maps to UiState.Error or ShowSnackbar event
```

HTTP 401 → `TokenAuthenticator` → `SessionManager.invalidateSession()` → navigate to login with optional message.

---

## Related docs

- [Setup](SETUP.md)
- [Architecture](ARCHITECTURE.md)
- [Features](FEATURES.md)
- [State management](STATE_MANAGEMENT.md)
