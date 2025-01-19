#!/bin/bash

# Определяем операционную систему
OS_TYPE=$(uname -a)

# Устанавливаем команду для docker-compose в зависимости от ОС
if [[ "$OS_TYPE" == *"Linux"* ]]; then
    DOCKER_COMPOSE_CMD="docker compose"
else
    DOCKER_COMPOSE_CMD="docker-compose"
fi

CURRENT_DIR=${PWD}

# Функция для остановки всех docker-compose
stop_all_docker_compose() {
    echo "Stopping docker-compose from all dirs..."
    cd Docker || exit
    $DOCKER_COMPOSE_CMD down
    cd dev || exit
    $DOCKER_COMPOSE_CMD down
    cd "$CURRENT_DIR" || exit
}

# Функция для запуска cloudflared туннеля
start_cloudflared_tunnel() {
    container_name="cloudflared"
    image="cloudflared/cloudflared"
    
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
        echo "Starting Cloudflare Tunnel..."
        docker run -d --name cloudflared --network host cloudflare/cloudflared tunnel --url http://host.docker.internal:80
        echo "Cloudflare Tunnel started."
    fi
}

# Функция для запуска контейнера ollama
start_ollama_container() {
    container_name="ollama"
    image="ollama/ollama"
    command_to_run="ollama run llama3.1:8b-instruct-q4_K_M"
    
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
        if command -v nvidia-smi &> /dev/null; then
            echo "GPU available. Running a container using the GPU."
            docker run -d --gpus=all -v ollama:/root/.ollama -p 11434:11434 --name $container_name $image
        else
            echo "GPU not available. Running a container using the CPU."
            docker run -d -v ollama:/root/.ollama -p 11434:11434 --name $container_name $image
        fi
        # Теперь подключаемся к контейнеру и выполняем команду
        echo "Running the command in the container..."
        docker exec -d $container_name $command_to_run

        echo "Command executed inside the container '$container_name'."
    fi
}



# Функция для запуска полного проекта
start_full_project() {
    check_main_files
    if [[ ! -f "./Backend/Sender-Service/src/main/resources/mail.properties" ]]; then
        echo
        echo "[ERROR] File mail.properties not found. Create it form file mail.properties.origin."
        echo "Exiting..."
        exit 1
    fi
    docker rmi docker-sender-app
    docker rmi docker-main-app
    echo "Starting docker-compose for full project..."
    cd Docker || exit
    $DOCKER_COMPOSE_CMD up -d
}


start_dev_project() {
    check_main_files
    echo "Starting docker-compose for dev project..."
    cd Docker || exit
    cd dev || exit
    $DOCKER_COMPOSE_CMD up -d
}


check_main_files() {
    if [[ ! -f "./Backend/Sender-Service/src/main/resources/mail.properties" ]]; then
        echo
        echo "[ERROR] File mail.properties not found. Create it form file mail.properties.origin."
        echo "Exiting..."
        exit 1
    fi
}

# Функция для остановки и удаления контейнеров
stop_and_remove_containers() {
    stop_and_remove_ollama
    stop_and_remove_cloudflared
}


stop_and_remove_ollama() {
    if docker ps -q -f name=ollama; then
        stop_ollama_container
        docker rm ollama
        docker rmi ollama/ollama
    fi
}

stop_ollama_container() {
    if docker ps -q -f name=ollama; then
        echo "Stopping container ollama..."
        docker stop ollama
    fi
}

stop_and_remove_cloudflared() {
    if docker ps -q -f name=cloudflared; then
        stop_cloudflared_container
        docker rm cloudflared
    fi
}


stop_cloudflared_container() {
     if docker ps -q -f name=cloudflared; then
        echo "Stopping container cloudflared..."
        docker stop cloudflared
    fi
}

echo_cloudflared_url() {
    docker logs --tail 17 cloudflared
}



# Обработка аргументов
case "$1" in
    "-cloud")
        stop_all_docker_compose
        start_cloudflared_tunnel
        start_ollama_container
        start_full_project
        echo_cloudflared_url
        ;;
    "-dev")
        stop_all_docker_compose
        start_ollama_container
        start_dev_project
        ;;
    "-down")
        stop_all_docker_compose
        stop_ollama_container
        stop_and_remove_cloudflared
        ;;
    "-down-all")
        stop_all_docker_compose
        stop_and_remove_containers
        ;;
    *)
        stop_all_docker_compose
        start_ollama_container
        start_full_project
        ;;
esac

cd "$CURRENT_DIR" || exit
