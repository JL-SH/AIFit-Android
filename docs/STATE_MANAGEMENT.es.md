# Gestión de estado

**Español** · [English](STATE_MANAGEMENT.md)

El estado fluye de forma **unidireccional**: acciones del usuario → ViewModel; estado y eventos → Compose. El trabajo asíncrono no expone excepciones crudas a la UI.

---

## Tipos principales

### `Result<T>` (domain / data → ViewModel)

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

### `UiState` (ViewModel → pantalla)

Por pantalla, p. ej. `TrainingDetailUiState`: `Loading`, `Ready`, `Error`, estados extra (`Regenerating`).

Muchos implementan `UiStateHost` para composables compartidos de carga/error.

### `UiEvent` (efectos de un solo uso)

Navegación, snackbars — no van en `UiState`:

```kotlin
sealed class TrainingUiEvent {
    data class ShowSnackbar(val message: String) : TrainingUiEvent()
    data object NavigateBack : TrainingUiEvent()
}
```

Emisión típica: `Channel` + `receiveAsFlow()`.

---

## Diagrama de flujo

```
Screen ──acción──► ViewModel ──► UseCase ──► Repository
   ▲                  │
   └── UiState / UiEvent (StateFlow + Flow)
```

---

## Patrones en ViewModel

### Estado

```kotlin
private val _uiState = MutableStateFlow<TrainingDetailUiState>(Loading)
val uiState = _uiState.asStateFlow()
```

En pantalla: `collectAsStateWithLifecycle()`.

### Colección de `Flow<Result>`

```kotlin
when (result) {
    is Result.Loading -> ...
    is Result.Success -> ...
    is Result.Error -> ...
}
```

### Formularios

Campos locales con `remember`; el ViewModel valida al enviar (login, registrar comida).

### Sesión global

`SessionManager` + `AppNavViewModel` para logout raíz — no duplicar en cada feature.

---

## Consumo en UI

- Renderizar con `when (uiState) { ... }`
- `LaunchedEffect` + `events.collect { }` para navegación y snackbars
- **No** llamar a `navController` desde el ViewModel

---

## Varios estados por ViewModel

Ej.: `NutritionViewModel` → `hubState`, `trackMealState`, `targetState`.

---

## Caché y UI obsoleta

Repositorios cache-first pueden emitir **dos Success** (caché + red). Los borrados usan guards en repositorio (`recentlyDeletedIds`) para evitar que un plan borrado reaparezca.

---

## Documentación relacionada

- [Arquitectura](ARCHITECTURE.es.md)
- [Navegación](NAVIGATION.es.md)
- [Integración API](API_INTEGRATION.es.md)
- [Testing](TESTING.es.md)
