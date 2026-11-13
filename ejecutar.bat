@echo off
REM ============================================================
REM  One Joyeria - arranca la aplicacion con el Maven incluido
REM  Doble clic en este archivo, o en la terminal:  ejecutar.bat
REM ============================================================
cd /d "%~dp0"
title One Joyeria

set "MAVEN=%~dp0..\apache-maven-3.10.0\bin\mvn.cmd"
if not exist "%MAVEN%" (
    echo No se encontro Maven en "%~dp0..\apache-maven-3.10.0".
    echo Debe estar en la carpeta de al lado de OneJoyeria_Java.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 if "%JAVA_HOME%"=="" (
    echo No se encontro Java. Instala el JDK 21 desde https://adoptium.net
    echo y marca la opcion "Set JAVA_HOME" durante la instalacion.
    pause
    exit /b 1
)

echo Recuerda levantar MySQL antes:  docker compose up -d
echo Iniciando One Joyeria... la primera vez tarda unos minutos.
echo Cuando aparezca "Started OneJoyeriaApplication", la API estara en http://localhost:8089/api
echo Para detenerlo presiona Ctrl + C.
echo.
call "%MAVEN%" spring-boot:run
pause
