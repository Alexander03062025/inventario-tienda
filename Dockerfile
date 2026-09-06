# ---------- Etapa 1: compilar el .jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero el pom para cachear la descarga de dependencias
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Luego el código y el empaquetado
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Etapa 2: imagen final, solo con el runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render define la variable PORT; la app ya la lee (server.port=${PORT:8080})
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
