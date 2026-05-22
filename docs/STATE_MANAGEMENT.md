# State Management

**English** · [Español](STATE_MANAGEMENT.es.md)

State in AIFit flows **unidirectionally**: user actions go up to the ViewModel; state and events flow down to Compose. Async work never exposes raw exceptions to the UI.

---

## Core types

### `Result<T>` (domain / data → ViewModel)

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

Repositories and use cases return `Result` or `Flow<Result<T>>`. ViewModels map these to screen-specific `UiState`.

### `UiState` (ViewModel → Screen)

Per-screen sealed interfaces/classes, e.g. `TrainingDetailUiState`:

| State | Meaning |
|-------|---------|
| `Loading` | Initial or refresh in progress |
| `Ready` / success variant | Data to render |
| `Error(message)` | User-visible failure |
| Domain-specific | e.g. `Regenerating` on training detail |

Many success/error states implement `UiStateHost` markers from `core/ui/components/layout/` for shared loading/error composables.

### `UiEvent` (ViewModel → Screen, one-shot)

Navigation, snackbars, and side effects that must not be stored in `UiState`:

```kotlin
sealed class TrainingUiEvent {
    data class NavigateToDetail(val planId: String) : TrainingUiEvent()
    data class ShowSnackbar(val message: String) : TrainingUiEvent()
    data object NavigateBack : TrainingUiEvent()
    // ...
}
```

Emitted via `Channel` + `receiveAsFlow()` or `SharedFlow` with `extraBufferCapacity = 1`.

---

## Data flow diagram

```
┌──────────────┐    user action     ┌──────────────┐
│   Screen     │ ─────────────────► │  ViewModel   │
│  (Compose)   │                    │              │
└──────────────┘                    └──────┬───────┘
       ▲                                   │
       │ collect                           │ calls
       │ StateFlow / collectAsState        ▼
       │                            ┌──────────────┐
       │                            │   UseCase    │
       │                            └──────┬───────┘
       │                                   │
       │                            Result / Flow<Result>
       │                                   ▼
       │                            ┌──────────────┐
       └──────── UiState / UiEvent    │  Repository  │
                                    └──────────────┘
```

---

## ViewModel patterns

### Exposing state

```kotlin
private val _uiState = MutableStateFlow<TrainingDetailUiState>(TrainingDetailUiState.Loading)
val uiState: StateFlow<TrainingDetailUiState> = _uiState.asStateFlow()
```

Screens use:

```kotlin
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

### Collecting repository flows

```kotlin
viewModelScope.launch {
    getTrainingPlansUseCase().collect { result ->
        when (result) {
            is Result.Loading -> _uiState.value = TrainingDetailUiState.Loading
            is Result.Success -> _uiState.value = mapToReady(result.data)
            is Result.Error -> _uiState.value = TrainingDetailUiState.Error(result.exception.userMessage)
        }
    }
}
```

### Form / input state

Screens with text fields often keep **local `remember` state** for drafts; ViewModel holds validated snapshots on submit (e.g. track meal, login).

### Global session state

`SessionManager.isLoggedIn` and `logoutEvent` are app-wide. `AppNavViewModel` bridges logout to root navigation — not duplicated in feature ViewModels.

---

## UI consumption

### Rendering by state

```kotlin
when (val state = uiState) {
    is TrainingDetailUiState.Loading -> LoadingScreen()
    is TrainingDetailUiState.Ready -> TrainingDetailContent(state)
    is TrainingDetailUiState.Error -> ErrorScreen(state.message, onRetry = viewModel::load)
}
```

### Collecting events

```kotlin
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is TrainingUiEvent.NavigateBack -> onNavigateBack()
            is TrainingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }
}
```

Keep navigation **out of ViewModels** — emit events; let the screen/graph lambdas perform `navController.navigate(...)`.

---

## Multi-state ViewModels

Some features expose several state holders:

| ViewModel | States |
|-----------|--------|
| `NutritionViewModel` | `hubState`, `trackMealState`, `targetState`, `selectedTabIndex` |
| `TrainingViewModel` | `detailUiState`, `generateUiState`, hub list state |

Each maps to a sub-screen or tab section.

---

## Caching and stale UI

Cache-first repositories may emit **Success twice** (cache, then network). ViewModels should treat later emissions as updates, not assume a single load.

Delete races are handled in repositories (e.g. `recentlyDeletedIds` in `TrainingRepositoryImpl`) so the UI does not flash deleted plans back from stale network responses.

---

## Related docs

- [Architecture](ARCHITECTURE.md)
- [Navigation](NAVIGATION.md)
- [API integration](API_INTEGRATION.md)
- [Testing](TESTING.md)
