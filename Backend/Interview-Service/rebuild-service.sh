#!/bin/bash

docker stop docker-llm-app-1
docker container prune
docker rmi docker-llm-app
dir=${PWD}
cd ../../Docker
echo ${dir}
echo ${PWD}
docker-compose up -d llm-app