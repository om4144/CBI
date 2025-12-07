# Use Eclipse Temurin
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Download PostgreSQL JDBC Driver
ADD https://jdbc.postgresql.org/download/postgresql-42.7.2.jar postgresql.jar

# Copy the entire src folder
COPY src ./src

# Create a directory for compiled classes
RUN mkdir classes

# Compile the code
# We find all .java files in src and compile them into the 'classes' folder
RUN find src -name "*.java" > sources.txt && \
    javac -cp .:postgresql.jar -d classes @sources.txt

# Run the server
# Note the full package name: com.cbi.BankServer
# And we add 'classes' to the classpath
CMD ["java", "-cp", "classes:postgresql.jar", "com.cbi.BankServer"]