# AGENTS.md

## Project Overview

Kotlin Multiplatform exam system targeting Android, iOS, and Desktop (JVM). Single-module Gradle project (`composeApp`). All shared code under `composeApp/src/commonMain`.

## Build & Run

```bash
./gradlew :composeApp:assembleDebug          # Android APK
./gradlew :composeApp:run                     # Desktop (JVM)
./gradlew :composeApp:iosArm64Binaries        # iOS framework
./gradlew :composeApp:test                    # Run common tests
./gradlew :composeApp:packageDmg              # macOS installer
./gradlew :composeApp:packageMsi              # Windows installer
./gradlew :composeApp:packageDeb              # Linux installer
```

Requires JDK 21. CI builds trigger on tag push (`v*`) via `.github/workflows/build-all-platforms.yml`.

## Architecture

MVVM + Clean Architecture under `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/`:

- `data/api/` — Ktor HTTP clients, each extending `BaseApiService`. Config in `HttpClientConfig.kt`.
- `data/dto/` — `@Serializable` request/response classes.
- `data/repository/` — Repositories coordinate API calls and expose `StateFlow`.
- `data/storage/` — Local persistence (token storage etc.).
- `domain/` — Business models and state sealed interfaces.
- `presentation/screens/` — Composable screens, organized by role: `auth/`, `admin/`, `student/`, `teacher/`.
- `presentation/viewmodel/` — ViewModels extending `com.hoc081098.kmp.viewmodel.ViewModel`.
- `presentation/navigation/` — `NavigationManager` with role-based routing.
- `di/` — Koin modules. `AppModule.kt` aggregates all sub-modules via `includes()`.

Platform entry points: `App.desktop.kt` (Desktop), `App.android.kt` (Android), both implement `PlatformKoinApplication`.

## Koin DI — Non-obvious Rules

**Do NOT use `viewModel { }` DSL** — it has KMP compatibility issues. Always use `single {}`:

```kotlin
single { LoginViewModel(get()) }   // correct
viewModel { LoginViewModel(get()) } // wrong
```

**Do NOT use `koinViewModel()`** in Composables. Use `koinInject()`:

```kotlin
val vm: LoginViewModel = koinInject()  // correct
val vm: LoginViewModel = koinViewModel() // wrong — breaks on KMP
```

New modules must be registered in `di/AppModule.kt` via `includes()`.

## API / Network

- Base URL hardcoded in `HttpClientConfig.kt`: `http://localhost:8080`
- Android emulator: the base URL must be changed to `10.0.2.2` (not automatic).
- All API responses wrap in `SaResult` (parsed in `BaseApiService.safeApiCall`). Business success code is `200`.
- Auth tokens are passed via `Authorization: Bearer <token>` header.

## Code Conventions

- **Language**: All comments, UI strings, and docs in **Simplified Chinese**. Use half-width punctuation only.
- **State management**: ViewModel exposes `MutableStateFlow`, Composables collect with `collectAsState()`. Define UI state as sealed interface with `Idle`/`Loading`/`Success`/`Error` variants.
- **Responsive layout**: Desktop screens use `Modifier.widthIn(max = ...)` to prevent full-width stretch. Layout adapts via `getResponsiveLayoutConfig()` — mobile gets bottom nav, desktop gets side rail.
- **Theme**: Material Design 3, primary color `#006495` (light) / `#8DCDFF` (dark). Use `MaterialTheme.colorScheme` for colors.

## Feature Development Workflow

1. Create `@Serializable` DTOs in `data/dto/`
2. Add API method in `data/api/` extending `BaseApiService`
3. Create repository in `data/repository/`
4. Create ViewModel in `presentation/viewmodel/`
5. Register in `di/` module, then add to `AppModule.kt`
6. Build Composable screen in `presentation/screens/`
7. Add route in `presentation/navigation/`

## Dependencies

All versions centralized in `gradle/libs.versions.toml`. Key libs: Ktor 3.x, Koin 4.x, kmp-viewmodel 0.8.0, Coil 3.x, Compose Multiplatform 1.9.x, Kotlin 2.2.x.

## Reference Docs

- `AI_PROJECT_SPEC.md` — Full project spec with architecture details
- `KMP_DI_Guide.md` — DI setup guide
- `HTTP_Debug_Guide.md` — Network debugging
- `frontend_integration_guide.md` — API integration reference
