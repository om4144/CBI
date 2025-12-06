# Use a lightweight Java image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Download PostgreSQL JDBC Driver (Cloud needs this)
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar postgresql.jar

# Copy all your Java files into the container
COPY . .

# Compile the code (adding the jar to classpath)
RUN javac -cp .:postgresql.jar *.java

# Run the server (Port 8080 is standard)
CMD ["java", "-cp", ".:postgresql.jar", "BankServer"]