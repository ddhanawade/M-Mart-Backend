# 🚀 M-Mart Backend Single EC2 Deployment Guide

## 💰 Cost Comparison: ECS vs Single EC2

### **ECS Fargate Deployment (Previous Estimate)**
- **Monthly Cost**: ~$150-200 (dev) / ~$400-600 (prod)
- **Components**: ECS Fargate + RDS + ElastiCache + MSK + ALB + ECR

### **Single EC2 Deployment (New Approach)**
- **Monthly Cost**: ~$30-50 (dev) / ~$80-120 (prod)
- **Cost Savings**: **75-80% reduction!**

## 📊 Detailed Cost Breakdown

### **Development Environment (~$30-50/month)**
```
EC2 Instance (t3.large):     ~$60/month
- With Reserved Instance:    ~$35/month (1-year term)
- With Spot Instance:        ~$18/month (if available)

EBS Storage (50GB):          ~$5/month
Data Transfer:               ~$5/month
Elastic IP:                  ~$3.6/month
CloudWatch Logs:             ~$2/month

Total with Reserved:         ~$50/month
Total with Spot:             ~$33/month
```

### **Production Environment (~$80-120/month)**
```
EC2 Instance (t3.xlarge):    ~$150/month
- With Reserved Instance:    ~$90/month (1-year term)

EBS Storage (100GB SSD):     ~$10/month
Backup Storage (50GB):       ~$2.5/month
Data Transfer:               ~$10/month
Elastic IP:                  ~$3.6/month
CloudWatch + Monitoring:     ~$5/month

Total with Reserved:         ~$121/month
```

## 🏗️ Single EC2 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Internet Gateway                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 Elastic IP                                  │
│              Security Groups                                │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                Single EC2 Instance                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Docker Compose                         │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │   │
│  │  │   Nginx     │ │API Gateway  │ │Config Server│   │   │
│  │  │(Reverse     │ │             │ │             │   │   │
│  │  │ Proxy)      │ │             │ │             │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘   │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │   │
│  │  │User Service │ │Product Svc  │ │Cart Service │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘   │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │   │
│  │  │Order Service│ │Payment Svc  │ │Notification │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘   │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │   │
│  │  │   MySQL     │ │   Redis     │ │   Kafka     │   │   │
│  │  │ (Container) │ │(Container)  │ │(Container)  │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Monitoring Stack                       │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │   │
│  │  │ Prometheus  │ │   Grafana   │ │   Loki      │   │   │
│  │  │             │ │             │ │   (Logs)    │   │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🔒 Security Considerations & Mitigation Strategies

### **Security Concerns**

#### **1. Single Point of Failure**
- **Risk**: All services on one instance
- **Mitigation**: 
  - Automated backups every 6 hours
  - AMI snapshots daily
  - Health monitoring with auto-restart
  - Quick disaster recovery procedures

#### **2. Network Security**
- **Risk**: All services exposed on same network interface
- **Mitigation**:
  - Strict security groups (only necessary ports)
  - Internal Docker network isolation
  - Nginx reverse proxy with rate limiting
  - Fail2ban for intrusion prevention

#### **3. Container Isolation**
- **Risk**: Container breakout could compromise entire system
- **Mitigation**:
  - Run containers as non-root users
  - Use Docker security profiles
  - Regular security updates
  - Container resource limits

#### **4. Data Security**
- **Risk**: Database and cache on same instance
- **Mitigation**:
  - Encrypted EBS volumes
  - Regular automated backups
  - Database access controls
  - Secrets management with AWS Systems Manager

#### **5. Access Control**
- **Risk**: SSH access to production instance
- **Mitigation**:
  - SSH key-based authentication only
  - AWS Systems Manager Session Manager
  - Bastion host for production access
  - Multi-factor authentication

### **Security Implementation**

#### **Network Security Groups**
```bash
# HTTP/HTTPS Traffic
Port 80 (HTTP) - 0.0.0.0/0
Port 443 (HTTPS) - 0.0.0.0/0

# SSH Access (Restricted)
Port 22 (SSH) - Your IP only / VPN CIDR

# Application Ports (Internal only)
Ports 8080-8090 - Internal Docker network only

# Database Ports (Internal only)
Port 3306 (MySQL) - Internal Docker network only
Port 6379 (Redis) - Internal Docker network only
Port 9092 (Kafka) - Internal Docker network only
```

#### **Docker Security Configuration**
```yaml
# Security-hardened Docker Compose
version: '3.8'
services:
  api-gateway:
    security_opt:
      - no-new-privileges:true
    user: "1000:1000"
    read_only: true
    tmpfs:
      - /tmp:noexec,nosuid,size=100m
    cap_drop:
      - ALL
    cap_add:
      - NET_BIND_SERVICE
```

## 📋 Step-by-Step Deployment Guide

### **Phase 1: EC2 Instance Setup**

#### **1.1 Launch EC2 Instance**
```bash
# Create security group
aws ec2 create-security-group \
  --group-name mmart-single-ec2-sg \
  --description "M-Mart Backend Single EC2 Security Group"

# Add inbound rules
aws ec2 authorize-security-group-ingress \
  --group-name mmart-single-ec2-sg \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
  --group-name mmart-single-ec2-sg \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
  --group-name mmart-single-ec2-sg \
  --protocol tcp \
  --port 22 \
  --cidr YOUR_IP/32

# Launch instance
aws ec2 run-instances \
  --image-id ami-0c02fb55956c7d316 \
  --count 1 \
  --instance-type t3.large \
  --key-name your-key-pair \
  --security-groups mmart-single-ec2-sg \
  --block-device-mappings DeviceName=/dev/xvda,Ebs='{VolumeSize=50,VolumeType=gp3,Encrypted=true}' \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=M-Mart-Backend}]'
```

#### **1.2 Allocate Elastic IP**
```bash
# Allocate Elastic IP
aws ec2 allocate-address --domain vpc

# Associate with instance
aws ec2 associate-address \
  --instance-id i-1234567890abcdef0 \
  --allocation-id eipalloc-12345678
```

### **Phase 2: Server Configuration**

#### **2.1 Connect and Update System**
```bash
# Connect to instance
ssh -i your-key.pem ec2-user@your-elastic-ip

# Update system
sudo yum update -y

# Install Docker
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install additional tools
sudo yum install -y git htop nginx certbot python3-certbot-nginx fail2ban
```

#### **2.2 Configure Security**
```bash
# Configure fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban

# Configure firewall
sudo yum install -y firewalld
sudo systemctl enable firewalld
sudo systemctl start firewalld

# Allow necessary ports
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --reload
```

### **Phase 3: Application Deployment**

#### **3.1 Clone Repository**
```bash
# Clone your private repository
git clone https://github.com/your-username/mahabaleshwer-mart-backend.git
cd mahabaleshwer-mart-backend
```

#### **3.2 Configure Environment**
```bash
# Create production environment file
cat > .env.prod << EOF
# Environment
SPRING_PROFILES_ACTIVE=prod
ENVIRONMENT=production

# Database Configuration
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=mahabaleshwer_mart
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_secure_password_here

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Security
JWT_SECRET=your_jwt_secret_here
CONFIG_SERVER_USERNAME=config-user
CONFIG_SERVER_PASSWORD=config-pass
EUREKA_SERVER_USERNAME=eureka-user
EUREKA_SERVER_PASSWORD=eureka-pass

# Email Configuration
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Payment Configuration (Mock mode for development)
PAYMENT_GATEWAY_ENABLED=false
PAYMENT_MOCK_MODE=true
EOF
```

### **Phase 4: Docker Compose Configuration**

#### **4.1 Create Production Docker Compose**
```yaml
# docker-compose.prod.yml
version: '3.8'

networks:
  mmart-network:
    driver: bridge

volumes:
  mysql_data:
  redis_data:
  kafka_data:
  prometheus_data:
  grafana_data:

services:
  # Reverse Proxy
  nginx:
    image: nginx:alpine
    container_name: mmart-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
    depends_on:
      - api-gateway
    networks:
      - mmart-network
    restart: unless-stopped

  # Infrastructure Services
  mysql:
    image: mysql:8.0
    container_name: mmart-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql
    networks:
      - mmart-network
    restart: unless-stopped
    security_opt:
      - no-new-privileges:true

  redis:
    image: redis:7-alpine
    container_name: mmart-redis
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD:-}
    volumes:
      - redis_data:/data
    networks:
      - mmart-network
    restart: unless-stopped
    security_opt:
      - no-new-privileges:true

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: mmart-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - mmart-network
    restart: unless-stopped

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: mmart-kafka
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
    volumes:
      - kafka_data:/var/lib/kafka/data
    networks:
      - mmart-network
    restart: unless-stopped

  # Application Services (same as your existing configuration)
  config-server:
    build:
      context: .
      dockerfile: config-server/Dockerfile
    container_name: mmart-config-server
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
      - CONFIG_SERVER_USERNAME=${CONFIG_SERVER_USERNAME}
      - CONFIG_SERVER_PASSWORD=${CONFIG_SERVER_PASSWORD}
    networks:
      - mmart-network
    restart: unless-stopped
    security_opt:
      - no-new-privileges:true

  # ... (include all other services from your docker-compose.yml)

  # Monitoring Stack
  prometheus:
    image: prom/prometheus:latest
    container_name: mmart-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - mmart-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: mmart-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - mmart-network
    restart: unless-stopped
```

### **Phase 5: Nginx Configuration**

#### **5.1 Create Nginx Configuration**
```nginx
# nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream api_gateway {
        server api-gateway:8080;
    }

    upstream grafana {
        server grafana:3000;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login:10m rate=1r/s;

    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

    server {
        listen 80;
        server_name your-domain.com;

        # Redirect HTTP to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;

        # API Gateway
        location /api/ {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://api_gateway;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Authentication endpoints
        location /auth/ {
            limit_req zone=login burst=5 nodelay;
            proxy_pass http://api_gateway;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        # Monitoring (restrict access)
        location /monitoring/ {
            allow YOUR_IP;
            deny all;
            proxy_pass http://grafana/;
        }

        # Health check
        location /health {
            proxy_pass http://api_gateway/actuator/health;
        }
    }
}
```

### **Phase 6: Deployment and Monitoring**

#### **6.1 Deploy Application**
```bash
# Build and start services
docker-compose -f docker-compose.prod.yml up -d --build

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

#### **6.2 Setup SSL Certificate**
```bash
# Install SSL certificate (Let's Encrypt)
sudo certbot --nginx -d your-domain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

#### **6.3 Setup Monitoring**
```bash
# Create monitoring configuration
mkdir -p monitoring

cat > monitoring/prometheus.yml << EOF
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'spring-boot'
    static_configs:
      - targets: ['api-gateway:8080', 'user-service:8081', 'product-service:8082']
    metrics_path: '/actuator/prometheus'
EOF
```

## 🔧 Maintenance and Operations

### **Backup Strategy**
```bash
#!/bin/bash
# backup.sh - Daily backup script

# Database backup
docker exec mmart-mysql mysqldump -u root -p${MYSQL_PASSWORD} --all-databases > /backup/mysql-$(date +%Y%m%d).sql

# Application data backup
tar -czf /backup/app-data-$(date +%Y%m%d).tar.gz /home/ec2-user/mahabaleshwer-mart-backend

# Upload to S3
aws s3 cp /backup/ s3://your-backup-bucket/ --recursive

# Cleanup old backups (keep 7 days)
find /backup -name "*.sql" -mtime +7 -delete
find /backup -name "*.tar.gz" -mtime +7 -delete
```

### **Health Monitoring**
```bash
#!/bin/bash
# health-check.sh - Health monitoring script

# Check if all containers are running
if [ $(docker ps | grep mmart | wc -l) -lt 12 ]; then
    echo "Some containers are down, restarting..."
    docker-compose -f docker-compose.prod.yml up -d
fi

# Check API health
if ! curl -f http://localhost/health > /dev/null 2>&1; then
    echo "API health check failed, restarting API Gateway..."
    docker-compose -f docker-compose.prod.yml restart api-gateway
fi
```

## 💡 Cost Optimization Tips

### **1. Reserved Instances**
- Purchase 1-year Reserved Instance for 40% savings
- Use Convertible Reserved Instances for flexibility

### **2. Spot Instances (Development)**
- Use Spot Instances for development environments
- Save up to 70% on compute costs
- Implement graceful shutdown handling

### **3. Storage Optimization**
- Use gp3 EBS volumes (20% cheaper than gp2)
- Implement log rotation and cleanup
- Compress backups before S3 upload

### **4. Network Optimization**
- Use CloudFront for static content
- Implement caching strategies
- Optimize Docker images

## 🚨 Security Best Practices

### **1. Regular Updates**
```bash
# Weekly security updates
sudo yum update -y
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

### **2. Access Control**
- Use AWS Systems Manager Session Manager instead of SSH
- Implement VPN for administrative access
- Regular access review and key rotation

### **3. Monitoring and Alerting**
- Set up CloudWatch alarms for CPU, memory, disk usage
- Monitor failed login attempts
- Implement log analysis for security events

### **4. Data Protection**
- Enable EBS encryption
- Regular security scans
- Implement secrets rotation

## 📊 Comparison Summary

| Aspect | ECS Fargate | Single EC2 |
|--------|-------------|------------|
| **Monthly Cost (Dev)** | $150-200 | $30-50 |
| **Monthly Cost (Prod)** | $400-600 | $80-120 |
| **Setup Complexity** | High | Medium |
| **Maintenance** | Low | Medium |
| **Scalability** | Excellent | Limited |
| **Security** | High | Medium-High |
| **Monitoring** | Built-in | Custom |
| **Backup** | Managed | Manual |

## 🎯 Recommendation

**For Development/Testing**: Single EC2 with Spot Instances (~$18-33/month)
**For Small Production**: Single EC2 with Reserved Instance (~$80-120/month)
**For Large Production**: ECS Fargate for better scalability and management

The single EC2 approach provides **75-80% cost savings** while maintaining all functionality, making it perfect for startups, development environments, and small to medium production workloads.
