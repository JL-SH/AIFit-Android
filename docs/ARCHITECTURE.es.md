# Arquitectura

**Español** · [English](ARCHITECTURE.md)

El cliente Android de AIFit sigue **Clean Architecture** con **MVVM** en la capa de presentación. Las reglas de negocio permanecen testeables e independientes del framework Android; las pantallas Compose son orquestadores ligeros.

---

## Capas

```
┌─────────────────────────────────────────────────────────┐
│  ui (Compose + ViewModel)                               │
│  Pantallas, UiState, UiEvent, tema, navegación          │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│  domain — modelos, interfaces de repositorio, use cases │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│  data — API, DTOs, Room, mappers, RepositoryImpl        │
└──────────────────────────┬──────────────────────────────┘
              ┌────────────┴────────────┐
              ▼                         ▼
        API REST backend            Room + DataStore
```

| Capa | Paquete | Depende de |
|------|---------|------------|
| **ui** | `feature.<nombre>.ui` | domain, core UI |
| **domain** | `feature.<nombre>.domain` | solo `Result` y modelos |
| **data** | `feature.<nombre>.data` | contratos domain, red/BD |

**domain** no importa Retrofit, Room ni Compose.

---

## MVVM

| Pieza | Rol |
|-------|-----|
| **Screen** | Renderiza `UiState`, envía acciones al ViewModel, consume `UiEvent` |
| **ViewModel** | Estado de pantalla, llama use cases, mapea `Result` → `UiState` |
| **UseCase** | Una acción de aplicación (p. ej. generar plan) |
| **Repository** | Abstrae fuentes de datos; interfaz en domain, impl en data |

Cadena típica: `Screen → ViewModel → UseCase → Repository → ApiService / DAO`

---

## Módulos por feature

```
feature/<nombre>/
├── data/       # api, dto, local, mapper, repository
├── domain/     # model, repository, usecase
├── ui/         # Screen, ViewModel, state
└── di/         # Módulo Hilt
```

**`core/`** compartido: red (`RetrofitClient`, interceptores), `AiFitDatabase`, `AuthDataStore`, `SessionManager`, tema Material 3, componentes UI, `Result` y `AppException`.

---

## Inyección de dependencias (Hilt)

- `@HiltAndroidApp` en la `Application`
- `@HiltViewModel` + `hiltViewModel()` en pantallas
- Módulo Hilt por feature en `di/`
- `NetworkModule` provee `OkHttpClient` y `Retrofit` singleton

---

## Estrategias de datos

### Remoto

Respuestas envueltas en `ApiResponse<T>`. `BaseRemoteDataSource.safeApiCall` devuelve `Result<T>`.

### Caché local

Muchos repositorios usan **cache-first** con `Flow`:

1. `Result.Loading`
2. Datos de Room si existen
3. Red, upsert en Room, `Success` o `Error`

Ejemplo: `TrainingRepositoryImpl.getTrainingPlans()`.

### Sesión

`SessionManager` gestiona login, logout y limpieza con `LocalDataCleaner` ante 401.

---

## Errores

`Result`: `Success`, `Error(AppException)`, `Loading`. Los ViewModels no propagan excepciones crudas a la UI.

---

## Documentación relacionada

- [Gestión de estado](STATE_MANAGEMENT.es.md)
- [Navegación](NAVIGATION.es.md)
- [Integración API](API_INTEGRATION.es.md)
- [Funcionalidades](FEATURES.es.md)
