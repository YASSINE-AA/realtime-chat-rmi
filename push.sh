#!/bin/bash

if [[ $1 == "init" ]]
then
    git init
    git branch -M main
    git remote add origin git@github.com:YASSINE-AA/realtime-chat-rmi.git
else
    git add .
    git commit -m $1
    git push -uf origin main
fi
