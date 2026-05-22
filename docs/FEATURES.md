# Features & Use Cases

**English** · [Español](FEATURES.es.md)

AIFit is organized into **feature modules** under `feature/`. Each module maps to one or more user-facing capabilities and backend endpoints.

---

## Use case index (15)

| # | Feature module(s) | Use case | Key use cases / screens |
|---|-------------------|----------|-------------------------|
| 1 | `auth` | Sign up & sign in | `LoginUseCase`, `RegisterUseCase`, `GoogleLoginUseCase` → Login, Register |
| 2 | `user` | Onboarding & profile | `CompleteOnboardingUseCase`, `CreateUserProfileUseCase` → Create profile, onboarding approval |
| 3 | `training` | AI training plans | `GenerateTrainingPlanUseCase` → Generate plan, approval |
| 4 | `training` | Training plan management | `GetTrainingPlansUseCase`, `SetActivePlanUseCase`, `DeleteTrainingPlanUseCase` → Training hub, detail |
| 5 | `workout`, `training` | Workout session | `FinalizeWorkoutSessionUseCase`, `AddSetToLogUseCase` → Workout session, log, history |
| 6 | `diet` | AI diet plans | `GenerateDietPlanUseCase` → Generate diet, approval |
| 7 | `nutrition` | Nutrition diary | `TrackMealUseCase`, `GetNutritionLogUseCase`, `GetNutritionHistoryUseCase` → Nutrition hub, track meal |
| 8 | `vision`, `nutrition` | Food photo analysis | `AnalyzeFoodPhotoUseCase` → Food vision → prefilled track meal |
| 9 | `chat` | AI Coach | `SendChatMessageUseCase`, `GetChatSessionsUseCase` → Session list, chat |
| 10 | `progress`, `home` | Progress dashboard | `GetProgressDashboardUseCase` → Dashboard, weekly summary |
| 11 | `progress` | Body weight logging | `LogBodyWeightUseCase`, `GetBodyWeightHistoryUseCase` → Body weight screen |
| 12 | `metabolic` | Metabolic analysis | `AnalyzeMetabolicProgressUseCase`, `ApplyMetabolicAdjustmentUseCase` → Metabolic analysis |
| 13 | `education` | Fitness education | `GetWhyThisExerciseUseCase`, `GetGlossaryTermUseCase` → Sheets, glossary |
| 14 | `shopping`, `nutrition` | Shopping list | `GenerateShoppingListUseCase`, `GetShoppingListsUseCase` → Shopping detail (from nutrition tab) |
| 15 | `gamification`, `user` | Gamification | `GetUserAchievementsUseCase`, `GetUserStreaksUseCase`, `GetProgressExportUseCase` → Gamification, export |

---

## Module reference

### `auth`
Email/password and Google Sign-In. Persists JWT via `SessionManager` after successful login.

### `user`
Profile CRUD, photo upload, onboarding flow (training + nutrition plan approval).

### `home`
Main dashboard: today's workout, shortcuts to progress and weight logging.

### `training`
Plan hub, detail, generation (standard/adaptive), approval, warm-up protocol, exercise substitutions.

### `workout`
Live session logging, set persistence, finalize with systemic fatigue and joint report, history and detail.

### `diet`
Diet plan hub, detail, generation, pause/delete, active plan selection.

### `nutrition`
Daily log, macro targets, meal tracking (manual/text), integration with vision prefills.

### `vision`
Camera capture and `AnalyzeFoodPhotoUseCase` for AI meal recognition.

### `chat`
Session list, new chat (lazy session creation on first message), rename/archive/delete.

### `progress`
Progress dashboard, weekly summary, body weight chart.

### `metabolic`
Metabolic status analysis and applying recommended caloric adjustments.

### `progression`
Per-exercise and full-plan progression recommendations.

### `education`
“Why this exercise/meal” explanations and glossary terms by knowledge level.

### `shopping`
Shopping lists generated from the active diet plan.

### `gamification`
Achievements, streaks, personal records, progress export.

---

## API services per feature

| Feature | Retrofit service |
|---------|------------------|
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

See [API integration](API_INTEGRATION.md) for auth, envelopes, and error handling.

---

## Related docs

- [Architecture](ARCHITECTURE.md)
- [Navigation](NAVIGATION.md)
- [State management](STATE_MANAGEMENT.md)
