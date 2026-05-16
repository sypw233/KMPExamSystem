@echo off
chcp 65001 >nul
echo 正在启动 KMP 考试系统桌面版...
cd /d "F:\androidProject\KMPExamSystem"
java -jar composeApp\build\libs\composeApp-desktop.jar
echo.
echo 应用已关闭。
pause
