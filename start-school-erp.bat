@echo off
REM ============================================
REM  School ERP - One-click Start
REM  Starts backend (Spring Boot) and frontend
REM  (Vite/React) each in their own window, using
REM  the dedicated run-backend.bat / run-frontend.bat
REM  scripts sitting next to this file.
REM ============================================

echo Starting backend (Spring Boot)...
start "School ERP - Backend" cmd /k "%~dp0run-backend.bat"

echo Waiting a few seconds before starting frontend...
timeout /t 5 /nobreak > nul

echo Starting frontend (Vite dev server)...
start "School ERP - Frontend" cmd /k "%~dp0run-frontend.bat"

echo.
echo Both servers are starting in separate windows.
echo Backend:  http://localhost:8081
echo Frontend: http://localhost:5173
echo.
pause


