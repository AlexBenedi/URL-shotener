@echo off
docker-compose down
docker-compose up -d
call gradlew build >nul 2>&1 || goto :continue
:continue
call gradlew build >nul 2>&1 || goto :continue
:continue
call gradlew build
call gradlew bootRun