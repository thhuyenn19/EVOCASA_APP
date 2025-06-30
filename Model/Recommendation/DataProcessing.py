import pandas as pd

# Đọc dữ liệu CustomerBehavior từ file JSON
customer_behavior = pd.read_json('Model/Recommendation/EvoCasa.CustomerBehavior.json')

# Kiểm tra nếu customer_id và product_id là dict (có dạng { "$oid": "..." }), chuyển thành string
customer_behavior['customer_id'] = customer_behavior['customer_id'].apply(lambda x: x.get('$oid') if isinstance(x, dict) else x)
customer_behavior['product_id'] = customer_behavior['product_id'].apply(lambda x: x.get('$oid') if isinstance(x, dict) else x)

# Mã hóa hành vi (action_type) thành điểm số
action_map = {
    "click": 0.5,
    "wishlist": 1,
    "order": 3,
    "review": lambda x: x['rating']  # Mã hóa review thành rating
}

# Hàm mã hóa hành vi
def encode_action(row):
    action_score = action_map.get(row['action_type'])
    if callable(action_score):
        return action_score(row)  # Nếu là review, dùng rating
    return action_score

# Thêm cột action_score vào dataframe
customer_behavior['action_score'] = customer_behavior.apply(encode_action, axis=1)

# Kiểm tra dữ liệu sau khi mã hóa hành vi
print(customer_behavior.head())

# Chuyển customer_id và product_id thành các chỉ số số nguyên (integer encoding)
customer_id_mapping = {id: idx for idx, id in enumerate(customer_behavior['customer_id'].unique())}
product_id_mapping = {id: idx for idx, id in enumerate(customer_behavior['product_id'].unique())}

customer_behavior['customer_id_encoded'] = customer_behavior['customer_id'].map(customer_id_mapping)
customer_behavior['product_id_encoded'] = customer_behavior['product_id'].map(product_id_mapping)

# Kiểm tra sau khi mã hóa ID
print(customer_behavior[['customer_id', 'product_id', 'customer_id_encoded', 'product_id_encoded']].head())

# Lưu dữ liệu đã mã hóa vào file CSV để sử dụng cho mô hình
customer_behavior.to_csv('Model/Recommendation/processed_customer_behavior.csv', index=False)
