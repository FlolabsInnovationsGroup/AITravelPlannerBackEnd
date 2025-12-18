# Deploy to EC2 using AWS Systems Manager (SSM)
# This bypasses SSH connectivity issues

param(
    [switch]$SetupSSM,
    [switch]$Deploy
)

$instanceId = "i-0fc71de8cfc123fd8"

function Install-SSMAgent {
    Write-Host "📦 Installing/configuring SSM Agent on EC2..." -ForegroundColor Cyan
    
    # Create a temporary security group rule to allow SSM
    Write-Host "Ensuring IAM role allows SSM access..."
    
    # The instance needs the AmazonSSMManagedInstanceCore policy
    # We'll create a simple install script
    
    $installScript = @"
#!/bin/bash
# Install SSM Agent if not present
if ! systemctl is-active --quiet amazon-ssm-agent; then
    echo "Installing SSM Agent..."
    cd /tmp
    wget https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/debian_amd64/amazon-ssm-agent.deb
    sudo dpkg -i amazon-ssm-agent.deb
    sudo systemctl enable amazon-ssm-agent
    sudo systemctl start amazon-ssm-agent
    echo "SSM Agent installed and started"
else
    echo "SSM Agent already running"
fi
"@
    
    Write-Host "⚠️  SSM requires the EC2 instance to have an IAM role with SSM permissions" -ForegroundColor Yellow
    Write-Host "    You may need to manually add the IAM role in AWS Console" -ForegroundColor Yellow
}

function Deploy-Application {
    Write-Host "🚀 Deploying application to EC2 via AWS Systems Manager..." -ForegroundColor Cyan
    
    # Read environment variables
    $envVars = @{}
    if (Test-Path "c:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\AITravelPlannerBackEnd\.env") {
        Get-Content "c:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\AITravelPlannerBackEnd\.env" | ForEach-Object {
            if ($_ -match '^([^=]+)=(.*)$') {
                $envVars[$matches[1]] = $matches[2]
            }
        }
    }
    
    $deployScript = @"
#!/bin/bash
set -e

echo '🚀 Starting deployment...'

# Create app directory
mkdir -p ~/app

# Create .env file
cat > ~/app/.env << 'ENVEOF'
SPRING_AI_OPENAI_API_KEY=$($envVars['SPRING_AI_OPENAI_API_KEY'])
GOOGLE_API_KEY=$($envVars['GOOGLE_API_KEY'])
VIATOR_API_KEY=$($envVars['VIATOR_API_KEY'])
VIATOR_PARTNER_KEY=$($envVars['VIATOR_PARTNER_KEY'])
AMADEUS_API_KEY=$($envVars['AMADEUS_API_KEY'])
AMADEUS_API_SECRET=$($envVars['AMADEUS_API_SECRET'])
JWT_SECRET=$($envVars['JWT_SECRET'])
MONGO_URL=$($envVars['MONGO_URL'])
WEATHER_API_KEY=$($envVars['WEATHER_API_KEY'])
POSITION_STACK_API_KEY=$($envVars['POSITION_STACK_API_KEY'])
SPRING_PROFILES_ACTIVE=prod
ENVEOF

echo '✅ .env file created'

# Clone or update repository
if [ ! -d ~/AITravelPlannerBackEnd ]; then
  cd ~ && git clone https://github.com/FlomadLabsRD/AITravelPlannerBackEnd.git
  echo '✅ Repository cloned'
else
  cd ~/AITravelPlannerBackEnd && git pull origin master
  echo '✅ Repository updated'
fi

# Build JAR
echo '🔨 Building JAR (this may take 5-7 minutes)...'
cd ~/AITravelPlannerBackEnd && mvn clean package -DskipTests
echo '✅ Build complete'

# Copy to deployment directory
cp target/Flomad-AI-planner-0.0.1-SNAPSHOT.jar ~/app/
echo '✅ JAR copied to ~/app/'

# Deploy with Docker Compose
cd ~/docker-app
docker-compose down || true
rm -f *.jar .env
cp ~/app/Flomad-AI-planner-0.0.1-SNAPSHOT.jar .
cp ~/app/.env .
echo '🐳 Starting Docker containers...'
docker-compose up -d

# Wait for application to start
echo '⏳ Waiting for application to start (30 seconds)...'
sleep 30

# Health check
echo '🏥 Checking application health...'
curl -f http://localhost:8080/actuator/health || echo 'Health check failed'

echo '✅ Deployment complete'
"@

    Write-Host "Sending deployment command to EC2..."
    
    try {
        # Try SSM first
        $result = aws ssm send-command `
            --instance-ids $instanceId `
            --document-name "AWS-RunShellScript" `
            --parameters "commands=['$deployScript']" `
            --output json | ConvertFrom-Json
        
        $commandId = $result.Command.CommandId
        Write-Host "✅ Command sent! Command ID: $commandId" -ForegroundColor Green
        Write-Host "⏳ Waiting for execution..." -ForegroundColor Cyan
        
        Start-Sleep -Seconds 10
        
        # Get command status
        $status = aws ssm get-command-invocation `
            --command-id $commandId `
            --instance-id $instanceId `
            --output json | ConvertFrom-Json
        
        Write-Host "Status: $($status.Status)"
        Write-Host "`nOutput:"
        Write-Host $status.StandardOutputContent
        
        if ($status.StandardErrorContent) {
            Write-Host "`nErrors:" -ForegroundColor Red
            Write-Host $status.StandardErrorContent
        }
    }
    catch {
        Write-Host "❌ SSM not available. Instance needs IAM role with SSM permissions." -ForegroundColor Red
        Write-Host "`nFalling back to creating deployment package..." -ForegroundColor Yellow
        
        # Create a deployment package
        $packagePath = "C:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\deployment-package"
        New-Item -ItemType Directory -Force -Path $packagePath | Out-Null
        
        Copy-Item "C:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\AITravelPlannerBackEnd\target\Flomad-AI-planner-0.0.1-SNAPSHOT.jar" -Destination "$packagePath\"
        Copy-Item "c:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\AITravelPlannerBackEnd\deploy-ec2.sh" -Destination "$packagePath\"
        
        Write-Host "✅ Deployment package created at: $packagePath" -ForegroundColor Green
        Write-Host "`nNext steps:" -ForegroundColor Cyan
        Write-Host "1. You need to manually copy these files to EC2"
        Write-Host "2. Or add an IAM role to the EC2 instance for SSM access"
        Write-Host "3. Or fix the SSH key configuration"
    }
}

# Main execution
if ($SetupSSM) {
    Install-SSMAgent
}
elseif ($Deploy) {
    Deploy-Application
}
else {
    Write-Host "Usage:" -ForegroundColor Cyan
    Write-Host "  .\deploy-via-ssm.ps1 -SetupSSM   # Setup Systems Manager"
    Write-Host "  .\deploy-via-ssm.ps1 -Deploy     # Deploy application"
}
