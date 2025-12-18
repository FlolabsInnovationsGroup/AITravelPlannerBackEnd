#!/bin/bash
set -e

cd /home/ubuntu/app

# Download JAR using presigned URL
echo "Downloading JAR from S3..."
rm -f Flomad-AI-planner-0.0.1-SNAPSHOT.jar
wget -O Flomad-AI-planner-0.0.1-SNAPSHOT.jar 'https://flomad-deploy-temp-31756fdf.s3.us-east-1.amazonaws.com/Flomad-AI-planner-0.0.1-SNAPSHOT.jar?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIA6GBMBSXPBSFYHIFN%2F20251218%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251218T151954Z&X-Amz-Expires=600&X-Amz-SignedHeaders=host&X-Amz-Signature=a29302bfa3e7cc5c3c942069c697bac619e1bde3daa8f3817443fbce3e241fcd'

# Verify download
ls -lh Flomad-AI-planner-0.0.1-SNAPSHOT.jar
echo "MD5:"
md5sum Flomad-AI-planner-0.0.1-SNAPSHOT.jar

# Stop old process
echo "Stopping old process..."
sudo pkill -9 -f Flomad-AI-planner || true
sleep 5

# Start new process
echo "Starting new process..."
sudo -u ubuntu nohup java -Xmx2048m -Xms512m -XX:+UseG1GC -jar Flomad-AI-planner-0.0.1-SNAPSHOT.jar > stdout.log 2>&1 &

# Wait for startup
echo "Waiting 90 seconds for Spring Boot to start..."
sleep 90

# Health check
if curl -f http://localhost:8080/actuator/health; then
    echo "✓ DEPLOYMENT SUCCESSFUL"
else
    echo "⚠ Application still starting, check logs with: tail -f /home/ubuntu/app/stdout.log"
fi
