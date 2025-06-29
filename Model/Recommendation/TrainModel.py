import pandas as pd
import tensorflow as tf
from tensorflow.keras import layers
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import StandardScaler
import pickle
import json

class HybridRecommendationSystem:
    def __init__(self):
        self.collaborative_model = None
        self.content_similarity_matrix = None
        self.tfidf_vectorizer = TfidfVectorizer(max_features=1000, stop_words='english')
        self.price_scaler = StandardScaler()
        
        # Mappings
        self.customer_id_mapping = {}
        self.product_id_mapping = {}
        self.reverse_product_mapping = {}
        self.product_features = {}
        
    def load_and_preprocess_data(self):
        """Load and preprocess both customer behavior and product data"""
        print("Loading data...")
        
        # Load customer behavior data
        customer_behavior = pd.read_csv('processed_customer_behavior.csv')
        
        # Load product data
        product_data = pd.read_json('Downloaded_Products.json')
        
        # Create product mapping
        product_mapping = product_data.set_index('id').to_dict('index')
        
        # Add product details to customer behavior
        def add_product_details(row):
            product = product_mapping.get(row['product_id'])
            if product:
                row['product_name'] = product['Name']
                row['product_description'] = product['Description']
                row['product_price'] = product['Price']
                row['product_category'] = str(product['category_id']['$oid'])
            return row
        
        customer_behavior = customer_behavior.apply(add_product_details, axis=1)
        
        # Remove rows without product details
        customer_behavior = customer_behavior.dropna(subset=['product_name'])
        
        return customer_behavior, product_data
    
    def prepare_collaborative_data(self, customer_behavior):
        """Prepare data for collaborative filtering"""
        print("Preparing collaborative filtering data...")
        
        # Create ID mappings
        self.customer_id_mapping = {id: idx for idx, id in enumerate(customer_behavior['customer_id'].unique())}
        self.product_id_mapping = {id: idx for idx, id in enumerate(customer_behavior['product_id'].unique())}
        self.reverse_product_mapping = {idx: id for id, idx in self.product_id_mapping.items()}
        
        # Encode IDs
        customer_behavior['customer_id_encoded'] = customer_behavior['customer_id'].map(self.customer_id_mapping)
        customer_behavior['product_id_encoded'] = customer_behavior['product_id'].map(self.product_id_mapping)
        
        return customer_behavior
    
    def prepare_content_data(self, product_data):
        """Prepare data for content-based filtering"""
        print("Preparing content-based filtering data...")
        
        # Create product features dictionary
        for _, product in product_data.iterrows():
            product_id = product['id']
            
            # Combine text features
            text_features = f"{product['Name']} {product['Description']}"
            
            self.product_features[product_id] = {
                'text': text_features,
                'price': product['Price'],
                'category': str(product['category_id']['$oid']),
                'name': product['Name']
            }
        
        # Create content similarity matrix
        self._build_content_similarity()
    
    def _build_content_similarity(self):
        """Build content-based similarity matrix"""
        print("Building content similarity matrix...")
        
        # Get all products that have mappings
        mapped_products = list(self.product_id_mapping.keys())
        
        # Extract text features for mapped products
        texts = []
        prices = []
        product_ids_ordered = []
        
        for product_id in mapped_products:
            if product_id in self.product_features:
                texts.append(self.product_features[product_id]['text'])
                prices.append(self.product_features[product_id]['price'])
                product_ids_ordered.append(product_id)
        
        # TF-IDF vectorization for text
        tfidf_matrix = self.tfidf_vectorizer.fit_transform(texts)
        
        # Normalize prices
        prices_normalized = self.price_scaler.fit_transform(np.array(prices).reshape(-1, 1))
        
        # Combine text and price features (weight text more heavily)
        text_weight = 0.8
        price_weight = 0.2
        
        # Convert to dense arrays
        tfidf_dense = tfidf_matrix.toarray()
        
        # Combine features
        combined_features = np.hstack([
            tfidf_dense * text_weight,
            prices_normalized * price_weight
        ])
        
        # Calculate cosine similarity
        self.content_similarity_matrix = cosine_similarity(combined_features)
        self.content_product_order = product_ids_ordered
        
        print(f"Content similarity matrix shape: {self.content_similarity_matrix.shape}")
    
    def build_collaborative_model(self, customer_behavior):
        """Build Neural Collaborative Filtering model"""
        print("Building collaborative filtering model...")
        
        num_users = len(self.customer_id_mapping)
        num_items = len(self.product_id_mapping)
        
        # Input layers
        user_input = layers.Input(shape=(1,), dtype=tf.int32, name='user_input')
        item_input = layers.Input(shape=(1,), dtype=tf.int32, name='item_input')
        
        # Embedding layers
        user_embedding = layers.Embedding(
            input_dim=num_users, 
            output_dim=64,
            embeddings_regularizer=tf.keras.regularizers.l2(0.01)
        )(user_input)
        
        item_embedding = layers.Embedding(
            input_dim=num_items, 
            output_dim=64,
            embeddings_regularizer=tf.keras.regularizers.l2(0.01)
        )(item_input)
        
        # Flatten
        user_flat = layers.Flatten()(user_embedding)
        item_flat = layers.Flatten()(item_embedding)
        
        # Concatenate
        concat = layers.Concatenate()([user_flat, item_flat])
        
        # Dense layers with dropout
        x = layers.Dense(128, activation="relu")(concat)
        x = layers.Dropout(0.2)(x)
        x = layers.Dense(64, activation="relu")(x)
        x = layers.Dropout(0.2)(x)
        x = layers.Dense(32, activation="relu")(x)
        
        # Output
        output = layers.Dense(1, activation="sigmoid")(x)
        
        # Create and compile model
        self.collaborative_model = tf.keras.Model([user_input, item_input], output)
        self.collaborative_model.compile(
            optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
            loss='mse',
            metrics=['mae']
        )
        
        return self.collaborative_model
    
    def train_collaborative_model(self, customer_behavior, epochs=20, batch_size=256):
        """Train the collaborative filtering model"""
        print("Training collaborative model...")
        
        # Normalize action_score to 0-1 range
        action_scores = customer_behavior['action_score']
        normalized_scores = (action_scores - action_scores.min()) / (action_scores.max() - action_scores.min())
        
        # Prepare training data
        user_ids = customer_behavior['customer_id_encoded'].values
        item_ids = customer_behavior['product_id_encoded'].values
        
        # Add early stopping
        callbacks = [
            tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True),
            tf.keras.callbacks.ReduceLROnPlateau(factor=0.5, patience=3)
        ]
        
        # Train model
        history = self.collaborative_model.fit(
            [user_ids, item_ids], 
            normalized_scores,
            epochs=epochs,
            batch_size=batch_size,
            validation_split=0.2,
            callbacks=callbacks,
            verbose=1
        )
        
        return history
    
    def get_content_based_recommendations(self, product_id, top_k=10):
        """Get content-based recommendations for a product"""
        try:
            # Find product index in content matrix
            if product_id not in self.content_product_order:
                print(f"Product {product_id} not found in content data")
                return []
            
            product_idx = self.content_product_order.index(product_id)
            
            # Get similarity scores
            similarity_scores = self.content_similarity_matrix[product_idx]
            
            # Get top similar products (excluding itself)
            similar_indices = np.argsort(similarity_scores)[::-1][1:top_k+1]
            
            recommendations = []
            for idx in similar_indices:
                similar_product_id = self.content_product_order[idx]
                similarity_score = similarity_scores[idx]
                product_name = self.product_features[similar_product_id]['name']
                
                recommendations.append({
                    'product_id': similar_product_id,
                    'product_name': product_name,
                    'similarity_score': float(similarity_score)
                })
            
            return recommendations
            
        except Exception as e:
            print(f"Error in content-based recommendations: {e}")
            return []
    
    def get_collaborative_recommendations(self, customer_id, top_k=10, exclude_interacted=True):
        """Get collaborative filtering recommendations for a user"""
        try:
            if customer_id not in self.customer_id_mapping:
                print(f"Customer {customer_id} not found")
                return []
            
            customer_encoded = self.customer_id_mapping[customer_id]
            
            # Get all products
            all_product_ids = list(self.product_id_mapping.values())
            customer_array = np.full(len(all_product_ids), customer_encoded)
            
            # Predict scores
            predictions = self.collaborative_model.predict(
                [customer_array, all_product_ids], 
                verbose=0
            ).flatten()
            
            # Create product-score pairs
            product_scores = list(zip(all_product_ids, predictions))
            
            # Sort by score
            product_scores.sort(key=lambda x: x[1], reverse=True)
            
            # Get top recommendations
            recommendations = []
            for product_encoded, score in product_scores[:top_k]:
                original_product_id = self.reverse_product_mapping[product_encoded]
                
                if original_product_id in self.product_features:
                    product_name = self.product_features[original_product_id]['name']
                    recommendations.append({
                        'product_id': original_product_id,
                        'product_name': product_name,
                        'predicted_score': float(score)
                    })
            
            return recommendations
            
        except Exception as e:
            print(f"Error in collaborative recommendations: {e}")
            return []
    
    def save_models(self):
        """Save all models and mappings"""
        print("Saving models...")
        
        # Save collaborative model
        self.collaborative_model.save('collaborative_model.h5')
        
        # Convert to TFLite
        converter = tf.lite.TFLiteConverter.from_keras_model(self.collaborative_model)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        tflite_model = converter.convert()
        
        with open('collaborative_model.tflite', 'wb') as f:
            f.write(tflite_model)
        
        # Save content similarity matrix and mappings
        np.save('content_similarity_matrix.npy', self.content_similarity_matrix)
        
        # Save all mappings and features
        with open('recommendation_data.pkl', 'wb') as f:
            pickle.dump({
                'customer_id_mapping': self.customer_id_mapping,
                'product_id_mapping': self.product_id_mapping,
                'reverse_product_mapping': self.reverse_product_mapping,
                'product_features': self.product_features,
                'content_product_order': self.content_product_order,
                'tfidf_vectorizer': self.tfidf_vectorizer,
                'price_scaler': self.price_scaler
            }, f)
        
        print("All models and data saved!")

# Usage example
def main():
    # Initialize system
    rec_system = HybridRecommendationSystem()
    
    # Load and prepare data
    customer_behavior, product_data = rec_system.load_and_preprocess_data()
    customer_behavior = rec_system.prepare_collaborative_data(customer_behavior)
    rec_system.prepare_content_data(product_data)
    
    # Build and train collaborative model
    collaborative_model = rec_system.build_collaborative_model(customer_behavior)
    history = rec_system.train_collaborative_model(customer_behavior, epochs=30)
    
    # Save models
    rec_system.save_models()
    
    # Test recommendations
    print("\n=== TESTING RECOMMENDATIONS ===")
    
    # Test content-based (for product detail page)
    sample_product_id = customer_behavior['product_id'].iloc[0]
    content_recs = rec_system.get_content_based_recommendations(sample_product_id, top_k=5)
    print(f"\nContent-based recommendations for product {sample_product_id}:")
    for rec in content_recs:
        print(f"- {rec['product_name']}: {rec['similarity_score']:.3f}")
    
    # Test collaborative (for "might you like")
    sample_customer_id = customer_behavior['customer_id'].iloc[0]
    collab_recs = rec_system.get_collaborative_recommendations(sample_customer_id, top_k=5)
    print(f"\nCollaborative recommendations for customer {sample_customer_id}:")
    for rec in collab_recs:
        print(f"- {rec['product_name']}: {rec['predicted_score']:.3f}")

if __name__ == "__main__":
    main()