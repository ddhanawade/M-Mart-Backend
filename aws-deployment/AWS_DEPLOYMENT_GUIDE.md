# 🚀 M-Mart Backend AWS Deployment Guide

## 📋 Architecture Overview

Your M-Mart Backend consists of:
- **8 Microservices**: API Gateway, Config Server, Service Discovery, User, Product, Cart, Order, Payment, Notification
- **Infrastructure**: MySQL, Redis, Kafka + Zookeeper
- **Features**: JWT Authentication, Event Streaming, Caching, Service Discovery

## 💰 Cost-Effective AWS Architecture

### **Recommended Architecture: ECS Fargate + Managed Services**

```
┌─────────────────────────────────────────────────────────────┐
│                     Internet Gateway                        │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│              Application Load Balancer                      │
│                    (ALB)                                    │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                  ECS Fargate                                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │ API Gateway │ │Config Server│ │  Service Discovery  │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │User Service │ │Product Svc  │ │    Cart Service     │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │Order Service│ │Payment Svc  │ │ Notification Service│   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                 Managed Services                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │   RDS MySQL │ │ElastiCache  │ │      MSK Kafka      │   │
│  │             │ │   Redis     │ │                     │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 💵 Cost Breakdown (Monthly Estimates)

### **Development Environment (~$150-200/month)**
- **ECS Fargate**: 8 services × 0.25 vCPU × $0.04048/hour = ~$60
- **RDS MySQL**: db.t3.micro = ~$15
- **ElastiCache Redis**: cache.t3.micro = ~$12
- **MSK Kafka**: kafka.t3.small (2 brokers) = ~$50
- **ALB**: $16.20 + data processing = ~$20
- **ECR**: 10GB storage = ~$1
- **Data Transfer**: ~$10

### **Production Environment (~$400-600/month)**
- **ECS Fargate**: 8 services × 0.5 vCPU × $0.04048/hour = ~$120
- **RDS MySQL**: db.t3.small with Multi-AZ = ~$60
- **ElastiCache Redis**: cache.t3.small = ~$25
- **MSK Kafka**: kafka.m5.large (3 brokers) = ~$200
- **ALB**: $16.20 + data processing = ~$30
- **Auto Scaling**: Additional capacity = ~$50
- **Monitoring & Logs**: ~$20

## 🏗️ Step-by-Step Deployment Guide

### **Phase 1: Prerequisites & Setup**

#### **1.1 AWS Account Setup**
```bash
# Install AWS CLI
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /

# Configure AWS CLI
aws configure
# Enter: Access Key ID, Secret Access Key, Region (us-east-1), Output format (json)
```

#### **1.2 Install Required Tools**
```bash
# Install Terraform
brew install terraform

# Install Docker (if not already installed)
brew install docker

# Install AWS CDK (optional)
npm install -g aws-cdk
```

#### **1.3 Create IAM Roles and Policies**
```bash
# Create deployment user with necessary permissions
aws iam create-user --user-name mmart-deployer
aws iam attach-user-policy --user-name mmart-deployer --policy-arn arn:aws:iam::aws:policy/AmazonECS_FullAccess
aws iam attach-user-policy --user-name mmart-deployer --policy-arn arn:aws:iam::aws:policy/AmazonRDSFullAccess
aws iam attach-user-policy --user-name mmart-deployer --policy-arn arn:aws:iam::aws:policy/ElastiCacheFullAccess
aws iam attach-user-policy --user-name mmart-deployer --policy-arn arn:aws:iam::aws:policy/AmazonMSKFullAccess
```

### **Phase 2: Infrastructure Setup with Terraform**

#### **2.1 VPC and Networking**
```hcl
# Create VPC with public and private subnets
# Enable NAT Gateway for private subnet internet access
# Configure security groups for each service
```

#### **2.2 Managed Services Setup**
```bash
# RDS MySQL
aws rds create-db-instance \
  --db-instance-identifier mmart-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0 \
  --allocated-storage 20 \
  --db-name mahabaleshwer_mart \
  --master-username admin \
  --master-user-password YourSecurePassword123

# ElastiCache Redis
aws elasticache create-cache-cluster \
  --cache-cluster-id mmart-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1

# MSK Kafka Cluster
aws kafka create-cluster \
  --cluster-name mmart-kafka \
  --broker-node-group-info instanceType=kafka.t3.small,clientSubnets=subnet-xxx,subnet-yyy \
  --kafka-version 2.8.1 \
  --number-of-broker-nodes 2
```

### **Phase 3: Container Registry Setup**

#### **3.1 Create ECR Repositories**
```bash
# Create ECR repositories for each service
services=("api-gateway" "config-server" "service-discovery" "user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service")

for service in "${services[@]}"; do
  aws ecr create-repository --repository-name mmart/$service
done

# Get ECR login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
```

#### **3.2 Build and Push Docker Images**
```bash
# Build all services
mvn clean package -DskipTests

# Build and push each service
for service in "${services[@]}"; do
  docker build -t mmart/$service -f $service/Dockerfile .
  docker tag mmart/$service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/mmart/$service:latest
  docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/mmart/$service:latest
done
```

### **Phase 4: ECS Cluster Setup**

#### **4.1 Create ECS Cluster**
```bash
aws ecs create-cluster --cluster-name mmart-cluster --capacity-providers FARGATE
```

#### **4.2 Create Task Definitions**
```json
{
  "family": "mmart-api-gateway",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::<account>:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "api-gateway",
      "image": "<account>.dkr.ecr.us-east-1.amazonaws.com/mmart/api-gateway:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        },
        {
          "name": "CONFIG_SERVER_URI",
          "value": "http://config-server.mmart.local:8888"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/mmart-api-gateway",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

### **Phase 5: CI/CD Pipeline for Private Repositories**

#### **5.1 GitHub Actions Workflow**
```yaml
name: Deploy M-Mart Backend

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1
    
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1
    
    - name: Build and push Docker images
      env:
        ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
        ECR_REPOSITORY: mmart
        IMAGE_TAG: ${{ github.sha }}
      run: |
        # Build all services
        mvn clean package -DskipTests
        
        # Build and push each service
        services=("api-gateway" "config-server" "service-discovery" "user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service")
        
        for service in "${services[@]}"; do
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY/$service:$IMAGE_TAG -f $service/Dockerfile .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY/$service:$IMAGE_TAG
        done
    
    - name: Deploy to ECS
      run: |
        # Update ECS services with new image tags
        services=("api-gateway" "config-server" "service-discovery" "user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service")
        
        for service in "${services[@]}"; do
          aws ecs update-service --cluster mmart-cluster --service mmart-$service --force-new-deployment
        done
```

#### **5.2 AWS CodeBuild for Private Repos**
```yaml
version: 0.2

phases:
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
      
  build:
    commands:
      - echo Build started on `date`
      - echo Building the Docker images...
      - mvn clean package -DskipTests
      
      # Build all services
      - services=("api-gateway" "config-server" "service-discovery" "user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service")
      
      - for service in "${services[@]}"; do
          docker build -t $IMAGE_REPO_NAME/$service:$IMAGE_TAG -f $service/Dockerfile . ;
          docker tag $IMAGE_REPO_NAME/$service:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME/$service:$IMAGE_TAG ;
        done
      
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Pushing the Docker images...
      
      - for service in "${services[@]}"; do
          docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME/$service:$IMAGE_TAG ;
        done
```

### **Phase 6: Service Discovery & Load Balancing**

#### **6.1 Application Load Balancer Setup**
```bash
# Create ALB
aws elbv2 create-load-balancer \
  --name mmart-alb \
  --subnets subnet-xxx subnet-yyy \
  --security-groups sg-xxx

# Create target groups for each service
aws elbv2 create-target-group \
  --name mmart-api-gateway-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id vpc-xxx \
  --target-type ip \
  --health-check-path /actuator/health
```

#### **6.2 Service Discovery with AWS Cloud Map**
```bash
# Create private DNS namespace
aws servicediscovery create-private-dns-namespace \
  --name mmart.local \
  --vpc vpc-xxx

# Create services for each microservice
aws servicediscovery create-service \
  --name config-server \
  --namespace-id ns-xxx \
  --dns-config NamespaceId=ns-xxx,DnsRecords=[{Type=A,TTL=60}]
```

### **Phase 7: Environment Configuration**

#### **7.1 AWS Systems Manager Parameter Store**
```bash
# Store configuration parameters securely
aws ssm put-parameter \
  --name "/mmart/mysql/host" \
  --value "mmart-mysql.xxx.us-east-1.rds.amazonaws.com" \
  --type "String"

aws ssm put-parameter \
  --name "/mmart/mysql/password" \
  --value "YourSecurePassword123" \
  --type "SecureString"

aws ssm put-parameter \
  --name "/mmart/jwt/secret" \
  --value "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" \
  --type "SecureString"
```

#### **7.2 Update Application Properties**
```properties
# application-aws.properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST}:3306/${MYSQL_DATABASE}
spring.datasource.username=${MYSQL_USERNAME}
spring.datasource.password=${MYSQL_PASSWORD}

spring.redis.host=${REDIS_HOST}
spring.redis.port=6379

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}

eureka.client.service-url.defaultZone=${EUREKA_SERVER_URL}
```

### **Phase 8: Monitoring & Logging**

#### **8.1 CloudWatch Setup**
```bash
# Create log groups for each service
services=("api-gateway" "config-server" "service-discovery" "user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service")

for service in "${services[@]}"; do
  aws logs create-log-group --log-group-name /ecs/mmart-$service
done
```

#### **8.2 CloudWatch Alarms**
```bash
# CPU utilization alarm
aws cloudwatch put-metric-alarm \
  --alarm-name "mmart-high-cpu" \
  --alarm-description "Alarm when CPU exceeds 70%" \
  --metric-name CPUUtilization \
  --namespace AWS/ECS \
  --statistic Average \
  --period 300 \
  --threshold 70 \
  --comparison-operator GreaterThanThreshold
```

## 🔒 Security Best Practices

### **1. Network Security**
- Use private subnets for ECS tasks
- Configure security groups with minimal required access
- Enable VPC Flow Logs

### **2. Secrets Management**
- Store sensitive data in AWS Systems Manager Parameter Store
- Use IAM roles for service-to-service authentication
- Rotate secrets regularly

### **3. Container Security**
- Scan Docker images for vulnerabilities
- Use minimal base images
- Run containers as non-root users

## 🚀 Deployment Commands

### **Quick Deployment Script**
```bash
#!/bin/bash

# Set variables
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID="your-account-id"
CLUSTER_NAME="mmart-cluster"

# Build and deploy all services
echo "Building and deploying M-Mart Backend..."

# Build Maven project
mvn clean package -DskipTests

# Services array
services=("api-gateway" "config-server" "service-discovery" "user-service" "product-service" "cart-service" "order-service" "payment-service" "notification-service")

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build and push images
for service in "${services[@]}"; do
  echo "Building and pushing $service..."
  
  docker build -t mmart/$service -f $service/Dockerfile .
  docker tag mmart/$service:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/mmart/$service:latest
  docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/mmart/$service:latest
  
  echo "Updating ECS service for $service..."
  aws ecs update-service --cluster $CLUSTER_NAME --service mmart-$service --force-new-deployment
done

echo "Deployment completed!"
```

## 📊 Cost Optimization Tips

### **1. Right-sizing Resources**
- Start with smaller instance types (t3.micro, t3.small)
- Use AWS Compute Optimizer recommendations
- Implement auto-scaling based on metrics

### **2. Reserved Instances**
- Purchase RDS Reserved Instances for 1-3 years
- Use Savings Plans for ECS Fargate

### **3. Spot Instances**
- Use Spot Instances for non-critical workloads
- Implement graceful shutdown handling

### **4. Data Transfer Optimization**
- Use CloudFront for static content
- Minimize cross-AZ data transfer
- Implement data compression

## 🔧 Troubleshooting Guide

### **Common Issues**

1. **Service Discovery Issues**
   - Check security group rules
   - Verify DNS resolution in Cloud Map
   - Check ECS service logs

2. **Database Connection Issues**
   - Verify RDS security groups
   - Check parameter store values
   - Test connectivity from ECS tasks

3. **Load Balancer Issues**
   - Check target group health
   - Verify security group rules
   - Check ALB access logs

## 📈 Scaling Strategy

### **Horizontal Scaling**
```bash
# Update ECS service desired count
aws ecs update-service \
  --cluster mmart-cluster \
  --service mmart-api-gateway \
  --desired-count 3
```

### **Auto Scaling**
```bash
# Create auto scaling target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/mmart-cluster/mmart-api-gateway \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 1 \
  --max-capacity 10
```

---

This guide provides a complete, production-ready deployment strategy for your M-Mart Backend on AWS with cost optimization and security best practices. The estimated monthly cost for a development environment is around $150-200, making it very affordable for testing and development purposes.
