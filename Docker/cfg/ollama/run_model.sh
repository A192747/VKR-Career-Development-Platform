#!/bin/bash

echo "Starting Ollama server..."
ollama serve &  # Start Ollama in the background

echo "Waiting for Ollama server to be active..."
while [ "$(ollama list | grep 'NAME')" == "" ]; do
  sleep 1
done

echo "Running llama3.1:8b-instruct-q4_K_M..."
ollama run llama3.1:8b-instruct-q4_K_M
