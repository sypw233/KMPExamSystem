$files = @(
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/auth/LoginScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/auth/RegisterScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/DashboardScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/CoursesScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/ExamsScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/ExamTakingScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/GradeHistoryScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/NotificationScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/ProfileScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/AdminDashboardScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/QuestionBankScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/SystemSettingsScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/UserManageScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/TeacherExamManageScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/ExamComposeScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/ExamSubmissionsScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/GradeSubmissionScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/student/GradeDetailScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/test/ApiTestScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/test/FileTestScreen.kt",
  "F:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/test/ImageTestScreen.kt"
)

foreach ($file in $files) {
    $content = Get-Content $file -Raw
    
    # 1. Add imports if missing
    if ($content -notmatch "import ovo\.sypw\.kmp\.examsystem\.utils\.LocalResponsiveConfig") {
        # Find a utils import to insert after
        if ($content -match "import ovo\.sypw\.kmp\.examsystem\.utils\.\w+") {
            $lastUtilsImport = [regex]::Matches($content, "import ovo\.sypw\.kmp\.examsystem\.utils\.\w+") | Select-Object -Last 1
            if ($lastUtilsImport) {
                $utilsLine = $lastUtilsImport.Value
                $newImports = "$utilsLine`nimport ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig`nimport ovo.sypw.kmp.examsystem.utils.ResponsiveUtils"
                $content = $content -replace [regex]::Escape($utilsLine), $newImports
            }
        } else {
            # No utils import exists, add after last import
            $lastImport = [regex]::Matches($content, "^import .+", [System.Text.RegularExpressions.RegexOptions]::Multiline) | Select-Object -Last 1
            if ($lastImport) {
                $importLine = $lastImport.Value
                $newImports = "$importLine`nimport ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig`nimport ovo.sypw.kmp.examsystem.utils.ResponsiveUtils"
                $content = $content -replace [regex]::Escape($importLine), $newImports
            }
        }
    }
    
    # 2. Add val config after the first var/val declaration in the composable function
    if ($content -notmatch "val config = LocalResponsiveConfig\.current") {
        # Find first val/var after fun XxxScreen
        $pattern = "(fun \w+Screen\(.*\n)(\s+)(val|var)\s+"
        $match = [regex]::Match($content, $pattern)
        if ($match.Success) {
            $indent = $match.Groups[2].Value
            $replacement = "$($match.Groups[1].Value)${indent}val config = LocalResponsiveConfig.current`n${indent}$($match.Groups[3].Value) "
            $content = $content -replace [regex]::Escape($match.Value), $replacement
        }
    }
    
    # 3. Replace .widthIn(max = X.dp) with conditional version
    $content = [regex]::Replace($content, "\.widthIn\(max = (\d+)\.dp\)", {
        $val = $args[0].Groups[1].Value
        return ".then(`n                        if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {`n                            Modifier.widthIn(max = ${val}.dp)`n                        } else Modifier`n                    )"
    })
    
    Set-Content $file -Value $content -NoNewline
    Write-Host "Processed: $file"
}
