@echo off
REM =====================================================================
REM  Sistema de Tickets (Spring Boot) - Arrancar la aplicacion
REM =====================================================================
REM  Levanta el backend en http://localhost:4000
REM  Detenerlo con Ctrl + C
REM =====================================================================
setlocal

cd /d "%~dp0"

echo.
echo === Sistema de Tickets - backend Spring Boot ===
echo.

REM --- Comprobar el JDK -----------------------------------------------
REM  Spring Boot 3.x exige Java 17. Si el equipo tiene Java 8, el
REM  arranque falla con "invalid target release".
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%v"
echo JDK detectado: %JAVA_VER%
echo.

REM --- Arrancar ---------------------------------------------------------
call mvn spring-boot:run
if errorlevel 1 (
    echo.
    echo ERROR: no se pudo arrancar la aplicacion.
    echo Revisar docs\COMO-EJECUTAR.md, seccion 8 (problemas frecuentes).
    echo.
)

endlocal
