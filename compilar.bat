@echo off
REM =====================================================================
REM  Sistema de Tickets (Spring Boot) - Compilar y empaquetar
REM =====================================================================
REM  Genera target\sistema-tickets-spring-1.0.0.jar (fat jar ejecutable)
REM =====================================================================
setlocal

cd /d "%~dp0"

echo.
echo === Compilando el backend Spring Boot ===
echo.

call mvn clean package
if errorlevel 1 (
    echo.
    echo ERROR: la compilacion fallo.
    echo Revisar docs\COMO-EJECUTAR.md, seccion 8 (problemas frecuentes).
    echo.
    goto :fin
)

echo.
echo Listo. El jar esta en:
echo   target\sistema-tickets-spring-1.0.0.jar
echo.
echo Para ejecutarlo sin Maven:
echo   java -jar target\sistema-tickets-spring-1.0.0.jar
echo.

:fin
endlocal
