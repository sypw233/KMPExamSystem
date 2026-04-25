# SCREEN LAYER KNOWLEDGE BASE

**Scope:** `presentation/screens/` and subdirectories
**Files:** 23 flat + 15 in subdirs (teacher:5, admin:4, test:3, auth:2, student:1)

## STRUCTURE

```
presentation/screens/
├── admin/              # 4 files — AdminDashboard, QuestionBank, SystemSettings, UserManage
├── auth/               # 2 files — LoginScreen, RegisterScreen
├── student/            # 1 file — GradeDetail
├── teacher/            # 5 files — ExamCompose, ExamSubmissions, GradeSubmission, QuestionManage, TeacherExamManage
├── test/               # 3 files — ApiTest, FileTest, ImageTest (debug screens)
├── CoursesScreen.kt    # Flat — course management
├── DashboardScreen.kt  # Flat — dashboard with widgets
├── ExamsScreen.kt      # Flat — exam list
├── ExamTakingScreen.kt # Flat — active exam UI
├── GradeHistoryScreen.kt
├── HomeScreen.kt
├── NotificationScreen.kt
├── ProfileScreen.kt    # Flat — profile + sub-screen routing
└── SettingsScreen.kt
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Add admin screen | `screens/admin/` | Use `AdminDashboardScreen` as template |
| Add teacher screen | `screens/teacher/` | Use `TeacherExamManageScreen` as template |
| Add auth screen | `screens/auth/` | Wrap with auth-gated navigation in `App.kt` |
| Add debug screen | `screens/test/` | Register in `NavigationScreen.kt` behind dev flag |
| Add top-level screen | `screens/` flat | Add route in `NavigationItem.kt` + `NavigationScreen.kt` |
| Fix responsive layout | Any screen | Desktop: `Modifier.widthIn(max = ...)`, use `getResponsiveLayoutConfig()` |

## CONVENTIONS

- **Role grouping**: Screens are grouped by user role (`admin/`, `teacher/`, `student/`, `auth/`), not by feature. Top-level screens (Dashboard, Courses, Exams) are flat in `screens/`.
- **State collection**: All screens collect `StateFlow` via `collectAsState()` from `koinInject()` ViewModels.
- **Navigation callbacks**: Pass `onNavigateToXxx` lambda params from `NavigationScreen.kt` — do not reference `NavigationManager` directly in screens.
- **Dialogs**: Use `DialogManager` (injected) for global dialogs. Inline `AlertDialog` for screen-local confirmations.
- **Material3**: Use `ExperimentalMaterial3Api` opt-in widely. `TopAppBar`, `NavigationBar`, `FloatingActionButton` follow M3 specs.
- **Images**: Use Coil `AsyncImage` for remote images. `rememberAsyncImagePainter` for custom painter needs.

## ANTI-PATTERNS

| Forbidden | Why | Fix |
|-----------|-----|-----|
| `koinViewModel()` | Breaks on KMP | Use `koinInject()` |
| `!!` assertions | Runtime crash risk | Use `?.let` or `?: return` |
| Screens >500 lines | Hard to maintain, violates SRP | Split into `*ListScreen` + `*FormScreen` |
| Direct `NavigationManager` access in screen | Tight coupling | Receive navigation callbacks via params |

## HOTSPOTS (Refactoring Candidates)

| File | Lines | Issue | Recommendation |
|------|-------|-------|----------------|
| `TeacherExamManageScreen.kt` | 784 | Largest file; mixes list + form + actions | Split into `TeacherExamListScreen` + `TeacherExamFormScreen` |
| `QuestionBankScreen.kt` | 729 | Bank CRUD + question assignment | Split bank management from question linking |
| `UserManageScreen.kt` | 704 | User CRUD + role filtering | Extract `UserFilterPanel` composable |
| `CoursesScreen.kt` | 689 | Course list + enrollment | Extract `CourseEnrollmentDialog` |
| `ProfileScreen.kt` | 614 | 9 composables; acts as mini-router | Make grades/settings true routed destinations |

## NOTES

- **Test screens** (`test/`) are shipped in production builds. Consider guarding behind `BuildConfig.DEBUG` equivalent.
- **Exam mode**: `NavigationManager.isInExamMode` blocks navigation during active exams. `ExamTakingScreen` handles this.
- **Bottom nav vs side rail**: `NavigationBar.kt` provides both. Mobile uses bottom, desktop uses side rail.
- **FileKit**: Cross-platform file picker used in `FileTestScreen` and avatar upload flows.