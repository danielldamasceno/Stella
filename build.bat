@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   BUILD - Stella
echo ============================================

REM ---- 1. Limpa build anterior ----
echo.
echo [1/6] Limpando pasta bin...
if exist bin (
    rmdir /S /Q bin
)
mkdir bin

REM ---- 2. Compila ----
echo.
echo [2/6] Compilando fontes Java...
javac -d bin -encoding UTF-8 @sources.txt
if errorlevel 1 (
    echo.
    echo [ERRO] Falha na compilacao. Build abortado.
    exit /b 1
)

REM ---- 3. Copia recursos ----
echo.
echo [3/6] Copiando recursos (src\res -^> bin\res)...
xcopy /E /I /Y src\res bin\res
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao copiar recursos. Build abortado.
    exit /b 1
)

REM ---- 4. Empacota em .jar ----
echo.
echo [4/6] Empacotando em Stella.jar...
if exist Stella.jar del /Q Stella.jar
pushd bin
jar --create --file ..\Stella.jar --main-class com.stella.core.App .
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao criar o .jar. Build abortado.
    popd
    exit /b 1
)
popd

REM ---- 5. Testa o jar ----
echo.
echo [5/6] Testando Stella.jar (feche o jogo para continuar o build)...
java -jar Stella.jar
if errorlevel 1 (
    echo.
    echo [AVISO] O jar retornou erro ao rodar. Verifique antes de gerar o .exe.
    echo Pressione qualquer tecla para continuar mesmo assim, ou feche esta janela para abortar.
    pause >nul
)

REM ---- 6. Gera o .exe (app-image) ----
echo.
echo [6/6] Gerando executavel com jpackage...
if exist dist rmdir /S /Q dist
jpackage --type app-image --name Stella --input . --main-jar Stella.jar --main-class com.stella.core.App --icon frontidle.ico --dest dist
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao gerar o .exe. Build abortado.
    exit /b 1
)

echo.
echo ============================================
echo   BUILD CONCLUIDO COM SUCESSO
echo   Executavel em: dist\Stella\Stella.exe
echo ============================================

endlocal