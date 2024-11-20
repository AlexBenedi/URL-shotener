#!/bin/bash
docker-compose down 
docker-compose up -d 
./gradlew build
./gradlew bootrun