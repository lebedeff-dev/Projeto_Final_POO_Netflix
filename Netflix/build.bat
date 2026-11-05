@echo off
rem =====================================================================
rem  Gera o arquivo Netflix.jar (a "build" que o grupo usa).
rem  So precisa rodar de novo se alguem mudar o codigo em src\.
rem  Requer o JDK instalado (comando javac e jar).
rem =====================================================================

rem Entra na pasta deste script (funciona com duplo clique, de qualquer lugar)
cd /d "%~dp0"

set DRIVER=lib\mysql-connector-j-9.3.0.jar

where javac >nul 2>nul
if errorlevel 1 (
    echo ERRO: javac nao encontrado. Instale o JDK ^(Java 11 ou superior^) e tente de novo.
    pause
    exit /b 1
)

rem Pasta temporaria limpa a cada build
if exist build rmdir /s /q build
mkdir build\classes

rem 1) Lista todos os .java de src\ em um arquivo (entre aspas e com "/" porque
rem    o caminho tem espacos, ex.: "Projeto Final POO").
setlocal enabledelayedexpansion
for /r src %%f in (*.java) do (
    set "arq=%%f"
    echo "!arq:\=/!">> build\fontes.txt
)
endlocal

rem 2) Compila (--release 11: o jar roda em qualquer Java 11 ou mais novo)
echo Compilando...
javac -encoding UTF-8 --release 11 -cp "%DRIVER%" -d build\classes @build\fontes.txt
if errorlevel 1 (
    echo.
    echo ERRO na compilacao. Veja as mensagens acima.
    pause
    exit /b 1
)

rem 3) Coloca o script SQL dentro do jar (o CriadorBanco le de la)
copy /y sql\netflix.sql build\classes\netflix.sql >nul

rem 4) Coloca o driver do MySQL dentro do jar (assim ninguem precisa configurar classpath)
pushd build\classes
jar xf "..\..\%DRIVER%"
del /q META-INF\MANIFEST.MF META-INF\INDEX.LIST 2>nul
popd

rem 5) Monta o jar dizendo qual classe tem o main
echo Main-Class: Main> build\manifest.txt
jar cfm ..\Netflix.jar build\manifest.txt -C build\classes .
if errorlevel 1 (
    echo ERRO ao gerar o jar.
    pause
    exit /b 1
)

rmdir /s /q build
echo.
echo Pronto! Gerado: Netflix.jar
pause
