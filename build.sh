#!/bin/bash

BUILD_DIR="build"
SERVER_CLASS="server.ChatServerMain"
CLIENT_CLASS="client.ChatClientMain"

usage() {
    echo "Usage: $0 {clean|build|server|client}"
    exit 1
}

if [[ -z $1 ]]; then
    echo "Error: No command provided."
    usage
fi

case $1 in
    clean)
        echo "Cleaning up build directory..."
        rm -rf "$BUILD_DIR"
        echo "Build directory cleaned."
        ;;
    build)
        echo "Building Java project..."
        mkdir -p "$BUILD_DIR"
        javac -d "$BUILD_DIR" */*.java
        if [[ $? -eq 0 ]]; then
            echo "Build successful. Classes saved in '$BUILD_DIR'."
        else
            echo "Build failed. Please check the source files for errors."
            exit 2
        fi
        ;;
    server)
        echo "Starting server..."
        if [[ -d "$BUILD_DIR" ]]; then
            cd "$BUILD_DIR" || exit
            java "$SERVER_CLASS"
        else
            echo "Error: Build directory not found. Run '$0 build' first."
            exit 3
        fi
        ;;
    client)
        echo "Starting client..."
        if [[ -d "$BUILD_DIR" ]]; then
            cd "$BUILD_DIR" || exit
            java "$CLIENT_CLASS"
        else
            echo "Error: Build directory not found. Run '$0 build' first."
            exit 3
        fi
        ;;
    *)
        echo "Error: Unknown command '$1'."
        usage
        ;;
esac
