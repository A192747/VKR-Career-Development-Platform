#!/bin/bash

CURRENT_DIR=${PWD}

# Останавливаем все docker-compose
echo "Stopping docker-compose from all dirs..."
cd Docker || exit
docker-compose down
cd dev || exit
docker-compose down

cd "$CURRENT_DIR" || exit

# Если передан ключ -dev, выполняем только docker-compose для development
if [[ "$1" == "-dev" ]]; then
    echo "Starting docker-compose for development..."
    cd Docker || exit
    cd dev || exit
    docker-compose up -d
else
    if [[ "$1" == "-down-all" ]]; then
        echo "Done"
    else
	docker rmi docker-sender-app
	docker rmi docker-main-app
        echo "Starting docker-compose for full project..."
        cd Docker || exit
        docker-compose up -d
    fi
fi

cd "$CURRENT_DIR" || exit
