@echo off
docker-compose down
docker-compose up -d
gradlew.bat build
gradlew.bat bootrun