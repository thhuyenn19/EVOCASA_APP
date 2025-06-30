const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');
const fs = require('fs');

console.log("Initializing Firebase...");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://evocasa-da7f2.firebaseio.com"
});

console.log("Firebase initialized, reading file...");
const db = admin.firestore();
const data = JSON.parse(fs.readFileSync('EvoCasa.Product.json', 'utf8'));

console.log(`Data loaded: ${data.length} products found`);

async function uploadProducts() {
  try {
    for (const item of data) {
      const id = item.id; // Sử dụng trường id làm document ID
      console.log(`Adding document ${id}...`);
      
      // Tạo một bản copy của item và xóa trường id để tránh trùng lặp
      const productData = { ...item };
      delete productData.id;
      
      await db.collection('Product').doc(id).set(productData);
      console.log(`Added document ${id} successfully`);
    }
    
    console.log("All products uploaded successfully!");
    process.exit(0);
    
  } catch (error) {
    console.error('Error uploading products:', error);
    process.exit(1);
  }
}

// Gọi hàm upload
uploadProducts();