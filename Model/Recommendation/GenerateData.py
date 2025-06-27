import json
import random
from datetime import datetime, timedelta

# Load customer, product, order, wishlist, review data
with open("data/firebase-upload/EvoCasa.Customers.json", "r", encoding="utf-8") as f:
    customers = json.load(f)

with open("data/firebase-upload/Downloaded_Products.json", "r", encoding="utf-8") as f:
    products = json.load(f)

with open("data/firebase-upload/EvoCasa.Order.json", "r", encoding="utf-8") as f:
    orders = json.load(f)

with open("data/firebase-upload/downloaded_wishlist.json", "r", encoding="utf-8") as f:
    wishlists = json.load(f)

with open("data/firebase-upload/downloaded_review.json", "r", encoding="utf-8") as f:
    reviews = json.load(f)

# Initialize lists
product_ids = [p["_id"]["$oid"] for p in products]
customer_ids = [c["_id"]["$oid"] for c in customers]

# Define behaviors
behavior_types = ["click", "wishlist", "order", "review"]
customer_behavior = []

# Simulate customer behavior for each customer
for customer in customers:
    customer_id = customer["_id"]["$oid"]

    # Simulate "click" behavior
    for _ in range(random.randint(2, 5)):
        product_id = random.choice(product_ids)
        behavior = {
            "customer_id": {"$oid": customer_id},
            "product_id": {"$oid": product_id},
            "action_type": "click",
            "source": "category_page",
            "timestamp": {"$date": (datetime.now() - timedelta(days=random.randint(1, 10))).isoformat() + "Z"}
        }
        customer_behavior.append(behavior)

    # Simulate "wishlist" behavior
    wishlist = next((w for w in wishlists if w["Customer_id"] == customer_id), None)
    if wishlist:
        for product_id in wishlist["Productid"]:
            behavior = {
                "customer_id": {"$oid": customer_id},
                "product_id": {"$oid": product_id},
                "action_type": "wishlist",
                "source": "wishlist_page",
                "timestamp": {"$date": (datetime.now() - timedelta(days=random.randint(1, 10))).isoformat() + "Z"}
            }
            customer_behavior.append(behavior)

    # Simulate "order" behavior
    customer_orders = [order for order in orders if order["Customer_id"]["$oid"] == customer_id]
    for order in customer_orders:
        for product in order["OrderProduct"]:
            product_id = product["id"]["$oid"]
            behavior = {
                "customer_id": {"$oid": customer_id},
                "product_id": {"$oid": product_id},
                "action_type": "order",
                "source": "order_page",
                "timestamp": {"$date": order["OrderDate"]["$date"]}
            }
            customer_behavior.append(behavior)

    # Simulate "review" behavior
    customer_reviews = [r for r in reviews if r["Customer_id"]["$oid"] == customer_id]
    for review in customer_reviews:
        behavior = {
            "customer_id": {"$oid": customer_id},
            "product_id": {"$oid": review["Product_id"]["$oid"]},
            "action_type": "review",
            "rating": review["Rating"]["$numberLong"],
            "source": "product_detail",
            "timestamp": {"$date": review["CreatedAt"]["$date"]}
        }
        customer_behavior.append(behavior)

# Save to file
output_path = "Generated_CustomerBehavior.json"
with open(output_path, "w", encoding="utf-8") as f:
    json.dump(customer_behavior, f, indent=2)

output_path
