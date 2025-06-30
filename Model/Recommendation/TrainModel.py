import pandas as pd
import numpy as np
import tensorflow as tf
from tensorflow.keras import layers
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import StandardScaler, MinMaxScaler, LabelEncoder
import pickle
import json
from tensorflow.keras.layers import Input, Lambda
from tensorflow.keras.models import Model

class DualRecommendationSystem:
    def __init__(self):
        self.collaborative_model = None
        self.content_based_similarity = None
        self.customer_id_mapping = {}
        self.product_id_mapping = {}
        self.reverse_product_mapping = {}
        self.product_features = None
        self.text_scaler = StandardScaler()
        self.price_scaler = MinMaxScaler()
        self.category_encoder = LabelEncoder()

    def load_and_preprocess_data(self):
        """Load và preprocess dữ liệu"""
        print("Loading data...")

        # Load data
        customer_behavior = pd.read_csv('Model/Recommendation/processed_customer_behavior.csv')
        product_data = pd.read_json('Model/Recommendation/EvoCasa.Products.json')

        # Create product mapping
        product_mapping = product_data.set_index('id').to_dict('index')

        # Add product details to customer behavior
        def add_product_details(row):
            product = product_mapping.get(row['product_id'])
            if product:
                row['product_name'] = product.get('Name', '')
                row['product_description'] = product.get('Description', '')
                row['product_price'] = product.get('Price', 0)
                row['product_origin'] = product.get('Origin', '')
                row['product_uses'] = product.get('Uses', '')
                # Handle category_id format
                category = product.get('category_id', '')
                if isinstance(category, dict) and '$oid' in category:
                    row['product_category'] = str(category['$oid'])
                else:
                    row['product_category'] = str(category)
            return row

        customer_behavior = customer_behavior.apply(add_product_details, axis=1)

        # Create action_score if not exists
        if 'action_score' not in customer_behavior.columns:
            print("Creating action_score from interactions...")
            customer_behavior['action_score'] = 1.0  # Implicit feedback

        # Create ID mappings (only for products that exist in both datasets)
        valid_products = set(customer_behavior['product_id'].dropna()) & set(product_data['id'])
        valid_products_sorted = sorted(valid_products)
        filtered_behavior = customer_behavior[customer_behavior['product_id'].isin(valid_products)]

        valid_customers_sorted = sorted(filtered_behavior['customer_id'].unique())
        self.customer_id_mapping = {id_: idx for idx, id_ in enumerate(valid_customers_sorted)}
        self.product_id_mapping = {id: idx for idx, id in enumerate(valid_products_sorted)}
        self.reverse_product_mapping = {idx: id for id, idx in self.product_id_mapping.items()}

        # Encode IDs
        filtered_behavior['customer_id_encoded'] = filtered_behavior['customer_id'].map(self.customer_id_mapping)
        filtered_behavior['product_id_encoded'] = filtered_behavior['product_id'].map(self.product_id_mapping)

        # Filter product_data to only include products in customer_behavior
        product_data_filtered = product_data[product_data['id'].isin(valid_products)]

        print(f"Final data: {len(filtered_behavior)} interactions, {len(self.customer_id_mapping)} users, {len(self.product_id_mapping)} products")

        return filtered_behavior, product_data_filtered

    def build_collaborative_filtering_model(self, customer_behavior):
        """Xây dựng Collaborative Filtering model cho 'might you like'"""
        print("Building Collaborative Filtering model...")

        num_users = max(self.customer_id_mapping.values()) + 1
        num_items = max(self.product_id_mapping.values()) + 1

        print(f"Building model with num_users={num_users}, num_items={num_items}")


        # Input layers
        user_input = layers.Input(shape=(1,), dtype=tf.int32, name='user_input')
        item_input = layers.Input(shape=(1,), dtype=tf.int32, name='item_input')

        # Embedding layers với regularization
        user_embedding = layers.Embedding(
            input_dim=num_users,
            output_dim=64,
            embeddings_regularizer=tf.keras.regularizers.l2(0.001)
        )(user_input)

        item_embedding = layers.Embedding(
            input_dim=num_items,
            output_dim=64,
            embeddings_regularizer=tf.keras.regularizers.l2(0.001)
        )(item_input)

        # Flatten
        user_flat = layers.Flatten()(user_embedding)
        item_flat = layers.Flatten()(item_embedding)

        # Neural MF approach
        # 1. Element-wise product
        mf_vector = layers.Multiply()([user_flat, item_flat])

        # 2. Concatenation for MLP
        mlp_vector = layers.Concatenate()([user_flat, item_flat])

        # MLP layers
        mlp = layers.Dense(128, activation="relu")(mlp_vector)
        mlp = layers.Dropout(0.2)(mlp)
        mlp = layers.Dense(64, activation="relu")(mlp)
        mlp = layers.Dropout(0.2)(mlp)
        mlp = layers.Dense(32, activation="relu")(mlp)

        # Combine MF and MLP
        combined = layers.Concatenate()([mf_vector, mlp])

        # Final prediction layer
        output = layers.Dense(1, activation="sigmoid")(combined)

        # Create model
        model = tf.keras.Model([user_input, item_input], output)
        model.compile(
            optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
            loss='binary_crossentropy',
            metrics=['mae']
        )

        return model

    def create_price_ranges(self, prices):
        """Tạo price ranges để group sản phẩm có giá tương tự"""
        # Sử dụng quantiles để tạo price ranges
        price_ranges = pd.qcut(prices, q=10, labels=False, duplicates='drop')
        return price_ranges

    def build_content_based_features(self, product_data):
        """Xây dựng Content-Based features với trọng số cải thiện"""
        print("Building improved Content-Based features...")

        # Reset index để đảm bảo consistency
        product_data = product_data.reset_index(drop=True)

        # Debug category distribution
        categories = product_data['category_id'].apply(
            lambda x: str(x['$oid']) if isinstance(x, dict) and '$oid' in x else str(x)
        )
        print(f"Debug: Category distribution:")
        print(categories.value_counts().head(10))

        # Chuẩn bị text features
        product_data['combined_text'] = (
            product_data['Name'].fillna('') + ' ' +
            product_data['Description'].fillna('') + ' ' +
            product_data['Origin'].fillna('') + ' ' +
            product_data['Uses'].fillna('')
        )

        # TF-IDF cho text features (giảm trọng số)
        tfidf = TfidfVectorizer(
            max_features=150,
            stop_words=None,
            ngram_range=(1, 1)
        )

        text_features = tfidf.fit_transform(product_data['combined_text']).toarray()

        # Chuẩn bị features với trọng số
        feature_components = []
        feature_weights = []

        # 1. Text features (weight: 0.2 - giảm xuống)
        feature_components.append(text_features)
        feature_weights.append(0.2)

        # 2. Category features (weight: 0.5 - tăng lên cao nhất)
        categories_clean = product_data['category_id'].apply(
            lambda x: str(x['$oid']) if isinstance(x, dict) and '$oid' in x else str(x)
        )

        # One-hot encode categories
        category_dummies = pd.get_dummies(categories_clean, prefix='category').values
        print(f"Debug: Category features shape: {category_dummies.shape}")

        feature_components.append(category_dummies)
        feature_weights.append(0.5)

        # 3. Price features (weight: 0.3)
        if 'Price' in product_data.columns:
            prices = pd.to_numeric(product_data['Price'], errors='coerce').fillna(0)

            # Tạo price ranges
            price_ranges = self.create_price_ranges(prices)
            price_range_dummies = pd.get_dummies(price_ranges, prefix='price_range').values

            # Normalize raw prices
            price_normalized = self.price_scaler.fit_transform(prices.values.reshape(-1, 1))

            # Combine price features
            price_features = np.hstack([price_range_dummies, price_normalized])
            feature_components.append(price_features)
            feature_weights.append(0.3)

        # Combine features với trọng số
        print("Combining weighted features...")
        normalized_components = []
        for i, component in enumerate(feature_components):
            print(f"Component {i} shape: {component.shape}, weight: {feature_weights[i]}")
            # Normalize component
            component_normalized = StandardScaler().fit_transform(component)
            # Apply weight
            component_weighted = component_normalized * feature_weights[i]
            normalized_components.append(component_weighted)

        # Combine all weighted features
        all_features = np.hstack(normalized_components)
        print(f"Final features shape: {all_features.shape}")

        # Calculate similarity matrix
        print("Computing cosine similarity...")
        similarity_matrix = cosine_similarity(all_features)

        # Apply additional filters cho similarity
        similarity_matrix = self.apply_similarity_filters(similarity_matrix, product_data)

        # Save features
        self.product_features = all_features
        self.content_based_similarity = similarity_matrix

        # Save components
        with open('tfidf_vectorizer.pkl', 'wb') as f:
            pickle.dump(tfidf, f)

        return similarity_matrix

    def apply_similarity_filters(self, similarity_matrix, product_data):
        """Apply additional filters để cải thiện similarity"""
        print("Applying similarity filters...")

        # Reset index để đảm bảo index mapping đúng
        product_data_reset = product_data.reset_index(drop=True)

        # Lấy thông tin sản phẩm
        prices = pd.to_numeric(product_data_reset['Price'], errors='coerce').fillna(0).values
        categories = product_data_reset['category_id'].apply(
            lambda x: str(x['$oid']) if isinstance(x, dict) and '$oid' in x else str(x)
        ).values

        print(f"Debug: Found {len(set(categories))} unique categories")
        print(f"Debug: Categories sample: {list(set(categories))[:5]}")

        # Tạo penalty matrix
        penalty_matrix = np.ones_like(similarity_matrix)
        category_matches = 0
        category_mismatches = 0

        for i in range(len(product_data_reset)):
            for j in range(len(product_data_reset)):
                if i != j:
                    # Category penalty - MẠNH HƠN để enforce same category
                    if categories[i] != categories[j]:
                        penalty_matrix[i, j] *= 0.2
                        category_mismatches += 1
                    else:
                        category_matches += 1
                        # Bonus cho cùng category
                        penalty_matrix[i, j] *= 1.1

                    # Price penalty - chỉ áp dụng trong cùng category
                    if categories[i] == categories[j]:
                        price_diff = abs(prices[i] - prices[j])
                        max_price = max(prices[i], prices[j])

                        if max_price > 0:
                            price_ratio = price_diff / max_price
                            # Nếu chênh lệch > 70% thì penalty mạnh
                            if price_ratio > 0.7:
                                penalty_matrix[i, j] *= 0.3  # Giảm 70% similarity
                            elif price_ratio > 0.5:
                                penalty_matrix[i, j] *= 0.5  # Giảm 60% similarity
                            elif price_ratio > 0.3:
                                penalty_matrix[i, j] *= 0.8  # Giảm 30% similarity

        print(f"Debug: Category matches: {category_matches}, mismatches: {category_mismatches}")

        # Apply penalty
        filtered_similarity = similarity_matrix * penalty_matrix

        # Ensure no negative values
        filtered_similarity = np.maximum(filtered_similarity, 0)

        return filtered_similarity

    def prepare_collaborative_training_data(self, customer_behavior):
        """Chuẩn bị data cho collaborative filtering"""
        print("Preparing collaborative filtering training data...")

        # Tạo positive samples từ interactions
        positive_samples = customer_behavior[['customer_id_encoded', 'product_id_encoded']].copy()
        positive_samples['rating'] = 1.0

        # Tạo negative samples
        num_users = len(self.customer_id_mapping)
        num_items = len(self.product_id_mapping)

        # Sample negative interactions
        num_negatives = len(positive_samples) * 2  # 2 negative per positive

        negative_users = np.random.choice(num_users, num_negatives)
        negative_items = np.random.choice(num_items, num_negatives)

        negative_samples = pd.DataFrame({
            'customer_id_encoded': negative_users,
            'product_id_encoded': negative_items,
            'rating': 0.0
        })

        # Remove false negatives
        positive_pairs = set(zip(
            positive_samples['customer_id_encoded'],
            positive_samples['product_id_encoded']
        ))

        negative_samples = negative_samples[
            ~negative_samples.apply(
                lambda x: (x['customer_id_encoded'], x['product_id_encoded']) in positive_pairs,
                axis=1
            )
        ]

        # Combine và shuffle
        all_samples = pd.concat([positive_samples, negative_samples], ignore_index=True)
        all_samples = all_samples.sample(frac=1).reset_index(drop=True)

        return all_samples

    def train_models(self, epochs=50, batch_size=512):
        """Train cả 2 models"""
        print("Starting training process...")

        # Load data
        customer_behavior, product_data = self.load_and_preprocess_data()

        # 1. Train Collaborative Filtering model
        print("\n=== Training Collaborative Filtering Model ===")
        self.collaborative_model = self.build_collaborative_filtering_model(customer_behavior)

        # Prepare training data
        training_data = self.prepare_collaborative_training_data(customer_behavior)

        # Train
        history = self.collaborative_model.fit(
            [training_data['customer_id_encoded'], training_data['product_id_encoded']],
            training_data['rating'],
            epochs=epochs,
            batch_size=batch_size,
            validation_split=0.2,
            callbacks=[
                tf.keras.callbacks.EarlyStopping(patience=10, restore_best_weights=True),
                tf.keras.callbacks.ReduceLROnPlateau(factor=0.5, patience=5)
            ],
            verbose=1
        )

        # 2. Build Content-Based similarity matrix
        print("\n=== Building Content-Based Model ===")
        self.build_content_based_features(product_data)

        print("Training completed!")
        return history

    def get_similar_products(self, product_id, top_k=5):
        """Lấy sản phẩm tương tự với category filtering mạnh"""
        if self.content_based_similarity is None:
            return []

        try:
            # Get product index
            product_idx = self.product_id_mapping[product_id]

            # Get similarity scores
            similarity_scores = self.content_based_similarity[product_idx]

            # Load product data để lấy category của target product
            product_data = pd.read_json('Downloaded_Products.json')
            target_product = product_data[product_data['id'] == product_id].iloc[0]

            # Extract target category
            target_category = target_product['category_id']
            if isinstance(target_category, dict) and '$oid' in target_category:
                target_category = str(target_category['$oid'])
            else:
                target_category = str(target_category)

            print(f"Debug: Target product category: {target_category}")

            # Filter chỉ lấy sản phẩm cùng category
            same_category_indices = []
            same_category_scores = []

            for idx, score in enumerate(similarity_scores):
                if idx != product_idx:  # Exclude self
                    candidate_product_id = self.reverse_product_mapping[idx]
                    candidate_product = product_data[product_data['id'] == candidate_product_id].iloc[0]

                    # Extract candidate category
                    candidate_category = candidate_product['category_id']
                    if isinstance(candidate_category, dict) and '$oid' in candidate_category:
                        candidate_category = str(candidate_category['$oid'])
                    else:
                        candidate_category = str(candidate_category)

                    # Chỉ lấy sản phẩm cùng category
                    if candidate_category == target_category:
                        same_category_indices.append(idx)
                        same_category_scores.append(score)

            print(f"Debug: Found {len(same_category_indices)} products in same category")

            if len(same_category_indices) == 0:
                print("No products found in same category, falling back to top similarity")
                # Fallback: lấy top similarity nhưng vẫn prefer same category
                return self.get_similar_products_fallback(product_id, top_k)

            # Sort by similarity score
            sorted_pairs = sorted(zip(same_category_indices, same_category_scores),
                                key=lambda x: x[1], reverse=True)

            similar_products = []
            for idx, score in sorted_pairs[:top_k]:
                similar_product_id = self.reverse_product_mapping[idx]
                similar_products.append((similar_product_id, score))

            return similar_products

        except Exception as e:
            print(f"Error getting similar products: {e}")
            return []

    def get_similar_products_fallback(self, product_id, top_k=5):
        """Fallback method nếu không tìm được sản phẩm cùng category"""
        try:
            product_idx = self.product_id_mapping[product_id]
            similarity_scores = self.content_based_similarity[product_idx]

            # Filter out very low similarity scores
            min_similarity_threshold = 0.05
            valid_indices = np.where(similarity_scores >= min_similarity_threshold)[0]

            if len(valid_indices) == 0:
                return []

            # Get top similar products (exclude self)
            valid_scores = similarity_scores[valid_indices]
            sorted_indices = np.argsort(valid_scores)[::-1]

            similar_products = []
            for idx in sorted_indices:
                if len(similar_products) >= top_k:
                    break

                actual_idx = valid_indices[idx]
                if actual_idx != product_idx:  # Exclude self
                    similar_product_id = self.reverse_product_mapping[actual_idx]
                    similarity_score = similarity_scores[actual_idx]
                    similar_products.append((similar_product_id, similarity_score))

            return similar_products

        except Exception as e:
            print(f"Error in fallback method: {e}")
            return []

    def get_user_recommendations(self, customer_id, top_k=10, exclude_seen=True):
        """Lấy gợi ý cho user (Collaborative Filtering)"""
        if self.collaborative_model is None:
            return []

        try:
            # Get customer index
            customer_idx = self.customer_id_mapping[customer_id]

            # Get all products
            all_products = list(range(len(self.product_id_mapping)))
            customer_array = np.full(len(all_products), customer_idx)

            # Predict scores
            scores = self.collaborative_model.predict(
                [customer_array, all_products],
                verbose=0
            ).flatten()

            # Get top recommendations
            top_indices = np.argsort(scores)[-top_k:][::-1]

            recommendations = []
            for idx in top_indices:
                product_id = self.reverse_product_mapping[idx]
                score = scores[idx]
                recommendations.append((product_id, score))

            return recommendations

        except KeyError:
            print(f"Customer {customer_id} not found")
            return []

    def analyze_similarity_reasons(self, product_id1, product_id2, product_data=None):
        """Phân tích lý do tại sao 2 sản phẩm được coi là similar"""
        try:
            if product_data is None:
                product_data = pd.read_json('Downloaded_Products.json')

            # Get product details
            product1 = product_data[product_data['id'] == product_id1].iloc[0]
            product2 = product_data[product_data['id'] == product_id2].iloc[0]

            print(f"\n=== Similarity Analysis ===")
            print(f"Product 1: {product1['Name']} (${product1['Price']})")
            print(f"Product 2: {product2['Name']} (${product2['Price']})")

            # Category comparison
            cat1 = product1['category_id']
            cat2 = product2['category_id']

            if isinstance(cat1, dict) and '$oid' in cat1:
                cat1_str = str(cat1['$oid'])
            else:
                cat1_str = str(cat1)

            if isinstance(cat2, dict) and '$oid' in cat2:
                cat2_str = str(cat2['$oid'])
            else:
                cat2_str = str(cat2)

            category_match = cat1_str == cat2_str
            print(f"Category 1: {cat1_str}")
            print(f"Category 2: {cat2_str}")
            print(f"Same category: {category_match}")

            # Price similarity
            price1 = float(product1['Price'])
            price2 = float(product2['Price'])
            price_diff = abs(price1 - price2)
            max_price = max(price1, price2)
            price_similarity = 1 - (price_diff / max_price) if max_price > 0 else 1
            print(f"Price similarity: {price_similarity:.3f}")

            # Text similarity
            text1 = f"{product1['Name']} {product1['Description']} {product1['Origin']} {product1['Uses']}"
            text2 = f"{product2['Name']} {product2['Description']} {product2['Origin']} {product2['Uses']}"

            tfidf = TfidfVectorizer()
            tfidf_matrix = tfidf.fit_transform([text1, text2])
            text_similarity = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])[0][0]
            print(f"Text similarity: {text_similarity:.3f}")

            # Overall similarity from matrix
            if self.content_based_similarity is not None:
                idx1 = self.product_id_mapping.get(product_id1)
                idx2 = self.product_id_mapping.get(product_id2)
                if idx1 is not None and idx2 is not None:
                    overall_similarity = self.content_based_similarity[idx1][idx2]
                    print(f"Overall similarity score: {overall_similarity:.3f}")

        except Exception as e:
            print(f"Error analyzing similarity: {e}")
    def build_content_based_tflite_model(self, top_k=5):
        """
        Xây dựng một Keras model nhận vào product index và xuất ra top_k indices
        theo content-based similarity đã tính sẵn.
        """
        features = self.product_features.astype(np.float32)
        features_const = tf.constant(features)
        num_items, feat_dim = features.shape

        inp = Input(shape=(), dtype=tf.int32, name="product_idx")

        # Lấy vector feature tương ứng
        selected = Lambda(
            lambda idx: tf.gather(features_const, idx),
            output_shape=(feat_dim,),
            name="gather_features"
        )(inp)

        # Tính similarity: dot product
        sims = Lambda(
            lambda vec: tf.matmul(tf.expand_dims(vec, 0), features_const, transpose_b=True),
            output_shape=(num_items,),
            name="compute_sims"
        )(selected)

        # Lấy top_k indices
        topk_indices = Lambda(
            lambda x: tf.math.top_k(x, k=top_k)[1],
            output_shape=(top_k,),
            name="topk_indices"
        )(sims)

        model = Model(inputs=inp, outputs=topk_indices, name="content_based_topk")
        return model


    def debug_category_distribution(self):
        """Debug function để kiểm tra category distribution"""
        try:
            product_data = pd.read_json('Downloaded_Products.json')

            # Extract categories
            categories = product_data['category_id'].apply(
                lambda x: str(x['$oid']) if isinstance(x, dict) and '$oid' in x else str(x)
            )

            print("=== Category Distribution ===")
            print(categories.value_counts())

            return categories.value_counts()

        except Exception as e:
            print(f"Error in debug: {e}")
            return None
    def check_mappings(self):
      print(f"Max user index: {max(self.customer_id_mapping.values())}")
      print(f"Max product index: {max(self.product_id_mapping.values())}")
      print(f"Num users: {len(self.customer_id_mapping)}")
      print(f"Num products: {len(self.product_id_mapping)}")

    def save_models(self):
        print("Saving models...")

        # Save collaborative model
        if self.collaborative_model:
            self.collaborative_model.save('Model/Recommendation/output/collaborative_model.keras')

            num_users = max(self.customer_id_mapping.values()) + 1
            num_items = max(self.product_id_mapping.values()) + 1
            dummy_size = min(10, num_users * num_items)
            dummy_users = np.zeros((dummy_size,), dtype=np.int32)
            dummy_items = np.zeros((dummy_size,), dtype=np.int32)
            # Thay vì chỉ test dummy zeros, test index biên
            for u in [0, max(self.customer_id_mapping.values())]:
                for i in [0, max(self.product_id_mapping.values())]:
                    self.collaborative_model.predict(
                        [np.array([u], dtype=np.int32), np.array([i], dtype=np.int32)],
                        verbose=0
                    )


            converter = tf.lite.TFLiteConverter.from_keras_model(self.collaborative_model)
            # ⚡ Giảm yêu cầu version op xuống thấp nhất
            converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]


            # Có thể bật experimental lowering version nếu cần
            # converter.experimental_new_converter = False

            print("Converting collaborative_model...")
            tflite_model = converter.convert()
            with open('Model/Recommendation/output/collaborative_model.tflite', 'wb') as f:
                f.write(tflite_model)
            print("Saved collaborative_model.tflite")

        # Save content-based model
        if self.content_based_similarity is not None:
            cb_model = self.build_content_based_tflite_model(top_k=5)
            converter = tf.lite.TFLiteConverter.from_keras_model(cb_model)

            converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
            # Bạn có thể bỏ optimize nếu thấy export lỗi với FULLY_CONNECTED
            # converter.optimizations = [tf.lite.Optimize.DEFAULT]

            print("Converting content_based model...")
            tflite_cb = converter.convert()
            with open('Model/Recommendation/output/content_based.tflite', 'wb') as f:
                f.write(tflite_cb)
            print("Saved content_based.tflite")

        # Save similarity matrix
        if self.content_based_similarity is not None:
            np.save('Model/Recommendation/output/content_similarity_matrix.npy', self.content_based_similarity)

        # Save mappings
        mappings = {
            'customer_id_mapping': self.customer_id_mapping,
            'product_id_mapping': self.product_id_mapping,
            'reverse_product_mapping': self.reverse_product_mapping
        }
        with open('Model/Recommendation/output/id_mappings.json', 'w') as f:
            json.dump(mappings, f)

        # Save scalers
        with open('Model/Recommendation/output/price_scaler.pkl', 'wb') as f:
            pickle.dump(self.price_scaler, f)

        with open('Model/Recommendation/output/category_encoder.pkl', 'wb') as f:
            pickle.dump(self.category_encoder, f)

        print("Models saved successfully!")


    def get_most_popular_products(self, customer_behavior, top_k=5):
            popular_products = (
                customer_behavior.groupby('product_id').size()
                .sort_values(ascending=False).head(top_k)
            )
            return list(popular_products.index)
    def get_bought_together_recommendations(self, customer_behavior, product_id, top_k=5):
            co_occurrence = (
                customer_behavior.groupby(['customer_id', 'product_id']).size().reset_index(name='count')
            )
            users_bought = co_occurrence[co_occurrence['product_id'] == product_id]['customer_id'].unique()
            bought_together = (
                co_occurrence[
                    (co_occurrence['customer_id'].isin(users_bought)) &
                    (co_occurrence['product_id'] != product_id)
                ]
                .groupby('product_id').size().sort_values(ascending=False).head(top_k)
            )
            return list(bought_together.index)


# Sử dụng
if __name__ == "__main__":
    # Khởi tạo system
    rec_system = DualRecommendationSystem()

    # Train models
    history = rec_system.train_models(epochs=30)

     # Reload data to use for testing recommendations
    customer_behavior, _ = rec_system.load_and_preprocess_data()

    # Test similarity
    similar_products = rec_system.get_similar_products('67c099e8ac22517ab6403a78', top_k=5)
    print("Similar products:", similar_products)

    # Test: Gợi ý cold start (most popular)
    popular_products = rec_system.get_most_popular_products(customer_behavior, top_k=5)
    print("Cold start (most popular products):", popular_products)

    # Test: Gợi ý bought together
    bought_together = rec_system.get_bought_together_recommendations(customer_behavior, '67c099e8ac22517ab6403a78', top_k=5)
    print("Bought together recommendations:", bought_together)

    rec_system.check_mappings()
    rec_system.save_models()