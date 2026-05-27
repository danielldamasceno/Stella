@echo off
REM Script completo para compilar e gerar Stella.exe

echo ===== Compilando código-fonte =====
javac -encoding UTF-8 -d bin src\com\stella\core\*.java src\com\stella\assets\*.java src\com\stella\entities\*.java src\com\stella\physics\*.java src\com\stella\player\*.java src\com\stella\world\*.java

if %errorlevel% neq 0 (
    echo Erro na compilação!
    pause
    exit /b 1
)

echo ===== Copiando recursos =====
xcopy /E /I /Y res bin\res

echo ===== Criando JAR executável =====
cd bin
jar cfm ..\Stella.jar ..\MANIFEST.MF com\stella res
cd ..

if exist Stella.jar (
    echo JAR criado com sucesso!
    echo.
    echo Para converter para EXE, é necessário Launch4j:
    echo 1. Baixe em: https://sourceforge.net/projects/launch4j/
    echo 2. Instale e abra o Launch4j
    echo 3. Abra o arquivo 'launch4j-config.xml'
    echo 4. Clique em 'Build wrappet'
    echo 5. Seu Stella.exe estará pronto!
    echo.
) else (
    echo Erro ao criar o JAR!
    pause
    exit /b 1
)

pause
