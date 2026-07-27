@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   BUILD EXE - Stella
echo ============================================

REM ---- 1. Limpa build anterior ----
echo.
echo [1/5] Limpando pasta bin...
if exist bin (
    rmdir /S /Q bin
)
mkdir bin

REM ---- 2. Compila ----
echo.
echo [2/5] Compilando fontes Java...
javac -d bin -encoding UTF-8 @sources.txt
if errorlevel 1 (
    echo.
    echo [ERRO] Falha na compilacao. Build abortado.
    exit /b 1
)

REM ---- 3. Copia recursos ----
echo.
echo [3/5] Copiando recursos (src\res -> bin\res)...
xcopy /E /I /Y src\res bin\res
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao copiar recursos. Build abortado.
    exit /b 1
)

REM ---- 4. Gera o .exe ----
echo.
echo [4/5] Gerando executavel .exe com jpackage...
if exist dist rmdir /S /Q dist
jpackage --type exe --name Stella --input bin --main-jar "" --main-class com.stella.core.App --icon frontidle.ico --dest dist
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao gerar o .exe. Build abortado.
    exit /b 1
)

echo.
echo ============================================
echo   BUILD EXE CONCLUIDO
echo   Arquivo gerado em: dist\Stella\Stella.exe
echo ============================================

endlocal
