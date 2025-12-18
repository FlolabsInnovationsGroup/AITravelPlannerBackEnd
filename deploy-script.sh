#!/bin/bash
cd /home/ubuntu/app
curl -o Flomad-AI-planner-0.0.1-SNAPSHOT.jar 'https://flomad-deploy-temp-31756fdf.s3.us-east-1.amazonaws.com/Flomad-AI-planner-WORKING.jar?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIA6GBMBSXPBSFYHIFN%2F20251218%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251218T160734Z&X-Amz-Expires=600&X-Amz-SignedHeaders=host&X-Amz-Signature=5ceb7d3826971968665ed64b26c5e967909d651f5f5b73e8c6a17028057485f4'
chown ubuntu:ubuntu Flomad-AI-planner-0.0.1-SNAPSHOT.jar
ls -lh Flomad-AI-planner-0.0.1-SNAPSHOT.jar
systemctl enable flomad.service
systemctl restart flomad.service
echo "Waiting for service to start..."
sleep 45
systemctl status flomad.service
curl http://localhost:8080/actuator/health
