# Manual Deployment Guide for EC2
# Since automated deployment via GitHub Actions is failing due to SSH connectivity issues

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "   MANUAL DEPLOYMENT TO EC2" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "The Issue:" -ForegroundColor Yellow
Write-Host "  - GitHub Actions cannot establish SSH connection to EC2"
Write-Host "  - SSH handshake consistently fails after 2-3 minutes"
Write-Host "  - This affects both SCP file transfer and SSH command execution"
Write-Host ""

Write-Host "Solution Options:" -ForegroundColor Green
Write-Host ""

Write-Host "OPTION 1: Fix EC2 SSH Access (Recommended)" -ForegroundColor Cyan
Write-Host "  1. Go to AWS Console → EC2 → Instances"
Write-Host "  2. Select instance i-0fc71de8cfc123fd8"
Write-Host "  3. Actions → Security → Modify IAM Role"
Write-Host "  4. Attach role with 'AmazonSSMManagedInstanceCore' policy"
Write-Host "  5. Then run: .\deploy-via-ssm.ps1 -Deploy"
Write-Host ""

Write-Host "OPTION 2: Use EC2 Instance Connect" -ForegroundColor Cyan
Write-Host "  1. Go to AWS Console → EC2 → Instances"
Write-Host "  2. Select instance i-0fc71de8cfc123fd8"
Write-Host "  3. Click 'Connect' button"
Write-Host "  4. Choose 'EC2 Instance Connect'"
Write-Host "  5. In the browser terminal, run:"
Write-Host "     curl -o deploy.sh https://raw.githubusercontent.com/FlomadLabsRD/AITravelPlannerBackEnd/master/deploy-ec2.sh"
Write-Host "     chmod +x deploy.sh"
Write-Host "     ./deploy.sh"
Write-Host ""

Write-Host "OPTION 3: Manual JAR Upload" -ForegroundColor Cyan
Write-Host "  1. JAR file is ready at:"
Write-Host "     C:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\AITravelPlannerBackEnd\target\Flomad-AI-planner-0.0.1-SNAPSHOT.jar"
Write-Host "  2. Upload via AWS S3:"
Write-Host "     aws s3 cp target/Flomad-AI-planner-0.0.1-SNAPSHOT.jar s3://YOUR_BUCKET/app.jar"
Write-Host "  3. Then on EC2 (via Instance Connect):"
Write-Host "     aws s3 cp s3://YOUR_BUCKET/app.jar ~/app/Flomad-AI-planner-0.0.1-SNAPSHOT.jar"
Write-Host ""

Write-Host "OPTION 4: Set up Self-Hosted GitHub Runner" -ForegroundColor Cyan
Write-Host "  1. On EC2 (via Instance Connect), run:"
Write-Host "     mkdir actions-runner && cd actions-runner"
Write-Host "     curl -o actions-runner-linux-x64-2.311.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.311.0/actions-runner-linux-x64-2.311.0.tar.gz"
Write-Host "     tar xzf ./actions-runner-linux-x64-2.311.0.tar.gz"
Write-Host "     ./config.sh --url https://github.com/FlomadLabsRD/AITravelPlannerBackEnd --token YOUR_TOKEN"
Write-Host "     ./run.sh"
Write-Host ""

Write-Host "Current Status:" -ForegroundColor Magenta
Write-Host "  ✅ JAR built successfully (194MB)"
Write-Host "  ✅ New SSH key generated and added to GitHub Secrets"
Write-Host "  ✅ EC2 instance is running and healthy"
Write-Host "  ❌ GitHub Actions → EC2 SSH connection failing"
Write-Host ""

Write-Host "Immediate Action Required:" -ForegroundColor Red
Write-Host "  Choose one of the options above to deploy the application"
Write-Host ""

$choice = Read-Host "Enter option number (1-4) or 'q' to quit"

switch ($choice) {
    "1" {
        Write-Host "`nOpening AWS Console..." -ForegroundColor Green
        Start-Process "https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Instances:instanceId=i-0fc71de8cfc123fd8"
        Write-Host "After adding IAM role, run: .\deploy-via-ssm.ps1 -Deploy"
    }
    "2" {
        Write-Host "`nOpening EC2 Instance Connect..." -ForegroundColor Green
        Start-Process "https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#ConnectToInstance:instanceId=i-0fc71de8cfc123fd8"
    }
    "3" {
        Write-Host "`nCreating deployment package..." -ForegroundColor Green
        $bucketName = Read-Host "Enter your S3 bucket name"
        aws s3 cp "C:\Users\purpl\OneDrive\Desktop\FlomadLabsRD\AITravelPlannerBackEnd\target\Flomad-AI-planner-0.0.1-SNAPSHOT.jar" "s3://$bucketName/flomad-app.jar"
        Write-Host "✅ Uploaded to S3. Now connect to EC2 and run:"
        Write-Host "   aws s3 cp s3://$bucketName/flomad-app.jar ~/app/Flomad-AI-planner-0.0.1-SNAPSHOT.jar"
    }
    "4" {
        Write-Host "`nOpening GitHub Settings..." -ForegroundColor Green
        Start-Process "https://github.com/FlomadLabsRD/AITravelPlannerBackEnd/settings/actions/runners/new"
        Write-Host "Follow the instructions to set up a self-hosted runner on EC2"
    }
    default {
        Write-Host "Exiting..." -ForegroundColor Yellow
    }
}
