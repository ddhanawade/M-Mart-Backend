#!/bin/bash

# Script to insert sample products into M-Mart Backend
# Make sure the API Gateway and Product Service are running

API_BASE_URL="http://localhost:8082/api/products"

echo "🚀 Starting to insert sample products into M-Mart Backend..."

# Function to create a product
create_product() {
    local product_data="$1"
    echo "Creating product..."
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "$product_data" \
        "$API_BASE_URL")
    
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 201 ]; then
        echo "✅ Product created successfully"
    else
        echo "❌ Failed to create product. HTTP Code: $http_code"
        echo "Response: $response_body"
    fi
    echo "---"
}

# 1. Fresh Organic Apples
create_product '{
    "name": "Fresh Organic Red Apples",
    "description": "Premium quality organic red apples, crisp and sweet. Rich in vitamins and antioxidants. Perfect for snacking or baking. Sourced from local organic farms in Mahabaleshwar hills.",
    "price": 180.00,
    "originalPrice": 220.00,
    "category": "FRUITS",
    "subcategory": "Fresh Fruits",
    "image": "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 150,
    "unit": "kg",
    "rating": 4.5,
    "reviewCount": 89,
    "organic": true,
    "fresh": true,
    "discount": 18.18,
    "active": true,
    "featured": true,
    "sku": "FRUIT-APPLE-ORG-001",
    "barcode": "1234567890123",
    "weightKg": 1.0,
    "shelfLifeDays": 7,
    "storageInstructions": "Store in cool, dry place. Refrigerate for longer freshness.",
    "originCountry": "India",
    "supplierName": "Mahabaleshwar Organic Farms",
    "nutritionalInfo": {
        "caloriesPer100g": 52,
        "proteinG": 0.3,
        "carbsG": 14.0,
        "fatG": 0.2,
        "fiberG": 2.4,
        "vitamins": ["Vitamin C", "Vitamin K", "Vitamin A"]
    }
}'

# 2. Fresh Bananas
create_product '{
    "name": "Fresh Ripe Bananas",
    "description": "Sweet and nutritious bananas, perfect for breakfast, smoothies, or snacking. Rich in potassium and natural sugars for instant energy boost.",
    "price": 60.00,
    "originalPrice": 70.00,
    "category": "FRUITS",
    "subcategory": "Fresh Fruits",
    "image": "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 200,
    "unit": "dozen",
    "rating": 4.3,
    "reviewCount": 156,
    "organic": false,
    "fresh": true,
    "discount": 14.29,
    "active": true,
    "featured": true,
    "sku": "FRUIT-BANANA-001",
    "barcode": "1234567890124",
    "weightKg": 1.2,
    "shelfLifeDays": 5,
    "storageInstructions": "Store at room temperature. Avoid direct sunlight.",
    "originCountry": "India",
    "supplierName": "South Indian Banana Co.",
    "nutritionalInfo": {
        "caloriesPer100g": 89,
        "proteinG": 1.1,
        "carbsG": 23.0,
        "fatG": 0.3,
        "fiberG": 2.6,
        "vitamins": ["Vitamin B6", "Vitamin C", "Potassium"]
    }
}'

# 3. Fresh Tomatoes
create_product '{
    "name": "Fresh Red Tomatoes",
    "description": "Juicy and flavorful red tomatoes, perfect for cooking, salads, and sauces. Rich in lycopene and vitamin C. Locally grown for maximum freshness.",
    "price": 40.00,
    "originalPrice": 50.00,
    "category": "VEGETABLES",
    "subcategory": "Fresh Vegetables",
    "image": "https://images.unsplash.com/photo-1546470427-e26264be0b0d?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1546470427-e26264be0b0d?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 180,
    "unit": "kg",
    "rating": 4.2,
    "reviewCount": 203,
    "organic": false,
    "fresh": true,
    "discount": 20.00,
    "active": true,
    "featured": false,
    "sku": "VEG-TOMATO-001",
    "barcode": "1234567890125",
    "weightKg": 1.0,
    "shelfLifeDays": 4,
    "storageInstructions": "Store in cool, dry place. Refrigerate for longer freshness.",
    "originCountry": "India",
    "supplierName": "Maharashtra Vegetable Growers",
    "nutritionalInfo": {
        "caloriesPer100g": 18,
        "proteinG": 0.9,
        "carbsG": 3.9,
        "fatG": 0.2,
        "fiberG": 1.2,
        "vitamins": ["Vitamin C", "Vitamin K", "Lycopene"]
    }
}'

# 4. Fresh Milk
create_product '{
    "name": "Fresh Whole Milk",
    "description": "Pure and creamy whole milk from grass-fed cows. Rich in calcium, protein, and essential nutrients. Perfect for drinking, cooking, and baking.",
    "price": 65.00,
    "originalPrice": 65.00,
    "category": "DAIRY",
    "subcategory": "Milk & Cream",
    "image": "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 100,
    "unit": "liter",
    "rating": 4.6,
    "reviewCount": 78,
    "organic": false,
    "fresh": true,
    "discount": 0.00,
    "active": true,
    "featured": true,
    "sku": "DAIRY-MILK-WHOLE-001",
    "barcode": "1234567890126",
    "weightKg": 1.03,
    "shelfLifeDays": 3,
    "storageInstructions": "Keep refrigerated at 4°C or below. Consume within 3 days of opening.",
    "originCountry": "India",
    "supplierName": "Mahabaleshwar Dairy Co-operative",
    "nutritionalInfo": {
        "caloriesPer100g": 61,
        "proteinG": 3.2,
        "carbsG": 4.8,
        "fatG": 3.3,
        "fiberG": 0.0,
        "vitamins": ["Vitamin D", "Vitamin B12", "Calcium"]
    }
}'

# 5. Basmati Rice
create_product '{
    "name": "Premium Basmati Rice",
    "description": "Aromatic long-grain basmati rice, aged for perfect texture and flavor. Ideal for biryanis, pulavs, and everyday meals. Premium quality grains.",
    "price": 120.00,
    "originalPrice": 140.00,
    "category": "GROCERIES",
    "subcategory": "Rice & Grains",
    "image": "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 80,
    "unit": "kg",
    "rating": 4.4,
    "reviewCount": 134,
    "organic": false,
    "fresh": false,
    "discount": 14.29,
    "active": true,
    "featured": false,
    "sku": "GROC-RICE-BASMATI-001",
    "barcode": "1234567890127",
    "weightKg": 1.0,
    "shelfLifeDays": 365,
    "storageInstructions": "Store in cool, dry place in airtight container. Keep away from moisture.",
    "originCountry": "India",
    "supplierName": "Punjab Rice Mills",
    "nutritionalInfo": {
        "caloriesPer100g": 345,
        "proteinG": 7.1,
        "carbsG": 78.0,
        "fatG": 0.9,
        "fiberG": 1.3,
        "vitamins": ["Vitamin B1", "Vitamin B3", "Iron"]
    }
}'

# 6. Fresh Bread
create_product '{
    "name": "Whole Wheat Bread",
    "description": "Freshly baked whole wheat bread, soft and nutritious. Made with 100% whole wheat flour, perfect for sandwiches and toast. No artificial preservatives.",
    "price": 45.00,
    "originalPrice": 50.00,
    "category": "BAKERY",
    "subcategory": "Bread & Buns",
    "image": "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1509440159596-0249440159596?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 50,
    "unit": "pieces",
    "rating": 4.1,
    "reviewCount": 67,
    "organic": false,
    "fresh": true,
    "discount": 10.00,
    "active": true,
    "featured": false,
    "sku": "BAKERY-BREAD-WW-001",
    "barcode": "1234567890128",
    "weightKg": 0.4,
    "shelfLifeDays": 3,
    "storageInstructions": "Store in cool, dry place. Best consumed fresh within 3 days.",
    "originCountry": "India",
    "supplierName": "Mahabaleshwar Bakery",
    "nutritionalInfo": {
        "caloriesPer100g": 247,
        "proteinG": 13.0,
        "carbsG": 41.0,
        "fatG": 4.2,
        "fiberG": 7.0,
        "vitamins": ["Vitamin B1", "Vitamin B3", "Iron", "Fiber"]
    }
}'

# 7. Green Tea
create_product '{
    "name": "Premium Green Tea",
    "description": "High-quality green tea leaves with natural antioxidants. Refreshing and healthy beverage choice. Sourced from the finest tea gardens of Darjeeling.",
    "price": 250.00,
    "originalPrice": 300.00,
    "category": "BEVERAGES",
    "subcategory": "Tea & Coffee",
    "image": "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1597318181409-cf64d0b3d8bd?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 60,
    "unit": "grams",
    "rating": 4.7,
    "reviewCount": 92,
    "organic": true,
    "fresh": false,
    "discount": 16.67,
    "active": true,
    "featured": true,
    "sku": "BEV-TEA-GREEN-001",
    "barcode": "1234567890129",
    "weightKg": 0.1,
    "shelfLifeDays": 720,
    "storageInstructions": "Store in cool, dry place in airtight container. Keep away from moisture and strong odors.",
    "originCountry": "India",
    "supplierName": "Darjeeling Tea Estate",
    "nutritionalInfo": {
        "caloriesPer100g": 1,
        "proteinG": 0.2,
        "carbsG": 0.0,
        "fatG": 0.0,
        "fiberG": 0.0,
        "vitamins": ["Antioxidants", "Catechins", "Vitamin C"]
    }
}'

# 8. Turmeric Powder
create_product '{
    "name": "Pure Turmeric Powder",
    "description": "Premium quality turmeric powder with high curcumin content. Essential spice for Indian cooking with anti-inflammatory properties. 100% pure and natural.",
    "price": 80.00,
    "originalPrice": 90.00,
    "category": "SPICES",
    "subcategory": "Ground Spices",
    "image": "https://images.unsplash.com/photo-1615485500704-8e990f9900f7?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1615485500704-8e990f9900f7?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1609501676725-7186f0b6c6e8?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1599909533730-8c1b8b8b7c8b?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 120,
    "unit": "grams",
    "rating": 4.5,
    "reviewCount": 178,
    "organic": true,
    "fresh": false,
    "discount": 11.11,
    "active": true,
    "featured": false,
    "sku": "SPICE-TURMERIC-001",
    "barcode": "1234567890130",
    "weightKg": 0.1,
    "shelfLifeDays": 730,
    "storageInstructions": "Store in cool, dry place in airtight container. Keep away from direct sunlight.",
    "originCountry": "India",
    "supplierName": "Kerala Spice Co.",
    "nutritionalInfo": {
        "caloriesPer100g": 312,
        "proteinG": 9.7,
        "carbsG": 67.1,
        "fatG": 3.2,
        "fiberG": 22.7,
        "vitamins": ["Curcumin", "Iron", "Manganese"]
    }
}'

# 9. Mixed Nuts
create_product '{
    "name": "Premium Mixed Nuts",
    "description": "Delicious mix of cashews, almonds, walnuts, and pistachios. Perfect healthy snack packed with proteins, healthy fats, and essential nutrients.",
    "price": 450.00,
    "originalPrice": 500.00,
    "category": "SNACKS",
    "subcategory": "Nuts & Seeds",
    "image": "https://images.unsplash.com/photo-1508747703725-719777637510?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1508747703725-719777637510?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1559656914-a30970c1affd?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1605522883193-8b8c40d7b0b5?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 40,
    "unit": "grams",
    "rating": 4.8,
    "reviewCount": 156,
    "organic": false,
    "fresh": false,
    "discount": 10.00,
    "active": true,
    "featured": true,
    "sku": "SNACK-NUTS-MIX-001",
    "barcode": "1234567890131",
    "weightKg": 0.25,
    "shelfLifeDays": 180,
    "storageInstructions": "Store in cool, dry place in airtight container. Keep away from moisture.",
    "originCountry": "India",
    "supplierName": "Kashmir Dry Fruits",
    "nutritionalInfo": {
        "caloriesPer100g": 607,
        "proteinG": 20.0,
        "carbsG": 20.0,
        "fatG": 50.0,
        "fiberG": 7.0,
        "vitamins": ["Vitamin E", "Magnesium", "Healthy Fats"]
    }
}'

# 10. Hand Sanitizer
create_product '{
    "name": "Antibacterial Hand Sanitizer",
    "description": "70% alcohol-based hand sanitizer for effective germ protection. Quick-drying formula with moisturizing agents. Essential for daily hygiene and health protection.",
    "price": 120.00,
    "originalPrice": 150.00,
    "category": "PERSONAL_CARE",
    "subcategory": "Health & Hygiene",
    "image": "https://images.unsplash.com/photo-1584744982491-665216d95f8b?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1584744982491-665216d95f8b?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1586015555751-63bb77f4b3d4?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1605289982774-9a6fef564df8?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 200,
    "unit": "ml",
    "rating": 4.3,
    "reviewCount": 89,
    "organic": false,
    "fresh": false,
    "discount": 20.00,
    "active": true,
    "featured": false,
    "sku": "CARE-SANITIZER-001",
    "barcode": "1234567890132",
    "weightKg": 0.5,
    "shelfLifeDays": 1095,
    "storageInstructions": "Store in cool, dry place. Keep away from heat and direct sunlight.",
    "originCountry": "India",
    "supplierName": "HealthCare Products Ltd.",
    "nutritionalInfo": {
        "caloriesPer100g": 0,
        "proteinG": 0.0,
        "carbsG": 0.0,
        "fatG": 0.0,
        "fiberG": 0.0,
        "vitamins": ["Alcohol 70%", "Glycerin", "Aloe Vera"]
    }
}'

# 11. Alphonso Mango (Seasonal Featured)
create_product '{
    "name": "Alphonso Mango (Seasonal)",
    "description": "Premium Alphonso mangoes from Ratnagiri, rich aroma and sweetness. Limited seasonal stock.",
    "price": 750.00,
    "originalPrice": 899.00,
    "category": "FRUITS",
    "subcategory": "Seasonal",
    "image": "https://images.unsplash.com/photo-1623428454178-42ebc1dd1bac?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1623428454178-42ebc1dd1bac?w=500&h=500&fit=crop",
        "https://images.unsplash.com/photo-1601042879364-f3947d3b4019?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 35,
    "unit": "dozen",
    "rating": 4.9,
    "reviewCount": 42,
    "organic": false,
    "fresh": true,
    "discount": 16.59,
    "active": true,
    "featured": true,
    "sku": "FRUIT-MANGO-ALPH-001",
    "barcode": "1234567890133",
    "weightKg": 2.0,
    "shelfLifeDays": 6,
    "storageInstructions": "Keep in a cool, ventilated place. Do not refrigerate until ripe.",
    "originCountry": "India",
    "supplierName": "Ratnagiri Mango Farmers Co.",
    "nutritionalInfo": {
        "caloriesPer100g": 60,
        "proteinG": 0.8,
        "carbsG": 15.0,
        "fatG": 0.4,
        "fiberG": 1.6,
        "vitamins": ["Vitamin A", "Vitamin C", "Folate"]
    }
}'

# 12. Organic Spinach (Vegetables)
create_product '{
    "name": "Organic Spinach Bunch",
    "description": "Fresh organic spinach leaves, perfect for salads, soups, and curries. Rich in iron and vitamins.",
    "price": 40.00,
    "originalPrice": 50.00,
    "category": "VEGETABLES",
    "subcategory": "Leafy Greens",
    "image": "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 120,
    "unit": "bunch",
    "rating": 4.6,
    "reviewCount": 54,
    "organic": true,
    "fresh": true,
    "discount": 20.00,
    "active": true,
    "featured": false,
    "sku": "VEG-SPINACH-ORG-001",
    "barcode": "1234567890134",
    "weightKg": 0.25,
    "shelfLifeDays": 3,
    "storageInstructions": "Refrigerate in a perforated bag. Wash before use.",
    "originCountry": "India",
    "supplierName": "Mahabaleshwar Organic Farms",
    "nutritionalInfo": {
        "caloriesPer100g": 23,
        "proteinG": 2.9,
        "carbsG": 3.6,
        "fatG": 0.4,
        "fiberG": 2.2,
        "vitamins": ["Vitamin K", "Vitamin A", "Iron"]
    }
}'

# 13. Mapro Strawberry Crush (Brand)
create_product '{
    "name": "Mapro Strawberry Crush",
    "description": "Popular from Panchgani; authentic strawberry crush perfect for desserts and beverages.",
    "price": 180.00,
    "originalPrice": 220.00,
    "category": "BEVERAGES",
    "subcategory": "Syrups & Crushes",
    "image": "https://images.unsplash.com/photo-1604908812385-6e4a6a93d3da?w=500&h=500&fit=crop",
    "images": [
        "https://images.unsplash.com/photo-1604908812385-6e4a6a93d3da?w=500&h=500&fit=crop"
    ],
    "inStock": true,
    "quantity": 90,
    "unit": "ml",
    "rating": 4.5,
    "reviewCount": 120,
    "organic": false,
    "fresh": false,
    "discount": 18.18,
    "active": true,
    "featured": true,
    "sku": "MAPRO-CRUSH-STRAWBERRY-750",
    "barcode": "1234567890135",
    "weightKg": 0.9,
    "shelfLifeDays": 365,
    "storageInstructions": "Store in a cool, dry place. Refrigerate after opening.",
    "originCountry": "India",
    "supplierName": "Mapro Foods",
    "nutritionalInfo": {
        "caloriesPer100g": 120,
        "proteinG": 0.2,
        "carbsG": 30.0,
        "fatG": 0.1,
        "fiberG": 0.2,
        "vitamins": ["Vitamin C"]
    }
}'

echo "🎉 Sample product insertion completed!"
echo "Check your M-Mart application to see the new products with images."
