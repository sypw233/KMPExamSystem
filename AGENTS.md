# PROJECT KNOWLEDGE BASE

**Generated:** 2026-04-26
**Commit:** e27b7c8
**Branch:** master

## OVERVIEW

Kotlin Multiplatform exam system targeting Android, iOS, and Desktop (JVM). Single-module Gradle project (`composeApp`). All shared code under `composeApp/src/commonMain`. 119 shared Kotlin files, ~17k lines.

## STRUCTURE

```
composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/
├── data/           # API, DTOs, repositories, storage (44 files)
├── domain/         # Business models (thin — only 6 files)
├── presentation/   # Screens, viewmodels, navigation (46 files)
├── di/             # Koin modules (13 files)
├── utils/          # Responsive, logging, dialogs (7 files)
└── App.kt          # Central composable root
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Add new API endpoint | `data/api/` | Extend `BaseApiService`, add method to `*Api.kt` |
| Add new DTO | `data/dto/` | Use `@Serializable`, mirror backend contract |
| Add new repository | `data/repository/` | Coordinate API calls, expose `StateFlow` |
| Add new screen | `presentation/screens/` | Group by role: `auth/`, `admin/`, `student/`, `teacher/` |
| Add new ViewModel | `presentation/viewmodel/` | Extend `com.hoc081098.kmp.viewmodel.ViewModel` |
| Add navigation route | `presentation/navigation/` | Update `AppRoutes`, `NavigationScreen.kt`, `NavigationItem.kt` |
| Register DI | `di/` | Add to feature module, then `includes()` in `AppModule.kt` |
| Platform-specific code | `androidMain/`, `iosMain/`, `desktopMain/` | Each has 8 files, symmetric structure |

## CODE MAP

| Symbol | Type | Location | Role |
|--------|------|----------|------|
| `App` | `@Composable` | `App.kt` | Central root — all platforms converge here |
| `PlatformKoinApplication` | `expect/actual` | `App.*.kt` | Koin init inside Composable (non-standard) |
| `BaseApiService` | `abstract class` | `data/api/BaseApiService.kt` | HTTP helpers + `safeApiCall()` |
| `SaResult` | `@Serializable data class` | `data/dto/result/SaResult.kt` | Unified API response wrapper |
| `NavigationManager` | `class` | `presentation/navigation/NavigationManager.kt` | Route + role + exam-mode state |
| `NavigationScreen` | `@Composable` | `presentation/navigation/NavigationScreen.kt` | Route dispatcher |
| `appModule` | `Koin module` | `di/AppModule.kt` | Aggregates 14 sub-modules |

## CONVENTIONS

- **Language**: All comments, UI strings, docs in **Simplified Chinese**. Half-width punctuation only.
- **State management**: ViewModel exposes `MutableStateFlow`, Composables collect with `collectAsState()`. UI state as sealed interface with `Idle`/`Loading`/`Success`/`Error` variants.
- **Responsive layout**: Desktop screens use `Modifier.widthIn(max = ...)`. `getResponsiveLayoutConfig()` — mobile = bottom nav, desktop = side rail.
- **Theme**: Material Design 3, primary `#006495` (light) / `#8DCDFF` (dark). Use `MaterialTheme.colorScheme`.
- **DI**: `factory { }` for ViewModels, `single { }` for repositories and API services. Never `viewModel { }` DSL. In Composables use `koinInject()`, never `koinViewModel()`.

## ANTI-PATTERNS (THIS PROJECT)

| Forbidden | Why | What to use instead |
|-----------|-----|---------------------|
| `viewModel { }` Koin DSL | KMP compatibility issues | `factory { MyViewModel(get()) }` |
| `koinViewModel()` in Composables | Breaks on KMP | `koinInject()` |
| Wrong `ViewModel` import | Must use KMP-compatible base | `com.hoc081098.kmp.viewmodel.ViewModel` |
| `!!` non-null assertions | Runtime crash risk on any platform | Safe calls `?.`, `?: return`, `.let` |
| `println()` in shared code | Breaks cross-platform logging abstraction | `Logger` (expect/actual) |

## UNIQUE STYLES

- **`expect/actual PlatformKoinApplication`** — Koin initialization happens inside a Composable function, not in platform `main()`. Android wraps in `Scaffold` with system insets; Desktop/iOS do not.
- **Role-based screen grouping** — Screens organized by user role (`admin/`, `teacher/`, `student/`, `auth/`) rather than by feature. Top-level screens (Dashboard, Courses, Exams) are flat in `screens/`.
- **`backDto/` at project root** — 28 standalone backend DTO files (not compiled into app) serve as API contract reference.
- **In-app API testing** — `presentation/screens/test/` contains 3 Composable debug screens (`ApiTestScreen`, `FileTestScreen`, `ImageTestScreen`) for manual endpoint testing.
- **Custom navigation** — `NavigationManager` uses `mutableStateOf` (not Voyager/Decompose). Role-based routing with exam-mode lock.

## COMMANDS

```bash
./gradlew :composeApp:assembleDebug          # Android debug APK
./gradlew :composeApp:assembleRelease         # Android release APK
./gradlew :composeApp:run                     # Desktop JVM
./gradlew :composeApp:iosArm64Binaries        # iOS framework
./gradlew :composeApp:test                    # Run common tests (only 1 test exists)
./gradlew :composeApp:packageDmg              # macOS DMG
./gradlew :composeApp:packageMsi              # Windows MSI
./gradlew :composeApp:packageDeb              # Linux DEB
```

Requires JDK 21. CI triggers on tag push (`v*`) via `.github/workflows/build-all-platforms.yml`.

## NOTES

- **Base URL**: Hardcoded `http://localhost:8080` in `HttpClientConfig.kt`. Android emulator must manually use `10.0.2.2`.
- **Android cleartext**: `AndroidManifest.xml` has `usesCleartextTraffic="true"` for local HTTP dev.
- **Desktop DPI scaling**: `main.kt` applies `0.8f` density multiplier on Desktop.
- **iOS `LocalStorage.clear()` is empty** — does not remove NSUserDefaults keys. Bug.
- **iOS dispatcher**: `LocalStorage.ios.kt` uses `Dispatchers.Main` for all storage ops; should be `Dispatchers.Default`.
- **No lint configs**: No `.editorconfig`, detekt, or ktlint configured.
- **Tests minimal**: Only 1 trivial test in `commonTest/`; no platform-specific test source sets.
- **AGENTS.md child docs**: See `data/AGENTS.md`, `presentation/screens/AGENTS.md`, `di/AGENTS.md` for layer-specific conventions.

## Reference Docs

- `AI_PROJECT_SPEC.md` — Full project spec with architecture details
- `KMP_DI_Guide.md` — DI setup guide
- `HTTP_Debug_Guide.md` — Network debugging
- `frontend_integration_guide.md` — API integration reference
- `API_ENDPOINTS.md` — Complete API endpoint map
