#!/bin/bash
docker container prune
docker rmi my_rag
docker build -t my-rag .
docker run -p 8000:8000 my-rag