# Etapa 1: Build
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

# Copia apenas arquivos de configuração primeiro (para cache)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build -x test || return 0

# Copia o restante do código
COPY . .

# Compila o projeto (gera o JAR em build/libs)
RUN gradle clean bootJar -x test

# Etapa 2: Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copia o JAR gerado da etapa anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Comando de execução
ENTRYPOINT ["java", "-jar", "app.jar"]
