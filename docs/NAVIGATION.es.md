# Navegación

**Español** · [English](NAVIGATION.md)

La navegación usa **Navigation Compose** con un **grafo raíz** (auth vs main) y **subgrafos** por pestaña del bottom bar.

Código: `app/src/main/java/com/jlsh/aifit/navigation/`.

---

## Jerarquía

```
AppNavGraph
├── auth (AuthNavGraph)
└── main (MainNavGraph)
    └── NavHost de pestañas
        ├── home_graph
        ├── training_graph
        ├── nutrition_graph
        ├── coach_graph
        └── profile_graph
```

`AppNavViewModel` elige el destino inicial:

- Sin login → `auth`
- Login sin perfil completo → `auth` (create profile)
- Perfil completo → `main`

---

## Flujo auth

| Ruta | Pantalla |
|------|----------|
| `auth/login` | Login |
| `auth/register` | Registro |
| `auth/create_profile` | Perfil |
| `auth/onboarding_generating` | Generando planes |
| `auth/onboarding_training_approval` | Aprobar entreno |
| `auth/onboarding_nutrition_approval` | Aprobar nutrición |

Logout forzado (401): limpia pila → `auth` + mensaje en login.

---

## Shell principal

`MainNavScreen`: `NavHost` de pestañas + `BottomNavBar` (Home, Training, Nutrition, Coach, Profile).

La barra se **oculta** en rutas de foco: sesión de entreno, chat, visión, generación/aprobación de planes (`LocalBottomBarVisibility`).

Cambio de pestaña: `saveState` + `restoreState` + `launchSingleTop`.

---

## Subgrafos por pestaña

### Home (`HomeRoutes`)
`home`, `home/dashboard`, `home/body_weight`, `home/weekly_summary`, `home/metabolic_analysis`

### Training (`TrainingRoutes`)
Hub, `detail/{planId}`, `generate`, `approval/{planId}`, `session/{planId}/{dayId}`, `workout_log`, `workout_detail/{logId}`, `workout_history`

Helpers: `TrainingRoutes.detailRoute(planId)`, etc.

### Nutrition (`NutritionRoutes`)
Hub, `track_meal`, `food_vision`, `target`, `diet_detail`, `diet_generate`, `diet/approval`, `shopping_detail`

### Coach (`CoachRoutes`)
`coach`, `chat/{sessionId}`, `new_chat`

### Profile (`ProfileRoutes`)
Hub, `edit`, atajos a progreso, `export`, `gamification?tab=`, `glossary`

---

## Navegación entre pestañas

Las pantallas reciben lambdas `onNavigateToX` desde el `NavGraphBuilder`. Los ViewModels emiten `UiEvent`; la pantalla invoca el callback (sin `NavController` dentro del ViewModel).

---

## Rutas centralizadas

[`NavRoutes.kt`](../app/src/main/java/com/jlsh/aifit/navigation/NavRoutes.kt) — usar helpers en lugar de strings sueltos cuando sea posible.

---

## Documentación relacionada

- [Gestión de estado](STATE_MANAGEMENT.es.md)
- [Funcionalidades](FEATURES.es.md)
- [Arquitectura](ARCHITECTURE.es.md)
