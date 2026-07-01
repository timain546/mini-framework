#!/bin/bash

# Nom du JAR de sortie
JAR_NAME="mini-framework-v5.jar"

# Définition des dossiers
SRC_DIR="src"
BIN_DIR="bin"
LIB_DIR="lib"

# 1. Nettoyage des anciennes compilations
echo "=== Nettoyage ==="
rm -rf "$BIN_DIR"
rm -f "$JAR_NAME"
mkdir "$BIN_DIR"

# 2. Gestion du Classpath (inclusion de toutes les dépendances dans lib/, ex: servlet-api.jar)
echo "=== Préparation du Classpath ==="
CLASSPATH=""
if [ -d "$LIB_DIR" ]; then
    for jar in "$LIB_DIR"/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

# 3. Compilation des fichiers Java
echo "=== Compilation ==="
# On cherche tous les fichiers .java et on les compile vers le dossier bin/
find "$SRC_DIR" -name "*.java" > sources.txt

if [ -z "$CLASSPATH" ]; then
    javac -d "$BIN_DIR" @sources.txt
else
    javac -cp "$CLASSPATH" -d "$BIN_DIR" @sources.txt
fi

# Vérification si la compilation a réussi
if [ $? -ne 0 ]; then
    echo "Erreur de compilation !"
    rm sources.txt
    exit 1
fi
rm sources.txt

# 4. Création du fichier .jar
echo "=== Création du fichier JAR ==="
jar -cvf "$JAR_NAME" -C "$BIN_DIR" .

echo "=== Fin du Build ! Fichier généré : $JAR_NAME ==="