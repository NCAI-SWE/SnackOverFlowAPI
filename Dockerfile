FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# 1. Update certificates (Crucial inside corporate network)
RUN apk add --no-cache ca-certificates

WORKDIR /usr/src/app

# 2. Copy your configuration files
COPY pom.xml .
COPY settings.xml .

# 3. Download dependencies 
# We use -Dmaven.wagon.http.ssl.insecure=true to bypass corporate proxy SSL issues
RUN mvn -B -s settings.xml dependency:go-offline \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slotcaster=warn

# 4. Copy source and build
COPY src ./src
RUN mvn -B -s settings.xml clean install -DskipTests=true \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dmaven.wagon.http.ssl.allowall=true \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slotcaster=warn

# 5. Final runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the built JAR
COPY --from=builder /usr/src/app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
