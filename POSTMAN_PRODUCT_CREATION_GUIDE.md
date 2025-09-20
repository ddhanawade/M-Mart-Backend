# 🛍️ **Postman Product Creation Guide - Mahabaleshwer Mart**

## **📋 Product Creation Endpoint**

### **Endpoint Details:**
- **Method:** `POST`
- **URL:** `http://localhost:8082/api/products`
- **Content-Type:** `application/json`
- **Authentication:** None required (public endpoint)

---

## **🍎 Sample Product Data for Postman Testing**

### **1. Fresh Apple (Fruits Category)**
```json
{
  "name": "Fresh Red Apples",
  "description": "Premium quality fresh red apples, crisp and sweet. Perfect for snacking or cooking.",
  "price": 120.00,
  "originalPrice": 150.00,
  "category": "FRUITS",
  "subcategory": "Fresh Fruits",
  "image": "https://example.com/images/red-apples.jpg",
  "images": [
    "https://example.com/images/red-apples-1.jpg",
    "https://example.com/images/red-apples-2.jpg"
  ],
  "inStock": true,
  "quantity": 50,
  "unit": "kg",
  "rating": 4.5,
  "reviewCount": 25,
  "organic": true,
  "fresh": true,
  "discount": 30.00,
  "discountPercentage": 20.00,
  "featured": true,
  "sku": "FRU-APP-001",
  "barcode": "1234567890123",
  "weightKg": 1.0,
  "shelfLifeDays": 7,
  "storageInstructions": "Store in cool, dry place. Refrigerate for longer freshness.",
  "originCountry": "India",
  "supplierName": "Fresh Farms Ltd",
  "nutritionalInfo": {
    "caloriesPer100g": 52,
    "proteinG": 0.3,
    "carbsG": 14.0,
    "fatG": 0.2,
    "fiberG": 2.4,
    "vitamins": ["Vitamin C", "Vitamin K", "Potassium"]
  }
}
```

### **2. Organic Basmati Rice (Grains Category)**
```json
{
  "name": "Organic Basmati Rice",
  "description": "Premium quality organic basmati rice with long grains and aromatic fragrance. Aged for perfect texture.",
  "price": 180.00,
  "originalPrice": 200.00,
  "category": "GRAINS",
  "subcategory": "Rice",
  "image": "https://example.com/images/basmati-rice.jpg",
  "images": [
    "https://example.com/images/basmati-rice-1.jpg",
    "https://example.com/images/basmati-rice-2.jpg"
  ],
  "inStock": true,
  "quantity": 100,
  "unit": "kg",
  "rating": 4.8,
  "reviewCount": 45,
  "organic": true,
  "fresh": false,
  "discount": 20.00,
  "discountPercentage": 10.00,
  "featured": true,
  "sku": "GRA-BAS-002",
  "barcode": "2345678901234",
  "weightKg": 1.0,
  "shelfLifeDays": 365,
  "storageInstructions": "Store in airtight container in cool, dry place away from moisture.",
  "originCountry": "India",
  "supplierName": "Organic Grains Co",
  "nutritionalInfo": {
    "caloriesPer100g": 345,
    "proteinG": 7.1,
    "carbsG": 78.0,
    "fatG": 0.9,
    "fiberG": 1.3,
    "vitamins": ["Thiamine", "Niacin", "Iron"]
  }
}
```

### **3. Fresh Milk (Dairy Category)**
```json
{
  "name": "Fresh Whole Milk",
  "description": "Pure and fresh whole milk from grass-fed cows. Rich in calcium and protein.",
  "price": 60.00,
  "originalPrice": 60.00,
  "category": "DAIRY",
  "subcategory": "Milk",
  "image": "https://example.com/images/fresh-milk.jpg",
  "images": [
    "https://example.com/images/fresh-milk-1.jpg"
  ],
  "inStock": true,
  "quantity": 30,
  "unit": "liter",
  "rating": 4.6,
  "reviewCount": 35,
  "organic": false,
  "fresh": true,
  "discount": 0.00,
  "discountPercentage": 0.00,
  "featured": false,
  "sku": "DAI-MIL-003",
  "barcode": "3456789012345",
  "weightKg": 1.03,
  "shelfLifeDays": 3,
  "storageInstructions": "Keep refrigerated at 4°C or below. Consume within 3 days of opening.",
  "originCountry": "India",
  "supplierName": "Pure Dairy Farms",
  "nutritionalInfo": {
    "caloriesPer100g": 61,
    "proteinG": 3.2,
    "carbsG": 4.8,
    "fatG": 3.3,
    "fiberG": 0.0,
    "vitamins": ["Vitamin D", "Vitamin B12", "Calcium", "Riboflavin"]
  }
}
```

### **4. Organic Spinach (Vegetables Category)**
```json
{
  "name": "Organic Fresh Spinach",
  "description": "Fresh organic spinach leaves, rich in iron and vitamins. Perfect for salads and cooking.",
  "price": 40.00,
  "originalPrice": 50.00,
  "category": "VEGETABLES",
  "subcategory": "Leafy Greens",
  "image": "https://example.com/images/spinach.jpg",
  "images": [
    "https://example.com/images/spinach-1.jpg",
    "https://example.com/images/spinach-2.jpg"
  ],
  "inStock": true,
  "quantity": 25,
  "unit": "bunch",
  "rating": 4.3,
  "reviewCount": 18,
  "organic": true,
  "fresh": true,
  "discount": 10.00,
  "discountPercentage": 20.00,
  "featured": false,
  "sku": "VEG-SPI-004",
  "barcode": "4567890123456",
  "weightKg": 0.25,
  "shelfLifeDays": 5,
  "storageInstructions": "Store in refrigerator. Wash before use.",
  "originCountry": "India",
  "supplierName": "Green Leaf Organics",
  "nutritionalInfo": {
    "caloriesPer100g": 23,
    "proteinG": 2.9,
    "carbsG": 3.6,
    "fatG": 0.4,
    "fiberG": 2.2,
    "vitamins": ["Vitamin K", "Vitamin A", "Folate", "Iron", "Vitamin C"]
  }
}
```

### **5. Turmeric Powder (Spices Category)**
```json
{
  "name": "Pure Turmeric Powder",
  "description": "Premium quality pure turmeric powder with high curcumin content. Essential for Indian cooking.",
  "price": 80.00,
  "originalPrice": 90.00,
  "category": "SPICES",
  "subcategory": "Ground Spices",
  "image": "https://example.com/images/turmeric-powder.jpg",
  "images": [
    "https://example.com/images/turmeric-powder-1.jpg"
  ],
  "inStock": true,
  "quantity": 75,
  "unit": "grams",
  "rating": 4.7,
  "reviewCount": 52,
  "organic": true,
  "fresh": false,
  "discount": 10.00,
  "discountPercentage": 11.11,
  "featured": true,
  "sku": "SPI-TUR-005",
  "barcode": "5678901234567",
  "weightKg": 0.1,
  "shelfLifeDays": 730,
  "storageInstructions": "Store in airtight container away from light and moisture.",
  "originCountry": "India",
  "supplierName": "Spice Masters Ltd",
  "nutritionalInfo": {
    "caloriesPer100g": 354,
    "proteinG": 7.8,
    "carbsG": 64.9,
    "fatG": 9.9,
    "fiberG": 21.1,
    "vitamins": ["Curcumin", "Iron", "Manganese", "Vitamin B6"]
  }
}
```

### **6. Green Tea (Beverages Category)**
```json
{
  "name": "Premium Green Tea",
  "description": "High-quality green tea leaves with antioxidants. Perfect for a healthy lifestyle.",
  "price": 250.00,
  "originalPrice": 300.00,
  "category": "BEVERAGES",
  "subcategory": "Tea",
  "image": "https://example.com/images/green-tea.jpg",
  "images": [
    "https://example.com/images/green-tea-1.jpg",
    "https://example.com/images/green-tea-2.jpg"
  ],
  "inStock": true,
  "quantity": 40,
  "unit": "box",
  "rating": 4.4,
  "reviewCount": 28,
  "organic": true,
  "fresh": false,
  "discount": 50.00,
  "discountPercentage": 16.67,
  "featured": true,
  "sku": "BEV-GTE-006",
  "barcode": "6789012345678",
  "weightKg": 0.1,
  "shelfLifeDays": 720,
  "storageInstructions": "Store in cool, dry place away from strong odors.",
  "originCountry": "India",
  "supplierName": "Tea Gardens Ltd",
  "nutritionalInfo": {
    "caloriesPer100g": 1,
    "proteinG": 0.2,
    "carbsG": 0.0,
    "fatG": 0.0,
    "fiberG": 0.0,
    "vitamins": ["Antioxidants", "Catechins", "Vitamin C"]
  }
}
```

### **7. Organic Almonds (Snacks Category)**
```json
{
  "name": "Organic Raw Almonds",
  "description": "Premium quality organic raw almonds, rich in protein and healthy fats. Perfect for snacking.",
  "price": 600.00,
  "originalPrice": 650.00,
  "category": "SNACKS",
  "subcategory": "Nuts",
  "image": "https://example.com/images/almonds.jpg",
  "images": [
    "https://example.com/images/almonds-1.jpg",
    "https://example.com/images/almonds-2.jpg"
  ],
  "inStock": true,
  "quantity": 20,
  "unit": "kg",
  "rating": 4.9,
  "reviewCount": 67,
  "organic": true,
  "fresh": false,
  "discount": 50.00,
  "discountPercentage": 7.69,
  "featured": true,
  "sku": "SNA-ALM-007",
  "barcode": "7890123456789",
  "weightKg": 1.0,
  "shelfLifeDays": 365,
  "storageInstructions": "Store in airtight container in cool, dry place.",
  "originCountry": "USA",
  "supplierName": "Nut Paradise Co",
  "nutritionalInfo": {
    "caloriesPer100g": 579,
    "proteinG": 21.2,
    "carbsG": 21.6,
    "fatG": 49.9,
    "fiberG": 12.5,
    "vitamins": ["Vitamin E", "Magnesium", "Riboflavin", "Niacin"]
  }
}
```

### **8. Natural Shampoo (Personal Care Category)**
```json
{
  "name": "Natural Herbal Shampoo",
  "description": "Gentle natural herbal shampoo with no harmful chemicals. Suitable for all hair types.",
  "price": 180.00,
  "originalPrice": 200.00,
  "category": "PERSONAL_CARE",
  "subcategory": "Hair Care",
  "image": "https://example.com/images/herbal-shampoo.jpg",
  "images": [
    "https://example.com/images/herbal-shampoo-1.jpg"
  ],
  "inStock": true,
  "quantity": 35,
  "unit": "bottle",
  "rating": 4.2,
  "reviewCount": 22,
  "organic": true,
  "fresh": false,
  "discount": 20.00,
  "discountPercentage": 10.00,
  "featured": false,
  "sku": "PER-SHA-008",
  "barcode": "8901234567890",
  "weightKg": 0.3,
  "shelfLifeDays": 1095,
  "storageInstructions": "Store in cool, dry place away from direct sunlight.",
  "originCountry": "India",
  "supplierName": "Natural Care Products",
  "nutritionalInfo": null
}
```

---

## **🚀 How to Use in Postman**

### **Step 1: Create New Request**
1. Open Postman
2. Click "New" → "Request"
3. Name it "Create Product"

### **Step 2: Configure Request**
1. **Method:** Select `POST`
2. **URL:** Enter `http://localhost:8082/api/products`
3. **Headers:** Add `Content-Type: application/json`

### **Step 3: Add Request Body**
1. Go to "Body" tab
2. Select "raw" and "JSON"
3. Copy and paste any of the sample JSON data above

### **Step 4: Send Request**
1. Click "Send"
2. You should get a `201 Created` response with the created product data

---

## **📊 Expected Response Format**

```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": "generated-uuid",
    "name": "Fresh Red Apples",
    "description": "Premium quality fresh red apples...",
    "price": 120.00,
    "originalPrice": 150.00,
    "category": "FRUITS",
    "sku": "FRU-APP-001",
    "createdAt": "2025-09-19T20:02:43.123456789",
    // ... all other product fields
  },
  "statusCode": 201,
  "timestamp": "2025-09-19T20:02:43.123456789"
}
```

---

## **🎯 Available Product Categories**

Use these exact values for the `category` field:
- `FRUITS`
- `VEGETABLES` 
- `DAIRY`
- `GRAINS`
- `SPICES`
- `BEVERAGES`
- `SNACKS`
- `PERSONAL_CARE`
- `HOUSEHOLD`

---

## **✅ Quick Test Commands**

### **Create Multiple Products at Once:**
Use the samples above to create a variety of products, then test:

```bash
# Verify products were created
curl "http://localhost:8082/api/products?page=0&size=10"

# Search for specific products
curl "http://localhost:8082/api/products/search?query=apple"

# Get products by category
curl "http://localhost:8082/api/products/category/FRUITS"
```

---

## **🎉 Ready to Test!**

You now have 8 comprehensive product samples covering all major categories. Use these in Postman to populate your database with realistic test data, then test all the product browsing and search functionality!

**Happy Testing!** 🚀
