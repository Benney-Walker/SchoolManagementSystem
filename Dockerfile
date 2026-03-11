# ---- STAGE 1: Build the app ----
# Start with a JDK image that has Maven available
FROM eclipse-temurin:21-jdk-jammy AS build

# Set the working directory inside the container
WORKDIR /app

# Copy pom.xml first (so Maven dependencies are cached if code hasn't changed)
COPY pom.xml .
COPY src ./src

# Install Maven and build the JAR, skipping tests
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests


# ---- STAGE 2: Run the app ----
# Use a smaller JRE-only image (no need for full JDK at runtime)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install system libraries that JasperReports needs to generate PDFs
# libfreetype6 = font rendering (this is what was crashing on Railway)
# fontconfig = font configuration
# fonts-dejavu = actual font files
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from Stage 1 into this Stage 2 container
COPY --from=build /app/target/*.jar app.jar

# Tell Docker our app runs on port 8080
EXPOSE 8080

# Start the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]