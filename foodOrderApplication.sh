#!/bin/bash

# Set the path to your JAR file (using Unix-style path for Git Bash)
JAR_PATH="/c/Users/Admin/Desktop/Office/cake order system/out/artifacts/foodOrderSystem_jar/foodOrderSystem.jar"

# Set the path to the JavaFX SDK (using Unix-style path)
JAVA_FX_PATH="/c/Program Files/java/javafx-sdk-23.0.1"  # Use double quotes around paths with spaces

# Set the path to your JDK (using Unix-style path for Git Bash)
JAVA_HOME="/c/Program Files/Java/jdk-23"  # Use double quotes around paths with spaces

# Build the module path for JavaFX SDK by listing all JAR files in the lib folder
MODULE_PATH=""
for jar in "$JAVA_FX_PATH/lib"/*.jar; do
    MODULE_PATH="$MODULE_PATH:$jar"
done

# Remove the initial ':' from MODULE_PATH if it exists
MODULE_PATH=${MODULE_PATH#:}

# Run the application using Java with JavaFX
"$JAVA_HOME/bin/java" \
  --module-path "$MODULE_PATH" \
  --add-modules javafx.controls,javafx.fxml,javafx.web \
  -jar "$JAR_PATH"