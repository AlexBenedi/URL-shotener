#!/bin/bash
docker-compose down 
docker-compose up -d 
./gradlew build > /dev/null 2>&1 || true
./gradlew build > /dev/null 2>&1 || true
./gradlew build
./gradlew bootrun