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
container_name="ollama"
image="ollama/ollama"
command_to_run="ollama run llama3.1:8b-instruct-q4_K_M"

if [[ "$1" == "-dev" ]]; then
    echo "Starting docker-compose for development..."

    # Проверяем, существует ли контейнер
    container_id=$(docker ps -a -q -f name=$container_name)

    if [[ -n "$container_id" ]]; then
        # Если контейнер существует, проверяем его статус
        container_status=$(docker inspect --format '{{.State.Status}}' $container_name)
        
        if [[ "$container_status" == "running" ]]; then
            echo "Container '$container_name' is already running."
        elif [[ "$container_status" == "exited" ]]; then
            # Если контейнер существует, но остановлен, перезапускаем его
            echo "Container '$container_name' is stopped. Restarting it..."
            docker start $container_name
        else
            echo "Container '$container_name' is in an unknown state."
        fi
    else
        # Если контейнер не существует, запускаем его
        echo "Container '$container_name' does not exist. Starting it now..."
        docker run -d --gpus=all -v ollama:/root/.ollama -p 11434:11434 --name $container_name $image
        # Теперь подключаемся к контейнеру и выполняем команду
        echo "Running the command in the container..."
        docker exec -d $container_name $command_to_run

        echo "Command executed inside the container '$container_name'."
    fi
    cd Docker || exit
    cd dev || exit
    docker-compose up -d
else
    if [[ "$1" == "-down" ]]; then
        if docker ps -q -f name=ollama; then
            echo "Stopping container ollama..."
            docker stop ollama
        fi
    else
        if [[ "$1" == "-down-all" ]]; then
            if docker ps -q -f name=ollama; then
                echo "Removing container ollama..."
                docker stop ollama
                docker rm ollama
                docker rmi ollama/ollama
            fi
            echo "Done"
        else
            if [[ ! -f "./Backend/Sender-Service/src/main/resources/mail.properties" ]]; then
                echo
                echo "[ERROR] File mail.properties not found. Create it form file mail.properties.origin."
                echo "Exiting..."
                exit 1
            fi
            
            # Проверяем, существует ли контейнер
            container_id=$(docker ps -a -q -f name=$container_name)

            if [[ -n "$container_id" ]]; then
                # Если контейнер существует, проверяем его статус
                container_status=$(docker inspect --format '{{.State.Status}}' $container_name)
                
                if [[ "$container_status" == "running" ]]; then
                    echo "Container '$container_name' is already running."
                else
                    if [[ "$container_status" == "exited" ]]; then
                        # Если контейнер существует, но остановлен, перезапускаем его
                        echo "Container '$container_name' is stopped. Restarting it..."
                        docker start $container_name
                    else
                        echo "Container '$container_name' is in an unknown state."
                    fi
                fi
            else
                # Если контейнер не существует, запускаем его
                echo "Container '$container_name' does not exist. Starting it now..."
                docker run -d --gpus=all -v ollama:/root/.ollama -p 11434:11434 --name $container_name $image
                # Теперь подключаемся к контейнеру и выполняем команду
                echo "Running the command in the container..."
                docker exec -d $container_name $command_to_run

                echo "Command executed inside the container '$container_name'."
            fi
            docker rmi docker-sender-app
            docker rmi docker-main-app
            echo "Starting docker-compose for full project..."
            cd Docker || exit
            docker-compose up -d
        fi
    fi
fi

cd "$CURRENT_DIR" || exit