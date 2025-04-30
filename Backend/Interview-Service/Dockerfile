# Use custom base image with dependencies pre-installed
ARG BASE_IMAGE=rag-base:latest
FROM $BASE_IMAGE

# Copy application code
COPY . /app

# Set working directory
WORKDIR /app

# Start application
ENTRYPOINT ["python", "main.py"]