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

set "JAR_CMD=jar"
where jar >nul 2>&1
if errorlevel 1 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jar.exe" (
            set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
        ) else (
            echo [ERRO] jar.exe nao encontrado. Instale o JDK ou ajuste JAVA_HOME.
            exit /b 1
        )
    ) else (
        echo [ERRO] jar.exe nao encontrado no PATH e JAVA_HOME nao esta definido.
        exit /b 1
    )
)

"%JAR_CMD%" --create --file Stella.jar --main-class com.stella.core.App -C bin .
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao criar o .jar. Build abortado.
    exit /b 1
)

REM ---- 5. Testa o jar (pulando em CI) ----
echo.
if defined CI (
    echo [5/6] Ambiente CI detectado; pulando teste interativo do jar.
) else (
    echo [5/6] Testando Stella.jar (feche o jogo para continuar o build)...
    java -jar Stella.jar
    if errorlevel 1 (
        echo.
        echo [AVISO] O jar retornou erro ao rodar. Verifique antes de gerar o .exe.
        echo Pressione qualquer tecla para continuar mesmo assim, ou feche esta janela para abortar.
        pause >nul
    )
)

REM ---- 6. Gera o .exe ----
echo.
echo [6/6] Gerando executavel com jpackage...
if exist dist rmdir /S /Q dist

set "JPACKAGE_CMD=jpackage"
where jpackage >nul 2>&1
if errorlevel 1 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jpackage.exe" (
            set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"
        ) else (
            echo [ERRO] jpackage.exe nao encontrado. Instale o JDK ou ajuste JAVA_HOME.
            exit /b 1
        )
    ) else (
        echo [ERRO] jpackage.exe nao encontrado no PATH e JAVA_HOME nao esta definido.
        exit /b 1
    )
)

"%JPACKAGE_CMD%" --type exe --name Stella --input . --main-jar Stella.jar --main-class com.stella.core.App --icon frontidle.ico --dest dist --app-version 1.0
if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao gerar o .exe. Build abortado.
    exit /b 1
)

echo.
echo ============================================
echo   BUILD CONCLUIDO COM SUCESSO
for %%F in (dist\*.exe) do (
    echo Executavel gerado: %%~fF
)
echo ============================================

endlocal