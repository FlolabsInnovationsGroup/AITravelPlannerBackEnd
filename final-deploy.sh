#!/bin/bash
set -ex

cd /home/ubuntu
rm -rf docker-app
git clone https://github.com/FlomadLabsRD/AITravelPlannerBackEnd.git docker-app
cd docker-app
cp /home/ubuntu/.env .
chown -R ubuntu:ubuntu /home/ubuntu/docker-app
sudo -u ubuntu mvn clean package -DskipTests
docker-compose up -d
sleep 45
docker ps
curl -f http://localhost:8080/actuator/health && echo "APP IS ONLINE!" || echo "NOT READY YET"
