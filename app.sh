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

# Функция для запуска полного проекта
start_full_project() {
    check_main_files
    docker rmi docker-sender-app
    docker rmi docker-main-app
    build_dockerbase_for_interview_service
    echo "Starting docker-compose for full project..."
    cd Docker || exit
    $DOCKER_COMPOSE_CMD up -d
}


start_dev_project() {
    check_main_files
    build_dockerbase_for_interview_service
    echo "Starting docker-compose for dev project..."
    cd Docker || exit
    cd dev || exit
    $DOCKER_COMPOSE_CMD up -d
}

build_dockerbase_for_interview_service() {
    CURR_DIR=${PWD}
    cd ./Backend/Interview-Service || exit
    docker build -f Dockerfile.base -t rag-base:latest .
    cd "$CURR_DIR" || exit
}


check_main_files() {
    if [[ ! -f "./Backend/Sender-Service/src/main/resources/mail.properties" ]]; then
        echo
        echo "[ERROR] File mail.properties not found. Create it form file mail.properties.origin."
        echo "Exiting..."
        exit 1
    fi
}

echo_cloudflared_url() {
    sleep 5
    # docker logs cloudflare-tunnel
    docker logs cloudflare-tunnel &> output.txt
    URL=$(grep -o 'https://[^ ]*trycloudflare.com' output.txt)
    rm output.txt
    echo "=========================================================================="
    echo
    echo "Main url:    "$URL
    echo "Swagger url: "$URL"/swagger-ui/index.html"
    echo
    echo "=========================================================================="
}


# Обработка аргументов
case "$1" in
    "-dev")
        stop_all_docker_compose
        start_dev_project
        ;;
    "-down")
        stop_all_docker_compose
        ;;
    *)
        stop_all_docker_compose
        start_full_project
	    echo_cloudflared_url
        ;;
esac

cd "$CURRENT_DIR" || exit
