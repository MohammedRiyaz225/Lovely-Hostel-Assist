@echo off
title HostelFix Backend Server
echo ========================================================
echo    HostelFix Python FastAPI Backend & Warden Portal
echo ========================================================
echo.

cd /d "%~dp0"

set "PYTHONHOME=C:\Users\princ\AppData\Local\Programs\Python\Python311"
set "PYTHONPATH="
set "PATH=%PYTHONHOME%;%PYTHONHOME%\Scripts;%PATH%"

python.exe run.py

pause
