#!/bin/bash
set -e
cd /home/ubuntu/app

echo "Downloading fixed JAR..."
curl -f -o Flomad-AI-planner-0.0.1-SNAPSHOT.jar.new "https://flomad-deploy-temp-31756fdf.s3.us-east-1.amazonaws.com/deploy/Flomad-AI-planner-0.0.1-SNAPSHOT.jar?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIA6GBMBSXPBSFYHIFN%2F20251218%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251218T161221Z&X-Amz-Expires=600&X-Amz-SignedHeaders=host&X-Amz-Signature=554d1f0db37dedde38406fe02d521a618beabc6b9b04b399f37002f34cf0fbc0"

echo "Checking JAR size..."
ls -lh Flomad-AI-planner-0.0.1-SNAPSHOT.jar.new

echo "Replacing old JAR..."
mv Flomad-AI-planner-0.0.1-SNAPSHOT.jar.new Flomad-AI-planner-0.0.1-SNAPSHOT.jar
chown ubuntu:ubuntu Flomad-AI-planner-0.0.1-SNAPSHOT.jar

echo "Enabling and restarting service..."
systemctl enable flomad.service
systemctl restart flomad.service

echo "Waiting 45 seconds for startup..."
sleep 45

echo "=== Service Status ==="
systemctl status flomad.service --no-pager || true

echo "=== Health Check ==="
curl -f http://localhost:8080/actuator/health || echo "Health check failed"

echo "=== Deployment Complete ==="
