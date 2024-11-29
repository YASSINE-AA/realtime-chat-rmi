#!/bin/bash

BUILD_DIR="bin"
SERVER_CLASS="server.ChatServerMain"
CLIENT_CLASS="client.ChatClientMain"
JAR_DIR="lib"
JAR_LANTERNA="$JAR_DIR/lanterna.jar"
JAR_SQLITE="$JAR_DIR/sqlite.jar"

OUTPUT_JAR="Client.jar"

usage() {
    echo "Usage: $0 {clean|build|server|client|run}"
    exit 1
}

if [[ -z $1 ]]; then
    echo "Error: No command provided."
    usage
fi

case $1 in
    clean)
        echo "Cleaning up build directory..."
        rm -rf "$BUILD_DIR" "$OUTPUT_JAR"
        echo "Build directory cleaned."
        ;;
    build)
        echo "Building Java project..."

        mkdir -p "$BUILD_DIR"

        javac -cp "$JAR_LANTERNA:$JAR_SQLITE" -d "$BUILD_DIR" src/*/*.java
        if [[ $? -ne 0 ]]; then
            echo "Build failed. Please check the source files for errors."
            exit 2
        fi

        echo "Creating MANIFEST.MF..."
        mkdir -p "$BUILD_DIR/META-INF"
        cat > "$BUILD_DIR/META-INF/MANIFEST.MF" <<EOL
Manifest-Version: 1.0
Main-Class: $CLIENT_CLASS
Class-Path: $JAR_LANTERNA $JAR_SQLITE
EOL
        echo "MANIFEST.MF created."

        echo "Creating runnable JAR..."
        jar -cvfm "$OUTPUT_JAR" "$BUILD_DIR/META-INF/MANIFEST.MF" -C "$BUILD_DIR" .
        if [[ $? -eq 0 ]]; then
            echo "Runnable JAR created successfully: $OUTPUT_JAR"
        else
            echo "Failed to create JAR."
            exit 3
        fi
        ;;
    server)
        echo "Starting server..."
        if [[ -f "$BUILD_DIR/${SERVER_CLASS//.//}.class" ]]; then
            java -cp "$BUILD_DIR:$JAR_LANTERNA:$JAR_SQLITE" "$SERVER_CLASS"
        else
            echo "Error: Server class not found. Run '$0 build' first."
            exit 3
        fi
        ;;
    client)
        echo "Starting client..."
        if [[ -f "$BUILD_DIR/${CLIENT_CLASS//.//}.class" ]]; then
            java -cp "$BUILD_DIR:$JAR_LANTERNA:$JAR_SQLITE" "$CLIENT_CLASS"
        else
            echo "Error: Client class not found. Run '$0 build' first."
            exit 3
        fi
        ;;
    run)
        if [[ -f "$OUTPUT_JAR" ]]; then
            echo "Running JAR..."
            java -jar "$OUTPUT_JAR"
        else
            echo "Error: JAR file not found. Run '$0 build' first."
            exit 4
        fi
        ;;
    *)
        echo "Error: Unknown command '$1'."
        usage
        ;;
esac
