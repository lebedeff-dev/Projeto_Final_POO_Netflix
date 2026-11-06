#!/usr/bin/env bash
# =====================================================================
#  Gera o arquivo Netflix.jar (Linux / macOS). Mesmo passo a passo do build.bat.
#  Requer o JDK instalado (comandos javac e jar).
# =====================================================================

cd "$(dirname "$0")" || exit 1

DRIVER="lib/mysql-connector-j-9.3.0.jar"
FORMS="lib/forms_rt.jar"

if ! command -v javac >/dev/null 2>&1; then
    echo "ERRO: javac nao encontrado. Instale o JDK (Java 11 ou superior) e tente de novo."
    exit 1
fi

rm -rf build
mkdir -p build/classes

# 1) Lista os .java   2) Compila
find src -name "*.java" > build/fontes.txt
echo "Compilando..."
if ! javac -encoding UTF-8 --release 11 -cp "$DRIVER:$FORMS" -d build/classes @build/fontes.txt; then
    echo "ERRO na compilacao. Veja as mensagens acima."
    exit 1
fi

# 3) Script SQL dentro do jar   4) Driver do MySQL e runtime do GUI Designer (forms_rt) dentro do jar
cp sql/netflix.sql build/classes/netflix.sql
(cd build/classes && jar xf "../../$DRIVER" && jar xf "../../$FORMS" && rm -f META-INF/MANIFEST.MF META-INF/INDEX.LIST __index__)

# 5) Monta o jar dizendo qual classe tem o main
echo "Main-Class: Main" > build/manifest.txt
jar cfm ../Netflix.jar build/manifest.txt -C build/classes .

rm -rf build
echo "Pronto! Gerado: Netflix.jar"
