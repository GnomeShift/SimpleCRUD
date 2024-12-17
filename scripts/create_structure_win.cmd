@echo off
setlocal

set DATABASE_FILE=database
set SQL_SCRIPT=DBdemo.sql
set SQLITE_EXE=sqlite3.exe

echo Checking if %SQLITE_EXE% is in PATH...
where %SQLITE_EXE% >nul 2>&1
if %errorlevel% neq 0 (
    echo %SQLITE_EXE% not found in PATH!! Please make sure it is installed and added to your PATH environment variable!!
    pause
    exit /b 1
) else (
    echo %SQLITE_EXE% found!
)

echo Checking if %SQL_SCRIPT% script exists...
if not exist "%SQL_SCRIPT%" (
  echo SQL script file "%SQL_SCRIPT%" not found!! Please make sure it is in the same directory as %DATABASE_FILE%
  pause
  exit /b 1
) else (
    echo %SQL_SCRIPT% found!
)

echo Trying to execute %SQL_SCRIPT% script...
%SQLITE_EXE% %DATABASE_FILE% < %SQL_SCRIPT%

if %errorlevel% equ 0 (
    echo Script executed successfully!
) else (
    echo Error executing script!!
)

endlocal
pause