#!/usr/bin/env python3
"""
JWT Token Generator for M-Mart Backend Testing
Generates valid JWT tokens for testing payment APIs
"""

import jwt
import datetime
import json

# JWT Configuration from docker-compose.yml
JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
ALGORITHM = "HS256"

def generate_jwt_token(user_email="test@example.com", user_id="1", roles=["CUSTOMER"], expiry_hours=24):
    """
    Generate a valid JWT token for testing
    """
    # Current time
    now = datetime.datetime.utcnow()
    
    # Token payload
    payload = {
        "sub": user_email,  # Subject (user email)
        "userId": user_id,
        "iat": int(now.timestamp()),  # Issued at
        "exp": int((now + datetime.timedelta(hours=expiry_hours)).timestamp()),  # Expiry
        "roles": roles,
        "iss": "mahabaleshwer-mart-backend",  # Issuer
        "aud": "mahabaleshwer-mart-users"     # Audience
    }
    
    # Generate token
    token = jwt.encode(payload, JWT_SECRET, algorithm=ALGORITHM)
    
    return token, payload

def main():
    print("🔑 M-Mart Backend JWT Token Generator")
    print("=" * 50)
    
    # Generate customer token
    customer_token, customer_payload = generate_jwt_token(
        user_email="customer@example.com",
        user_id="1",
        roles=["CUSTOMER"],
        expiry_hours=24
    )
    
    # Generate admin token
    admin_token, admin_payload = generate_jwt_token(
        user_email="admin@example.com", 
        user_id="2",
        roles=["ADMIN"],
        expiry_hours=24
    )
    
    print("\n✅ CUSTOMER JWT TOKEN:")
    print("-" * 30)
    print(f"Token: {customer_token}")
    print(f"Payload: {json.dumps(customer_payload, indent=2)}")
    
    print("\n✅ ADMIN JWT TOKEN:")
    print("-" * 30)
    print(f"Token: {admin_token}")
    print(f"Payload: {json.dumps(admin_payload, indent=2)}")
    
    print("\n🧪 TESTING COMMANDS:")
    print("-" * 30)
    print("Test Payment Initiation (Customer):")
    print(f'curl -X POST "http://localhost:8084/api/orders/8a6458d6-12b7-42f7-a349-78124cc9a121/payment/initiate?paymentMethod=WALLET&gatewayProvider=RAZORPAY" \\')
    print(f'  -H "Content-Type: application/json" \\')
    print(f'  -H "Authorization: Bearer {customer_token}" \\')
    print(f'  -s | jq .')
    
    print(f"\nTest Payment Initiation (Admin):")
    print(f'curl -X POST "http://localhost:8084/api/orders/8a6458d6-12b7-42f7-a349-78124cc9a121/payment/initiate?paymentMethod=WALLET&gatewayProvider=RAZORPAY" \\')
    print(f'  -H "Content-Type: application/json" \\')
    print(f'  -H "Authorization: Bearer {admin_token}" \\')
    print(f'  -s | jq .')

if __name__ == "__main__":
    main()
