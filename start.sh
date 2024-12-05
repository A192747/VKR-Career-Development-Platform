#!/bin/bash
CURRENT_DIR=${PWD}

cd Docker || exit
docker-compose down
docker-compose up -d
cd "$CURRENT_DIR" || exit
