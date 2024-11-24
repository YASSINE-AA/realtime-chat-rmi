#!/bin/bash

if [[ $1 == "clean" ]]
then
    rm -rf "build"
elif [[ $1 == "build" ]]
then
    javac -d "build" *.java
else
    echo "No command found"
fi
