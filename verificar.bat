@echo off
REM =====================================================================
REM  Sistema de Tickets (Spring Boot) - Verificar el arranque
REM =====================================================================
REM  Compila el proyecto y comprueba que la aplicacion responde en los
REM  dos endpoints de diagnostico. Deja la salida en .verificacion\
REM
REM  Uso: primero arrancar en otra consola con ejecutar.bat
REM =====================================================================
setlocal

cd /d "%~dp0"

set "BASE=http://localhost:4000"
set "SALIDA=.verificacion"
set "FALLOS=0"

if not exist "%SALIDA%" mkdir "%SALIDA%"

echo.
echo === Verificacion del backend Spring Boot ===
echo.

REM --- 1. Compilar ------------------------------------------------------
echo [1/3] Compilando...
call mvn -q -B compile > "%SALIDA%\compilacion.txt" 2>&1
if errorlevel 1 (
    echo       FALLO: la compilacion devolvio errores.
    echo       Ver %SALIDA%\compilacion.txt
    set "FALLOS=1"
    goto :resumen
)
echo       OK. El codigo compila.
echo.

REM --- 2. Endpoints de diagnostico --------------------------------------
echo [2/3] Consultando %BASE% ...
echo.

call :probar "/vivo"  "vivo.txt"  "El proceso esta vivo"
call :probar "/salud" "salud.txt" "Dependencias al dia"
call :probar "/"      "raiz.txt"  "Resumen del servicio"

echo.
echo [3/3] Resumen
echo.
if "%FALLOS%"=="1" (
    echo       Hay fallos. Revisar la carpeta %SALIDA%.
) else (
    echo       Todo correcto.
)
echo.

:resumen
echo.
echo Salida guardada en la carpeta %SALIDA%
echo.
endlocal
exit /b %FALLOS%

REM ---------------------------------------------------------------------
REM  :probar  <ruta>  <archivo>  <descripcion>
REM  Consulta un endpoint y comprueba que devuelva HTTP 200.
REM  Nota: se escribe la salida en un archivo propio en lugar de usar
REM  /dev/null, porque en Git Bash /dev/null no funciona y curl informa
REM  "0 bytes" aunque el servidor si haya respondido.
REM ---------------------------------------------------------------------
:probar
set "RUTA=%~1"
set "ARCHIVO=%~2"
set "DESC=%~3"

curl -s -o "%SALIDA%\%ARCHIVO%" -w "%%{http_code}" "%BASE%%RUTA%" > "%SALIDA%\codigo.txt" 2>&1
set /p CODIGO=<"%SALIDA%\codigo.txt"

if "%CODIGO%"=="200" (
    echo       [OK]  %RUTA%  - %DESC%
) else (
    echo       [!!]  %RUTA%  - HTTP %CODIGO%
    echo             ^(quizas la aplicacion no esta arrancada^)
    set "FALLOS=1"
)
exit /b 0
