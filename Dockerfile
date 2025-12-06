# Use Eclipse Temurin (The new standard for OpenJDK)
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Download PostgreSQL JDBC Driver
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar postgresql.jar

# Copy all your Java files into the container
COPY . .

# Compile the code (adding the jar to classpath)
# Note: We use the '*' wildcard to compile all java files
RUN javac -cp .:postgresql.jar *.java

# Run the server (Port 8080 is standard)
CMD ["java", "-cp", ".:postgresql.jar", "BankServer"]
