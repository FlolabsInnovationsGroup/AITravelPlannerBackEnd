#!/bin/bash
# Flomad Backend Deployment Script
# Fixes JAR corruption issue by deploying the working JAR

set -e

echo "=== Flomad Backend Deployment ==="
echo "Stopping current service..."
systemctl stop flomad.service

echo "Downloading working JAR from S3..."
cd /home/ubuntu/app

# Use AWS CLI to download (instance has IAM role)
aws s3 cp s3://flomad-deploy-temp-31756fdf/WORKING-JAR.jar Flomad-AI-planner-0.0.1-SNAPSHOT.jar

echo "Checking JAR size..."
ls -lh Flomad-AI-planner-0.0.1-SNAPSHOT.jar

echo "Setting permissions..."
chown ubuntu:ubuntu Flomad-AI-planner-0.0.1-SNAPSHOT.jar

echo "Starting service..."
systemctl enable flomad.service
systemctl start flomad.service

echo "Waiting for application startup (45 seconds)..."
sleep 45

echo "=== Service Status ==="
systemctl status flomad.service --no-pager || true

echo "=== Health Check ==="
curl -s http://localhost:8080/actuator/health || echo "FAILED"

echo "=== Deployment Complete ==="
