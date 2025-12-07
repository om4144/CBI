# Use Eclipse Temurin (JDK 17)
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside the container
WORKDIR /app

# Download PostgreSQL JDBC Driver
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar postgresql.jar

# Copy the source code from your project into the container
# This copies the local 'src' folder to '/app/src' inside the container
COPY src ./src

# Create a directory for compiled classes
RUN mkdir classes

# Compile the code
# 1. Find all .java files inside src/main/java
# 2. Compile them into the 'classes' folder
# 3. Include the PostgreSQL jar in the classpath (-cp)
RUN find src -name "*.java" > sources.txt && \
    javac -cp .:postgresql.jar -d classes @sources.txt

# Run the server
# We specify the full package name: com.cbi.BankServer
# We add both 'classes' directory and the postgres jar to the classpath
CMD ["java", "-cp", "classes:postgresql.jar", "com.cbi.BankServer"]