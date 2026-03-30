@echo off
setlocal

:: --- CONFIGURATION ---
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set DB_USER=root
:: Tip: Leave DB_PASS blank here and it will prompt you, 
:: or fill it in for 100% automation (e.g., set DB_PASS=YourPassword)
set /p DB_PASS=Enter password for %DB_USER%: 

:: Create a temporary SQL file
set TEMP_SQL=%TEMP%\cleanup_script.sql

:: --- GENERATE SQL ---
(
    echo SET FOREIGN_KEY_CHECKS=0;
    
    echo TRUNCATE TABLE auth_db.refresh_tokens;
    echo TRUNCATE TABLE auth_db.user_roles;
    echo TRUNCATE TABLE auth_db.users;
    
    echo TRUNCATE TABLE admin_db.admin_actions;
    
    echo TRUNCATE TABLE policy_db.policies;
    
    echo TRUNCATE TABLE claims_db.claim_documents;
    echo TRUNCATE TABLE claims_db.claims;
    
    echo SET FOREIGN_KEY_CHECKS=1;
) > "%TEMP_SQL%"

echo Starting database cleanup for user: %DB_USER%...

:: --- EXECUTION ---
:: Note: No space between -p and %DB_PASS%
%MYSQL_PATH% -u %DB_USER% -p%DB_PASS% < "%TEMP_SQL%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] All tables truncated successfully.
) else (
    echo.
    echo [ERROR] Cleanup failed. Error Code: %ERRORLEVEL%
    echo Check if:
    echo 1. The password is correct.
    echo 2. MySQL path is accurate.
    echo 3. The databases (auth_db, admin_db, etc.) actually exist.
)

:: Clean up temp file
if exist "%TEMP_SQL%" del "%TEMP_SQL%"

pause