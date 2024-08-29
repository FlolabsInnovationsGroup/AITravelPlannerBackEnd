# Use an official Java runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the jar file into the container
COPY target/Flomad-AI-planner-0.0.1-SNAPSHOT.jar /app/Flomad-AI-planner-0.0.1-SNAPSHOT.jar

# Command to run the application
CMD ["java", "-jar", "Flomad-AI-planner-0.0.1-SNAPSHOT.jar"]
