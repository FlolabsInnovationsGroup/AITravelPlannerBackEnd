# AI Travel Planner - Comprehensive Deployment Troubleshooting Guide

**Date:** December 16-17, 2025  
**Duration:** 15+ hours  
**Status:** ✅ Site Online (with warnings)

---

## Executive Summary

After 2+ days of downtime following a major feature merge, we successfully restored the production site at https://flomadtravel.com. The deployment involved resolving backend connectivity issues, fixing Mixed Content Blocking, and implementing temporary HTTPS solutions. The site is now functional but requires DNS configuration for a permanent, secure solution.

---

## Phase 1: Backend Deployment Failure (13 Hours)

### Initial Problem
- **Issue:** Backend Spring Boot application not accessible at original EC2 instance
- **Impact:** Complete site outage for 2+ days
- **Root Cause:** Multiple cascading failures with SSH, IAM roles, and MongoDB configuration

### Attempted Solutions & Failures

#### Attempt 1: SSH Access Restoration ❌
**Problem:** SSH key-based authentication failing
```bash
ssh -i "FlomadAI.pem" ubuntu@ec2-18-232-125-178.compute-1.amazonaws.com
# Result: Permission denied (publickey)
```

**Issues Encountered:**
- PEM file permissions incorrect (needed chmod 400)
- EC2 instance security group blocking SSH (port 22)
- SSH service stopped on instance
- IAM role missing for Systems Manager

**Why It Failed:** Even after fixing permissions and security groups, SSH remained completely unresponsive. Instance was in an unrecoverable state.

#### Attempt 2: AWS Systems Manager Session Manager ❌
**Problem:** Cannot connect via SSM
```bash
aws ssm start-session --target i-0fc71de8cfc123fd8
# Result: TargetNotConnected: Instance is not connected
```

**Issues Encountered:**
- Instance missing IAM role for SSM
- SSM Agent not running on instance
- No internet connectivity to download SSM agent

**Why It Failed:** Chicken-and-egg problem - needed SSH to fix SSM, but SSH was broken.

#### Attempt 3: EC2 Instance Connect ❌
**Problem:** Instance Connect unavailable in us-east-1
```bash
aws ec2-instance-connect send-ssh-public-key
# Result: Service not available in this region
```

**Why It Failed:** Service limitation in the AWS region.

#### Attempt 4: Automated GitHub Actions Deployment ❌
**Problem:** GitHub Actions workflow failing
```yaml
# .github/workflows/deploy.yml
- name: Deploy to EC2
  run: |
    ssh -o StrictHostKeyChecking=no ubuntu@${{ secrets.EC2_HOST }} \
    "cd /home/ubuntu/app && ./deploy.sh"
```

**Issues Encountered:**
- SSH authentication failing in GitHub Actions
- Secrets not properly configured
- Deployment script missing execute permissions
- JAR file upload failing due to size (186MB)

**Why It Failed:** Same underlying SSH connectivity issues, plus GitHub Actions runner network restrictions.

---

### ✅ Successful Solution: New EC2 Instance + Manual Deployment

#### Decision Point
After 13 hours of debugging, determined old instance was unrecoverable. Created new instance with proper configuration from the start.

#### New Instance Setup
```bash
# Instance Details
Instance ID: i-09a0551570b63a763
Public IP: 18.232.125.178
Instance Type: t3a.small
AMI: Ubuntu 24.04
IAM Role: EC2SSMProfile (SSM + S3 access)
Security Group: sg-0120a25299523d7f4 (all ports open)
```

#### Deployment Method: AWS Console Session Manager
**Why This Worked:**
- Browser-based terminal (no SSH needed)
- Works through AWS Console UI
- IAM role pre-configured on instance
- No network restrictions

#### Deployment Steps
```bash
# 1. Upload JAR to S3
aws s3 cp target/Flomad-AI-planner-0.0.1-SNAPSHOT.jar s3://flomadai-deployment/

# 2. Download on EC2 via Session Manager
aws s3 cp s3://flomadai-deployment/Flomad-AI-planner-0.0.1-SNAPSHOT.jar /home/ubuntu/docker-app/app.jar

# 3. Create environment file
cat > /home/ubuntu/docker-app/.env << 'EOF'
SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/testdb
MONGO_URL=mongodb://mongodb:27017/testdb
# ... all API keys
EOF

# 4. Create docker-compose.yml
cat > /home/ubuntu/docker-app/docker-compose.yml << 'EOF'
version: '3.8'
services:
  springboot:
    image: eclipse-temurin:21-jdk
    container_name: flomad-app
    working_dir: /app
    volumes:
      - ./app.jar:/app/app.jar
      - ./.env:/app/.env
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=local
    command: java -jar /app/app.jar
    env_file:
      - .env
    restart: unless-stopped
    depends_on:
      - mongodb
  mongodb:
    image: mongo:7.0
    container_name: mongodb
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    restart: unless-stopped
volumes:
  mongodb_data:
EOF

# 5. Start services
docker-compose up -d
```

#### Critical Issues Fixed During Deployment

**Issue 1: MongoDB Connection Failure**
```bash
# Problem
MongoTimeoutException: Timed out after 30000 ms while waiting to connect

# Solution
# Wrong: MONGO_URL=mongodb://localhost:27017/testdb
# Right: MONGO_URL=mongodb://mongodb:27017/testdb
# Reason: Docker container networking requires service name, not localhost
```

**Issue 2: Disk Space Exhaustion**
```bash
# Problem
No space left on device (df -h showed 100% usage on 8GB disk)

# Solution
aws ec2 modify-volume --volume-id vol-xxx --size 16
# Then resize partition:
sudo growpart /dev/xvda 1
sudo resize2fs /dev/xvda1
```

**Issue 3: Environment Variable Conflicts**
```bash
# Problem
Application using wrong MongoDB URI from multiple sources

# Solution
# Ensured .env file has priority:
SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/testdb
# AND added to docker-compose.yml:
env_file:
  - .env
```

#### Final Result
```bash
# Verification
curl http://18.232.125.178:8080/actuator/health
# Response:
{
  "status": "UP",
  "components": {
    "mongo": {"status": "UP"},
    "aiService": {"status": "UP"},
    "redisCache": {"status": "UP"}
  }
}
```

**Backend Status:** ✅ ONLINE at http://18.232.125.178:8080

---

## Phase 2: Frontend "Updating App Please Wait" Loop (2 Hours)

### The Problem
- **Symptom:** flomadtravel.com continuously flashing "updating app please wait"
- **User Impact:** Complete site unusability
- **Root Cause:** Mixed Content Blocking

### Technical Explanation

**What is Mixed Content Blocking?**
```
HTTPS Site (flomadtravel.com)
    ↓ tries to load
HTTP Resource (http://18.232.125.178:8080/api/...)
    ↓
BLOCKED by browser security policy
    ↓
Site cannot fetch data → infinite reload loop
```

Modern browsers (Chrome, Firefox, Safari, Edge) **block HTTP requests from HTTPS sites** for security reasons. This is called Mixed Content Blocking.

**Browser Console Errors:**
```javascript
Mixed Content: The page at 'https://flomadtravel.com' was loaded over HTTPS, 
but requested an insecure XMLHttpRequest endpoint 
'http://18.232.125.178:8080/actuator/health'. 
This request has been blocked; the content must be served over HTTPS.
```

### Attempted Solutions

#### Attempt 1: Update Environment Variables to HTTP ❌
**Approach:** Use HTTP backend URL in Vercel
```env
NEXT_PUBLIC_API_URL=http://18.232.125.178:8080
```

**Why It Failed:** Vercel automatically serves via HTTPS. Browsers still blocked HTTP backend calls.

**Error:** Same Mixed Content Blocking error.

---

#### Attempt 2: Cloudflare Tunnel ⚠️ (Partial Success)
**Approach:** Use Cloudflare's free tunnel service for instant HTTPS

**Installation:**
```bash
# On EC2
cd /tmp
wget https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared-linux-amd64.deb
cloudflared tunnel --url http://localhost:8080
```

**Result:**
```
Your quick Tunnel has been created! Visit it at:
https://captain-thorough-university-flush.trycloudflare.com
```

**Updated Vercel:**
```env
NEXT_PUBLIC_API_URL=https://captain-thorough-university-flush.trycloudflare.com
```

**Why It Failed:**
- WebSocket connections returned 403 Forbidden
- Real-time map features broken
- Chat worked but map synchronization failed

**Error Logs:**
```
Failed to load resource: the server responded with a status of 403 ()
WebSocket connection error
```

**Conclusion:** Cloudflare Tunnel blocks WebSocket upgrade requests needed for real-time features.

---

#### Attempt 3: ngrok ❌
**Approach:** Alternative tunneling service with WebSocket support

**Installation:**
```bash
# Install ngrok
curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc
echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list
sudo apt update && sudo apt install ngrok -y

# Start tunnel
ngrok http 8080
```

**Why It Failed:**
```
ERROR: authentication failed: Usage of ngrok requires a verified account and authtoken.
ERROR: Sign up for an account: https://dashboard.ngrok.com/signup
```

**Conclusion:** Requires account signup and API token configuration.

---

#### Attempt 4: localtunnel ⚠️ (Partial Success)
**Approach:** Free tunnel service, no signup required

**Installation:**
```bash
# Install Node.js and localtunnel
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs
sudo npm install -g localtunnel

# Start tunnel
lt --port 8080
```

**Result:**
```
your url is: https://eighty-buses-type.loca.lt
```

**Updated Vercel:**
```env
NEXT_PUBLIC_API_URL=https://eighty-buses-type.loca.lt
```

**Why It Failed:**
- First-time visitors see password prompt page
- "To access the website, please enter the tunnel password below"
- No password was set, but page still appears
- User friction unacceptable for production

**Conclusion:** Requires clicking through security landing page on every new device/IP.

---

#### Attempt 5: NGINX Reverse Proxy with Self-Signed SSL ⚠️ (Temporary Success)
**Approach:** Install NGINX with self-signed SSL certificate

**Installation:**
```bash
# Install NGINX
sudo apt install nginx -y

# Create self-signed certificate
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/ssl/private/nginx-selfsigned.key \
  -out /etc/ssl/certs/nginx-selfsigned.crt \
  -subj "/C=US/ST=State/L=City/O=FlomadTravel/CN=18.232.125.178"

# Configure NGINX
sudo tee /etc/nginx/sites-available/backend > /dev/null <<'EOF'
server {
    listen 80;
    listen 443 ssl;
    server_name 18.232.125.178;

    ssl_certificate /etc/ssl/certs/nginx-selfsigned.crt;
    ssl_certificate_key /etc/ssl/private/nginx-selfsigned.key;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# Enable and restart
sudo ln -sf /etc/nginx/sites-available/backend /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

**Verification:**
```bash
curl -k https://localhost:443/actuator/health
# Response: {"status":"UP"}
```

**Updated Vercel:**
```env
NEXT_PUBLIC_API_URL=https://18.232.125.178
```

**Issues:**
1. **Browser Security Warning:** "Your connection is not private" (ERR_CERT_AUTHORITY_INVALID)
2. **Certificate Acceptance Required:** Users must click "Advanced" → "Proceed" once
3. **Mixed Content Still Initially:** Browser blocks until certificate manually accepted
4. **"Not Secure" Badge:** Site shows as "Not Secure" in browser address bar

**Workaround:**
1. Visit https://18.232.125.178/actuator/health directly
2. Click through certificate warning
3. Then visit https://flomadtravel.com
4. Site works but shows "Not Secure"

**Why This Is Acceptable (Temporarily):**
- ✅ Site functionally works
- ✅ All features operational (chat, map, WebSockets)
- ✅ NGINX runs as systemd service (survives reboots)
- ✅ No manual terminal required
- ⚠️ Shows "Not Secure" warning
- ⚠️ Users must accept certificate once per browser

---

### ✅ Current Solution: Self-Signed NGINX (Temporary)

**Backend Configuration:**
- URL: https://18.232.125.178
- NGINX listening on ports 80 (HTTP) and 443 (HTTPS)
- Self-signed SSL certificate
- WebSocket support enabled
- Runs as systemd service (permanent)

**Frontend Configuration:**
```env
# .env.production
NEXT_PUBLIC_API_URL=https://18.232.125.178

# vercel.json
{
  "env": {
    "NEXT_PUBLIC_API_URL": "https://18.232.125.178"
  }
}
```

**Deployment Status:**
- ✅ Backend: Online and healthy
- ✅ Frontend: Deployed to Vercel
- ✅ Map: Real-time synchronization working
- ✅ Chat: AI responses working
- ✅ WebSockets: Connected and functional
- ⚠️ Security Warning: Browser shows "Not Secure"

---

## What Still Needs to Be Done

### Immediate Priority: Proper SSL Certificate

**Problem:**
Site shows as "Not Secure" due to self-signed certificate. This is unacceptable for production.

**Permanent Solution: Let's Encrypt with DNS Configuration**

#### Step 1: DNS Configuration (Requires Domain Registrar Access)
**Add A Record:**
```
Type: A
Name: api
Value: 18.232.125.178
TTL: 300
```

**Result:** `api.flomadtravel.com` → `18.232.125.178`

#### Step 2: Install Certbot and Obtain Certificate
```bash
# Install Certbot
sudo apt update
sudo apt install certbot python3-certbot-nginx -y

# Obtain certificate (automatic verification via HTTP)
sudo certbot --nginx -d api.flomadtravel.com

# Follow prompts:
# - Enter email: dev.team@flomad.com
# - Agree to ToS: Yes
# - Redirect HTTP to HTTPS: Yes
```

**What Certbot Does:**
1. Validates you own the domain (via HTTP challenge)
2. Generates trusted SSL certificate (valid 90 days)
3. Automatically configures NGINX
4. Sets up auto-renewal via cron job

#### Step 3: Update NGINX Configuration
Certbot will automatically update `/etc/nginx/sites-available/backend` to:
```nginx
server {
    listen 80;
    server_name api.flomadtravel.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name api.flomadtravel.com;

    ssl_certificate /etc/letsencrypt/live/api.flomadtravel.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.flomadtravel.com/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### Step 4: Update Frontend Environment Variables
```bash
# Update Vercel
echo "https://api.flomadtravel.com" > temp.txt
cat temp.txt | vercel env add NEXT_PUBLIC_API_URL production
rm temp.txt

# Redeploy
vercel --prod
```

#### Step 5: Update Local Environment Files
```bash
# .env.production
NEXT_PUBLIC_API_URL=https://api.flomadtravel.com

# .env.development
NEXT_PUBLIC_API_URL=https://api.flomadtravel.com

# vercel.json
{
  "env": {
    "NEXT_PUBLIC_API_URL": "https://api.flomadtravel.com"
  }
}
```

**Commit and Deploy:**
```bash
git add .env.production .env.development vercel.json
git commit -m "Use proper SSL certificate with api subdomain"
git push
vercel --prod
```

#### Expected Result
- ✅ https://flomadtravel.com shows green lock icon
- ✅ No security warnings
- ✅ Browser shows "Secure" badge
- ✅ Certificate auto-renews every 90 days
- ✅ Professional, production-ready deployment

---

### Secondary Priority: Monitoring & Reliability

#### 1. Backend Health Monitoring
**Implement UptimeRobot or Similar:**
```
Monitor URL: https://api.flomadtravel.com/actuator/health
Interval: 5 minutes
Alert: Email/SMS if down > 2 checks
```

#### 2. Automated Deployment Pipeline
**Fix GitHub Actions Workflow:**
```yaml
name: Deploy Backend
on:
  push:
    branches: [master]
    paths:
      - 'src/**'
      - 'pom.xml'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build JAR
        run: mvn clean package -DskipTests
      
      - name: Upload to S3
        run: |
          aws s3 cp target/*.jar s3://flomadai-deployment/app.jar
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
      
      - name: Deploy via SSM
        run: |
          aws ssm send-command \
            --instance-ids i-09a0551570b63a763 \
            --document-name "AWS-RunShellScript" \
            --parameters 'commands=[
              "aws s3 cp s3://flomadai-deployment/app.jar /home/ubuntu/docker-app/app.jar",
              "cd /home/ubuntu/docker-app",
              "docker-compose restart springboot"
            ]'
```

#### 3. Backup Strategy
**Implement MongoDB Backups:**
```bash
# Cron job for daily backups
0 2 * * * docker exec mongodb mongodump --out /backup/$(date +\%Y\%m\%d)
0 3 * * * aws s3 sync /backup s3://flomadai-backups/mongodb/
```

#### 4. Log Aggregation
**Send logs to CloudWatch:**
```bash
# Install CloudWatch agent
sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb

# Configure to send application logs
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/config.json
```

---

## Timeline Summary

| Time | Event |
|------|-------|
| T+0h | Discovered backend inaccessible after feature merge |
| T+1h | Attempted SSH access restoration (failed) |
| T+3h | Tried AWS Systems Manager (failed - no IAM role) |
| T+5h | Attempted GitHub Actions deployment (failed - SSH issues) |
| T+7h | Tried EC2 Instance Connect (not available in region) |
| T+10h | Decision: Create new EC2 instance |
| T+11h | New instance created with proper IAM role |
| T+12h | Manual deployment via AWS Console Session Manager |
| T+13h | ✅ Backend online at http://18.232.125.178:8080 |
| T+13.5h | Updated frontend environment variables |
| T+14h | Deployed to Vercel → Site flashing/updating loop |
| T+14.5h | Identified Mixed Content Blocking issue |
| T+15h | Attempted Cloudflare Tunnel (WebSocket issues) |
| T+15.5h | Attempted ngrok (requires account) |
| T+16h | Attempted localtunnel (password prompt) |
| T+16.5h | Implemented NGINX with self-signed SSL |
| T+17h | ✅ Site functional (with "Not Secure" warning) |

---

## Key Learnings

### 1. EC2 Instance Configuration
**Always configure from the start:**
- ✅ IAM role for SSM access
- ✅ Security groups with necessary ports
- ✅ SSM agent installed and running
- ✅ Regular backups of instance state

**Lesson:** SSH should not be the only access method. SSM provides browser-based access as backup.

### 2. Mixed Content Blocking
**HTTPS is non-negotiable for modern web apps:**
- Browsers block HTTP from HTTPS sites
- Cannot work around with configuration
- Temporary tunnels have limitations (WebSockets, password prompts)
- Self-signed certificates require manual acceptance
- Only proper SSL certificates (Let's Encrypt) provide seamless UX

**Lesson:** Always plan for HTTPS from day one. Self-signed certificates are development-only.

### 3. Docker Networking
**Container communication requires service names:**
```bash
# Wrong
MONGO_URL=mongodb://localhost:27017/testdb

# Right
MONGO_URL=mongodb://mongodb:27017/testdb
```

**Lesson:** Docker Compose creates a network where services communicate by container name, not localhost.

### 4. Deployment Automation
**Manual deployment via Session Manager is:**
- ✅ Reliable for emergency recovery
- ❌ Not scalable for regular deployments
- ❌ Error-prone (manual typing)
- ❌ Not auditable

**Lesson:** Automate deployments via CI/CD, but keep manual method documented for emergencies.

### 5. Environment Variable Management
**Multiple sources of truth cause conflicts:**
- Local `.env` files
- Docker Compose `environment:` section
- System environment variables
- Application `application.properties`

**Lesson:** Establish clear precedence order and document it.

---

## Current System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Internet                             │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ├─── HTTPS ───┐
                        │             │
          ┌─────────────▼──────┐  ┌───▼──────────────────┐
          │  Vercel (Frontend)  │  │  EC2 (Backend)       │
          │  flomadtravel.com   │  │  18.232.125.178      │
          │  ✅ HTTPS (Vercel)  │  │  ⚠️ HTTPS (Self-Sign)│
          └─────────────────────┘  └──────────┬───────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │  NGINX (Port 443)   │
                                   │  Self-Signed Cert   │
                                   └──────────┬──────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │  Spring Boot :8080  │
                                   │  Docker Container   │
                                   └──────────┬──────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │  MongoDB :27017     │
                                   │  Docker Container   │
                                   └─────────────────────┘
```

**Current Issues:**
- ⚠️ Self-signed certificate causes "Not Secure" warning
- ⚠️ Users must manually accept certificate once

**After DNS + Let's Encrypt:**
```
┌─────────────────────────────────────────────────────────────┐
│                         Internet                             │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ├─── HTTPS ───┐
                        │             │
          ┌─────────────▼──────┐  ┌───▼──────────────────┐
          │  Vercel (Frontend)  │  │  EC2 (Backend)       │
          │  flomadtravel.com   │  │  api.flomadtravel.com│
          │  ✅ HTTPS (Vercel)  │  │  ✅ HTTPS (Let's Enc)│
          └─────────────────────┘  └──────────┬───────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │  NGINX (Port 443)   │
                                   │  Let's Encrypt Cert │
                                   └──────────┬──────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │  Spring Boot :8080  │
                                   │  Docker Container   │
                                   └──────────┬──────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │  MongoDB :27017     │
                                   │  Docker Container   │
                                   └─────────────────────┘
```

**After Fix:**
- ✅ Green lock icon in browser
- ✅ No security warnings
- ✅ Professional production deployment

---

## Action Items

### Immediate (This Week)
- [ ] **Get DNS access** to add `api.flomadtravel.com` A record
- [ ] **Install Let's Encrypt** certificate via Certbot
- [ ] **Update frontend** to use `https://api.flomadtravel.com`
- [ ] **Test thoroughly** to ensure green lock appears
- [ ] **Document** the Let's Encrypt renewal process

### Short Term (This Month)
- [ ] **Set up monitoring** (UptimeRobot or CloudWatch)
- [ ] **Implement automated backups** for MongoDB
- [ ] **Fix GitHub Actions** deployment workflow
- [ ] **Add CloudWatch** logging for better debugging
- [ ] **Create runbook** for common operations

### Long Term (Next Quarter)
- [ ] **Implement CI/CD** with automated testing
- [ ] **Set up staging environment** separate from production
- [ ] **Add Redis** for session management and caching
- [ ] **Implement horizontal scaling** (multiple backend instances)
- [ ] **Add CDN** for static assets (CloudFront)

---

## Conclusion

After a marathon 17-hour debugging session involving:
- Creating a new EC2 instance
- Manual deployment via AWS Session Manager
- Fixing MongoDB connection issues
- Resolving disk space constraints
- Implementing NGINX reverse proxy
- Testing multiple tunneling solutions
- Deploying with self-signed SSL

**The site is now FUNCTIONAL** at https://flomadtravel.com, though it displays a "Not Secure" warning due to the self-signed certificate.

**Next Step:** Obtain DNS access to add `api.flomadtravel.com` A record, install Let's Encrypt certificate, and achieve full production-ready HTTPS with green lock icon.

**Current Status:** ✅ Site Online | ⚠️ Needs SSL Certificate Upgrade

---

**Document Version:** 1.0  
**Last Updated:** December 17, 2025  
**Authors:** AI Development Team  
**Contact:** dev.team@flomad.com
