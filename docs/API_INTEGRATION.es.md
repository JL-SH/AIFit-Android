# Integración con la API

**Español** · [English](API_INTEGRATION.md)

La app Android es un **cliente REST** de [AIFit-API](https://github.com/JL-SH/AIFit-API). La lógica de negocio y la IA están en el servidor; el cliente serializa peticiones, cachea respuestas y gestiona autenticación y sesión.

---

## URL base

`BuildConfig.API_BASE_URL` — ver [Instalación](SETUP.es.md).

Producción por defecto:

```
https://aifit-api-production.up.railway.app/api/v1/
```

`RetrofitClient`: kotlinx-serialization, timeouts de **180 s** (generación con IA).

---

## Envoltorio de respuesta

```json
{
  "success": true,
  "data": { },
  "message": null
}
```

Modelo: `ApiResponse<T>`. Métodos en `BaseRemoteDataSource`:

- `safeApiCall` → `Result<T>`
- `safeEmptyApiCall` → `Result<Unit>` (HTTP 204)

Errores → `AppException` vía `NetworkErrorMapper`.

---

## Autenticación

| Paso | Componente |
|------|------------|
| Login / registro | `AuthApiService` → JWT |
| Persistencia | `SessionManager` → `AuthDataStore` |
| Cabecera | `AuthInterceptor`: `Authorization: Bearer <token>` |
| 401 | `TokenAuthenticator` invalida sesión (sin refresh token) |
| Logout forzado | `logoutEvent` → navegación a auth |

Sin token, las peticiones públicas (login/register) no llevan cabecera.

---

## Servicios Retrofit

| Servicio | Dominio |
|----------|---------|
| `AuthApiService` | Autenticación |
| `UserApiService` | Perfil, onboarding, foto |
| `TrainingApiService` | Planes, generación, sustituciones |
| `WorkoutApiService` | Logs de sesión |
| `DietApiService` | Planes de dieta |
| `NutritionLogApiService` | Comidas, análisis por texto |
| `NutritionTargetApiService` | Objetivos nutricionales |
| `VisionApiService` | Foto de comida (multipart) |
| `ChatApiService` | Sesiones y mensajes |
| `ProgressDashboardApiService` | Progreso agregado |
| `BodyWeightApiService` | Peso |
| `MetabolicApiService` | Análisis metabólico |
| `ProgressionApiService` | Progresión |
| `EducationApiService` | Explicaciones, glosario |
| `ShoppingApiService` | Listas de compra |
| `GamificationApiService` | Logros, rachas, exportación |

DTOs en `data/dto/`, mapeo a dominio en `data/mapper/`.

---

## Caché offline

Tras lectura exitosa, muchos repositorios hacen **upsert en Room** por `userId`. Lecturas: caché primero, luego red.

`LocalDataCleaner` borra datos locales al invalidar sesión.

---

## Casos especiales

| Caso | Comportamiento |
|------|----------------|
| Generación IA | POST largos; timeout 180 s |
| Visión de comida | Multipart; resultado precarga registro de comida |
| Sesión de entreno | Log en primer set; series incrementales; cierre con fatiga |
| Chat nuevo | Sesión puede crearse al primer mensaje |
| Fotos Cloudinary | El backend devuelve URL; el cliente prefiere Cloudinary frente a avatar de Google |

---

## Flujo de errores

```
Error HTTP / red → NetworkErrorMapper → AppException → Result.Error → UiState / Snackbar
```

401 → invalidación de sesión → login con mensaje opcional.

---

## Documentación relacionada

- [Instalación](SETUP.es.md)
- [Arquitectura](ARCHITECTURE.es.md)
- [Funcionalidades](FEATURES.es.md)
- [Gestión de estado](STATE_MANAGEMENT.es.md)
