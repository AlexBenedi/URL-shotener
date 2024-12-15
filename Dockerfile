# Usar una imagen base con JDK 17
FROM openjdk:17-jdk-slim

# Crear un usuario no root
RUN useradd -m appuser

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Cambiar al usuario creado
USER appuser

# Copiar el archivo JAR de tu aplicación al contenedor
COPY app-0.2024.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8080
EXPOSE 8080
EXPOSE 9092

ARG GOOGLE_CLIENT_ID_API_KEY
ARG GOOGLE_SAFE_BROWSING_API_KEY
ARG GOOGLE_CLIENT_SECRET_API_KEY
ARG NINJA_PROFANITY_FILTER_API_KEY
ARG KAFKA_BROKER_IP

ENV GOOGLE_CLIENT_ID_API_KEY=$GOOGLE_CLIENT_ID_API_KEY \
    GOOGLE_SAFE_BROWSING_API_KEY=$GOOGLE_SAFE_BROWSING_API_KEY \
    GOOGLE_CLIENT_SECRET_API_KEY=$GOOGLE_CLIENT_SECRET_API_KEY \
    NINJA_PROFANITY_FILTER_API_KEY=$NINJA_PROFANITY_FILTER_API_KEY \
    KAFKA_BROKER_IP=$KAFKA_BROKER_IP

# Comando para ejecutar el archivo JAR
CMD ["java", "-jar", "app.jar"]

