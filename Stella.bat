@echo off
REM Script para executar o jogo Stella

if exist Stella.jar (
    java -jar Stella.jar
) else (
    echo Erro: Stella.jar não encontrado!
    echo Execute "build.bat" primeiro para compilar o jogo.
    pause
)
