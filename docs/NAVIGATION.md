# Navigation

**English** · [Español](NAVIGATION.es.md)

Navigation uses **Jetpack Navigation Compose** with a **root graph** (auth vs main) and **nested graphs** per bottom-tab section.

Source files: `app/src/main/java/com/jlsh/aifit/navigation/`.

---

## Graph hierarchy

```
AppNavGraph (root NavHost)
├── auth (AuthNavGraph)          — unauthenticated / incomplete profile
└── main (MainNavGraph)          — authenticated shell
    └── Tab NavHost (5 nested graphs)
        ├── home_graph
        ├── training_graph
        ├── nutrition_graph
        ├── coach_graph
        └── profile_graph
```

| Graph | Route constant | Entry |
|-------|----------------|-------|
| Auth | `AuthRoutes.GRAPH` = `"auth"` | Login or create profile |
| Main | `MainRoutes.GRAPH` = `"main"` | Bottom navigation shell |

`AppNavViewModel` picks the root `startDestination`:

- Not logged in → `auth`
- Logged in, profile incomplete → `auth` (starts at create profile)
- Logged in, profile complete → `main`

---

## Auth flow (`AuthNavGraph`)

| Route | Screen |
|-------|--------|
| `auth/login` | Login |
| `auth/register` | Register |
| `auth/create_profile` | Create / edit profile |
| `auth/onboarding_generating` | Plan generation loading |
| `auth/onboarding_training_approval` | Approve training plan |
| `auth/onboarding_nutrition_approval` | Approve nutrition plan |

On forced logout (`SessionManager.logoutEvent`), `AppNavGraph` clears the back stack and navigates to `auth`, passing `sessionExpiredMessage` to login.

---

## Main shell (`MainNavGraph`)

`MainNavScreen` hosts:

- A **tab-level** `NavHost` with five nested navigation graphs
- `BottomNavBar` with tabs: Home, Training, Nutrition, Coach, Profile
- `LocalBottomBarVisibility` to hide the bar on focus screens (workout session, chat, food vision, plan generation/approval)

Tab switch uses `popUpTo(startDestination) { saveState = true }`, `launchSingleTop = true`, `restoreState = true` to preserve each tab's back stack.

---

## Tab subgraphs

### Home (`HomeRoutes`)

| Route | Screen |
|-------|--------|
| `home` | Home dashboard |
| `home/dashboard` | Progress dashboard |
| `home/body_weight` | Body weight |
| `home/weekly_summary` | Weekly summary |
| `home/metabolic_analysis` | Metabolic analysis |

### Training (`TrainingRoutes`)

| Route | Screen |
|-------|--------|
| `training` | Training hub |
| `training/detail/{planId}` | Plan detail |
| `training/generate?adaptive=&basePlanId=` | Generate plan |
| `training/approval/{planId}` | Plan approval |
| `training/session/{planId}/{dayId}` | Live workout |
| `training/workout_log?planId=` | Manual workout log |
| `training/workout_detail/{logId}` | Workout log detail |
| `training/workout_history` | History |

Helper functions: `TrainingRoutes.detailRoute(planId)`, `workoutSessionRoute(...)`, etc.

### Nutrition (`NutritionRoutes`)

| Route | Screen |
|-------|--------|
| `nutrition` | Nutrition hub |
| `nutrition/track_meal?mode=&prefilled=` | Track meal |
| `nutrition/food_vision` | Food vision |
| `nutrition/target` | Nutrition targets |
| `nutrition/diet_detail/{planId}` | Diet detail |
| `nutrition/diet_generate?...` | Generate diet |
| `nutrition/diet/approval/{planId}` | Diet approval |
| `nutrition/shopping_detail/{listId}` | Shopping list |

### Coach (`CoachRoutes`)

| Route | Screen |
|-------|--------|
| `coach` | Chat session list |
| `coach/chat/{sessionId}` | Existing chat |
| `coach/new_chat` | New chat (session created on first send) |

### Profile (`ProfileRoutes`)

| Route | Screen |
|-------|--------|
| `profile` | Profile hub |
| `profile/edit?mode=edit` | Edit profile |
| `profile/dashboard`, `body_weight`, `weekly_summary`, `metabolic` | Progress shortcuts |
| `profile/export` | Progress export |
| `profile/gamification?tab=` | Gamification |
| `profile/glossary` | Glossary |

---

## Cross-tab navigation

Screens receive lambda callbacks (`onNavigateToX`) from the `NavGraphBuilder` composable registrations. ViewModels emit `UiEvent` types; the screen collects them and invokes the appropriate callback.

Example: nutrition hub navigates to food vision → track meal with prefilled data via `NutritionRoutes.trackMealRoute(prefilled = ...)`.

---

## Route definitions

All route strings and builders are centralized in [`NavRoutes.kt`](../app/src/main/java/com/jlsh/aifit/navigation/NavRoutes.kt). Prefer these helpers over hard-coded strings in ViewModels when possible (some events still pass raw route segments resolved at the graph level).

---

## Related docs

- [State management](STATE_MANAGEMENT.md) — `UiEvent` navigation pattern
- [Features](FEATURES.md)
- [Architecture](ARCHITECTURE.md)
