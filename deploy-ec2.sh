#!/bin/bash
# EC2 Local Deployment Script - Run this ON the EC2 instance
# This script pulls code from GitHub and deploys locally

set -e

echo "🚀 Starting deployment..."

# Create .env file
cat > ~/app/.env << 'ENVEOF'
SPRING_AI_OPENAI_API_KEY=${SPRING_AI_OPENAI_API_KEY}
GOOGLE_API_KEY=${GOOGLE_API_KEY}
VIATOR_API_KEY=${VIATOR_API_KEY}
VIATOR_PARTNER_KEY=${VIATOR_PARTNER_KEY}
AMADEUS_API_KEY=${AMADEUS_API_KEY}
AMADEUS_API_SECRET=${AMADEUS_API_SECRET}
JWT_SECRET=${JWT_SECRET}
MONGO_URL=${MONGO_URL}
WEATHER_API_KEY=${WEATHER_API_KEY}
POSITION_STACK_API_KEY=${POSITION_STACK_API_KEY}
SPRING_PROFILES_ACTIVE=prod
ENVEOF

echo "✅ .env file created"

# Clone or update repository
if [ ! -d ~/AITravelPlannerBackEnd ]; then
  cd ~ && git clone https://github.com/FlomadLabsRD/AITravelPlannerBackEnd.git
  echo "✅ Repository cloned"
else
  cd ~/AITravelPlannerBackEnd && git pull origin master
  echo "✅ Repository updated"
fi

# Build JAR
echo "🔨 Building JAR (this may take 5-7 minutes)..."
cd ~/AITravelPlannerBackEnd && mvn clean package -DskipTests
echo "✅ Build complete"

# Copy to deployment directory
cp target/Flomad-AI-planner-0.0.1-SNAPSHOT.jar ~/app/
echo "✅ JAR copied to ~/app/"

# Deploy with Docker Compose
cd ~/docker-app
docker-compose down || true
rm -f *.jar .env
cp ~/app/Flomad-AI-planner-0.0.1-SNAPSHOT.jar .
cp ~/app/.env .
echo "🐳 Starting Docker containers..."
docker-compose up -d

# Wait for application to start
echo "⏳ Waiting for application to start (30 seconds)..."
sleep 30

# Health check
echo "🏥 Checking application health..."
if curl -f http://localhost:8080/actuator/health; then
  echo ""
  echo "✅ Deployment successful! Application is healthy."
else
  echo ""
  echo "❌ Health check failed. Checking logs..."
  docker logs flomad-app | tail -50
  exit 1
fi
