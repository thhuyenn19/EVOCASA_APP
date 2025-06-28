import tensorflow as tf
from tensorflow.keras import layers
import numpy as np

# Số lượng người dùng và sản phẩm từ dữ liệu đã chuẩn bị
num_users = len(processed_customer_behavior['customer_id_encoded'].unique())
num_items = len(processed_customer_behavior['product_id_encoded'].unique())

# Xây dựng mô hình Neural Collaborative Filtering (NCF)
user_input = layers.Input(shape=(1,), dtype=tf.int32)
item_input = layers.Input(shape=(1,), dtype=tf.int32)

# Embedding layers cho người dùng và sản phẩm
user_embedding = layers.Embedding(input_dim=num_users, output_dim=50)(user_input)
item_embedding = layers.Embedding(input_dim=num_items, output_dim=50)(item_input)

# Lớp Flatten để chuyển đổi output từ embedding thành dạng 1D
user_flat = layers.Flatten()(user_embedding)
item_flat = layers.Flatten()(item_embedding)

# Kết hợp người dùng và sản phẩm thông qua Concatenate
x = layers.Concatenate()([user_flat, item_flat])

# Các lớp Dense để học mối quan hệ
x = layers.Dense(64, activation="relu")(x)
x = layers.Dense(32, activation="relu")(x)

# Đầu ra là 1 giá trị (điểm dự đoán)
output = layers.Dense(1)(x)

# Tạo mô hình
model = tf.keras.Model([user_input, item_input], output)

# Compile mô hình
model.compile(optimizer='adam', loss='mse')

# Huấn luyện mô hình
model.fit([processed_customer_behavior['customer_id_encoded'], processed_customer_behavior['product_id_encoded']], processed_customer_behavior['action_score'], epochs=10, batch_size=32)

# Dự đoán cho một user-item pair
user_id = 1  # Thay đổi ID người dùng
item_id = 3  # Thay đổi ID sản phẩm
prediction = model.predict([tf.convert_to_tensor([user_id]), tf.convert_to_tensor([item_id])])
print(f"Prediction: {prediction}")
