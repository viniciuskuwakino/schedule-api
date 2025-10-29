# Traz o maven que ira compilar o código Java e gerar o .jar.
FROM maven:3.9.11-eclipse-temurin-21 AS build

# Copia a pasta src (seu código-fonte Java) para dentro do container, no diretório /app/src
COPY src /app/src

# Copia o arquivo pom.xml (descrição do projeto Maven e dependências) para o diretório /app
COPY pom.xml /app

# Define o diretório de trabalho padrão dentro do container (equivale a fazer cd /app)
WORKDIR /app

# Executa o build Maven dentro do container
RUN mvn clean install

# Imagem do jre apenas para executar a aplicação
FROM amazoncorretto:21-alpine3.19

# Copia o arquivo do build para /app/app.jar
COPY --from=build /app/target/schedule-api-0.0.1-SNAPSHOT.jar /app/app.jar

# Retorna para o diretório /app
WORKDIR /app

# Expoe a porta 8080
EXPOSE 8080

# Executa o comando "java -jar app.jar"
CMD ["java", "-jar", "app.jar"]
