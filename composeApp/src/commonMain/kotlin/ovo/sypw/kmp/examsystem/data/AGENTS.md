# DATA LAYER KNOWLEDGE BASE

**Scope:** `data/api/`, `data/dto/`, `data/repository/`, `data/storage/`
**Files:** 44 (api:14, dto:16, repository:11, storage:3)

## STRUCTURE

```
data/
├── api/           # 14 files — Ktor HTTP services extending BaseApiService
├── dto/           # 16 files — @Serializable request/response models
├── repository/    # 11 files — Business logic + StateFlow exposure
└── storage/       # 3 files — Local persistence (expect/actual)
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Add API endpoint | `data/api/*Api.kt` | Extend `BaseApiService`, use `getWithToken`/`postWithToken` |
| Add DTO | `data/dto/` | Use `@Serializable`, add to `result/` for wrappers |
| Add repository | `data/repository/` | Inject API + Storage, expose `StateFlow` |
| Change base URL | `data/api/HttpClientConfig.kt` | Hardcoded `http://localhost:8080` |
| Fix token storage | `data/storage/TokenStorageImpl.kt` | Platform-specific actuals in `androidMain/iosMain/desktopMain` |

## CONVENTIONS

- **BaseApiService**: All APIs extend this. Provides `get/post/put/patch/delete` + `*WithToken` variants. Use `safeApiCall { }` to auto-wrap responses in `NetworkResult<SaResult>`.
- **SaResult**: Unified backend wrapper `{ code, msg, data }`. `code == 200` means success. Parse `data` field via `SaResult.parseData<T>()`.
- **NetworkResult**: Sealed class `Loading | Success<T> | Error(message)`.
- **DTOs**: All use `@Serializable`. Request DTOs end with `Request`, response DTOs end with `Response`.
- **Repositories**: Inject API service + `TokenStorage`. Expose `StateFlow` for UI state. Use `viewModelScope` in ViewModel, not in repository.
- **Storage**: `LocalStorage` is `expect/actual class`. iOS actual uses `NSUserDefaults` but `clear()` is empty (bug). Android uses `SharedPreferences`. Desktop uses file-based storage.

## ANTI-PATTERNS

| Forbidden | Why | Fix |
|-----------|-----|-----|
| `println()` in data layer | Breaks logging abstraction | Use `Logger.d()` / `Logger.e()` |
| `!!` on API response data | Runtime crash if backend contract drifts | Use `?.let` or `?: return` |
| Duplicating `WithToken` methods | BaseApiService already provides them | Use `getWithToken`/`postWithToken` from base class |
| iOS `Dispatchers.Main` for storage | Blocks main thread unnecessarily | Use `Dispatchers.Default` |

## HOTSPOTS

- **BaseApiService.kt** (437 lines) — Contains 12 nearly-identical HTTP helpers (plain + `WithToken` for each verb). Candidate for `authenticatedRequest {}` builder to eliminate duplication.
- **AuthRepository.kt** (200 lines) — Mixes auth flow + profile management. Consider splitting into `AuthRepository` + `ProfileRepository`.
- **ExamApi.kt** (253 lines, 18 endpoints) — Largest API service by endpoint count.

## NOTES

- **Timeout**: 20s connect/request/socket in `HttpClientConfig.kt`.
- **JSON**: `ignoreUnknownKeys = true` to tolerate backend field additions.
- **File uploads**: Use `safeFileApiCall` (returns `ByteArray`) for download endpoints.
- **Token refresh**: Handled in `AuthRepository.refreshToken()`, not automatic on 401.