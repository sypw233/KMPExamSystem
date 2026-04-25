# DI LAYER KNOWLEDGE BASE

**Scope:** `di/` — Koin dependency injection modules
**Files:** 13 (AppModule, ViewModelModule, 10 feature modules, StorageModule)

## STRUCTURE

```
di/
├── AppModule.kt           # Aggregator — includes() all sub-modules + DialogManager
├── ViewModelModule.kt     # 3 ViewModels (Login, Register, ApiTest)
├── AuthModule.kt          # AuthApi + AuthRepository
├── CourseModule.kt        # CourseApi + CourseRepository + CourseViewModel
├── ExamModule.kt          # ExamApi + ExamRepository + ExamViewModel + ExamComposeViewModel
├── ExtendedModules.kt     # QuestionBank, File, AiGrading modules
├── NotificationModule.kt  # NotificationApi + NotificationRepository + NotificationViewModel
├── QuestionModule.kt      # QuestionApi + QuestionRepository + QuestionViewModel
├── StatisticsModule.kt    # StatisticsApi + StatisticsRepository + StatisticsViewModel
├── SubmissionModule.kt    # SubmissionApi + SubmissionRepository
├── StorageModule.kt       # expect fun createLocalStorage() + TokenStorage DI
├── UserManageModule.kt    # UserManageApi + UserManageRepository + UserManageViewModel
└── ApiTestModule.kt       # ApiTestService + ApiTestRepository + ApiTestViewModel
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Add new feature DI | Create `*Module.kt` | Follow `AuthModule.kt` pattern |
| Register module | `AppModule.kt` | Add `includes(yourModule)` |
| Add ViewModel to DI | `ViewModelModule.kt` or feature module | Use `factory { }`, never `viewModel { }` |
| Platform storage | `StorageModule.kt` + platform actuals | Android: SharedPreferences, iOS: NSUserDefaults, Desktop: file |

## CONVENTIONS

- **Module per feature**: Each domain area has its own module (`AuthModule`, `ExamModule`, etc.).
- **Aggregation**: `AppModule.kt` uses `includes()` to compose all sub-modules. This is the ONLY module registered at application startup.
- **ViewModels**: Use `factory { }` (fresh instance per screen). Never use Koin's `viewModel { }` DSL — it breaks on KMP.
- **APIs/Repositories**: Use `single { }` (singleton per app lifecycle).
- **Storage**: `StorageModule.kt` declares `expect fun createLocalStorage()`. Platform actuals provide `SharedPreferences` (Android), `NSUserDefaults` (iOS), or file-based (Desktop).
- **Composables**: Always use `koinInject()` to obtain ViewModels. Never use `koinViewModel()`.

## ANTI-PATTERNS

| Forbidden | Why | Fix |
|-----------|-----|-----|
| `viewModel { }` Koin DSL | Crashes on iOS/Desktop | `factory { MyViewModel(get()) }` |
| `koinViewModel()` in Composables | KMP incompatibility | `koinInject()` |
| `single { }` for ViewModels | VMs survive config changes / screen disposal | Use `factory { }` |
| Forgetting `includes()` in `AppModule.kt` | Module never loaded, runtime DI crash | Register every new module |

## NOTES

- **14 sub-modules** aggregated in `AppModule.kt`. New modules MUST be added there.
- **ViewModelModule.kt** only registers 3 ViewModels. Most ViewModels are registered in their respective feature modules (e.g., `ExamModule` registers `ExamViewModel`).
- **ExtendedModules.kt** is a grab-bag for `questionBankModule`, `fileModule`, and `aiGradingModule`. Consider splitting into separate files if they grow.
- **StorageModule** uses `expect/actual` pattern for `createLocalStorage()`. Platform modules (`StorageModule.android.kt`, etc.) provide the actual implementation.
- **No `viewModel { }` usage anywhere** — the project correctly avoids this KMP-incompatible DSL.