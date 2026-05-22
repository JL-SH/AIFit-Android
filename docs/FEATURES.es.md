# Funcionalidades y casos de uso

**Español** · [English](FEATURES.md)

AIFit se organiza en **módulos feature** bajo `feature/`. Cada módulo agrupa capacidades de usuario y endpoints del backend.

---

## Índice de 15 casos de uso

| # | Módulo(s) | Caso de uso | Use cases / pantallas clave |
|---|-----------|-------------|------------------------------|
| 1 | `auth` | Registro e inicio de sesión | `LoginUseCase`, `RegisterUseCase`, `GoogleLoginUseCase` |
| 2 | `user` | Onboarding y perfil | `CompleteOnboardingUseCase`, `CreateUserProfileUseCase` |
| 3 | `training` | Planes de entrenamiento (IA) | `GenerateTrainingPlanUseCase` |
| 4 | `training` | Gestión de planes | `GetTrainingPlansUseCase`, `SetActivePlanUseCase`, `DeleteTrainingPlanUseCase` |
| 5 | `workout`, `training` | Sesión de entrenamiento | `FinalizeWorkoutSessionUseCase`, `AddSetToLogUseCase` |
| 6 | `diet` | Planes de dieta (IA) | `GenerateDietPlanUseCase` |
| 7 | `nutrition` | Diario nutricional | `TrackMealUseCase`, `GetNutritionLogUseCase` |
| 8 | `vision`, `nutrition` | Análisis por foto | `AnalyzeFoodPhotoUseCase` → visión → registrar comida |
| 9 | `chat` | AI Coach | `SendChatMessageUseCase`, `GetChatSessionsUseCase` |
| 10 | `progress`, `home` | Panel de progreso | `GetProgressDashboardUseCase` |
| 11 | `progress` | Peso corporal | `LogBodyWeightUseCase`, `GetBodyWeightHistoryUseCase` |
| 12 | `metabolic` | Análisis metabólico | `AnalyzeMetabolicProgressUseCase`, `ApplyMetabolicAdjustmentUseCase` |
| 13 | `education` | Educación fitness | `GetWhyThisExerciseUseCase`, `GetGlossaryTermUseCase` |
| 14 | `shopping`, `nutrition` | Lista de la compra | `GenerateShoppingListUseCase`, `GetShoppingListsUseCase` |
| 15 | `gamification`, `user` | Gamificación | Logros, rachas, exportación de progreso |

---

## Referencia por módulo

| Módulo | Responsabilidad |
|--------|-----------------|
| `auth` | Email/contraseña y Google Sign-In; JWT vía `SessionManager` |
| `user` | Perfil, foto, flujo de onboarding |
| `home` | Dashboard principal y accesos rápidos |
| `training` | Hub, detalle, generación adaptativa, aprobación, sustituciones |
| `workout` | Sesión en vivo, series, historial |
| `diet` | Planes de dieta, generación, activación |
| `nutrition` | Diario, macros, registro de comidas |
| `vision` | Cámara y análisis IA de comida |
| `chat` | Sesiones, mensajes, archivar/renombrar |
| `progress` | Dashboard, resumen semanal, peso |
| `metabolic` | Análisis y ajustes calóricos |
| `progression` | Recomendaciones de progresión |
| `education` | Explicaciones y glosario |
| `shopping` | Listas desde el plan de dieta activo |
| `gamification` | Logros, rachas, récords, exportación |

---

## Servicios API

| Feature | Servicio |
|---------|----------|
| auth | `AuthApiService` |
| user | `UserApiService` |
| training | `TrainingApiService` |
| workout | `WorkoutApiService` |
| diet | `DietApiService` |
| nutrition | `NutritionLogApiService`, `NutritionTargetApiService` |
| vision | `VisionApiService` |
| chat | `ChatApiService` |
| progress | `ProgressDashboardApiService`, `BodyWeightApiService` |
| metabolic | `MetabolicApiService` |
| progression | `ProgressionApiService` |
| education | `EducationApiService` |
| shopping | `ShoppingApiService` |
| gamification | `GamificationApiService` |

Ver [Integración API](API_INTEGRATION.es.md).

---

## Documentación relacionada

- [Arquitectura](ARCHITECTURE.es.md)
- [Navegación](NAVIGATION.es.md)
- [Gestión de estado](STATE_MANAGEMENT.es.md)
