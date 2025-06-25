import json
import random
import pandas as pd
from datetime import datetime, timedelta
import os
base_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "firebase-upload"))
customer_file = os.path.join(base_path, "EvoCasa.Customers.json")
product_file = os.path.join(base_path, "EvoCasa.Product.json")

with open(customer_file, "r", encoding="utf-8") as f:
    customers = json.load(f)

with open(product_file, "r", encoding="utf-8") as f:
    products = json.load(f)


# Lấy danh sách user_id và product_id
customer_ids = [c["_id"]["$oid"] for c in customers if "_id" in c and "$oid" in c["_id"]]
product_ids = [p["_id"]["$oid"] for p in products if "_id" in p and "$oid" in p["_id"]]

# Tạo dữ liệu tương tác
interactions = []
for _ in range(1000):
    user_id = random.choice(customer_ids)
    product_id = random.choice(product_ids)
    rating = random.randint(1, 5)
    has_ordered = random.choice([0, 1])
    is_in_wishlist = random.choice([0, 1])
    timestamp = datetime.now() - timedelta(days=random.randint(0, 365))

    interactions.append({
        "user_id": user_id,
        "product_id": product_id,
        "rating": rating,
        "has_ordered": has_ordered,
        "is_in_wishlist": is_in_wishlist,
        "timestamp": timestamp.strftime("%Y-%m-%d %H:%M:%S")
    })

# Chuyển sang DataFrame và lưu file
df = pd.DataFrame(interactions)
df.to_csv("synthetic_interactions.csv", index=False)

print("✅ Đã tạo xong 1000 dòng dữ liệu giả và lưu vào file synthetic_interactions.csv")
