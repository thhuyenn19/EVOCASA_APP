// const admin = require('firebase-admin');
// const serviceAccount = require('./serviceAccountKey.json');
// const fs = require('fs');

// console.log("Initializing Firebase...");
// admin.initializeApp({
//   credential: admin.credential.cert(serviceAccount),
//   databaseURL: "https://evocasa-da7f2.firebaseio.com" });

// console.log("Firebase initialized, reading file...");
// const db = admin.firestore();
// const data = JSON.parse(fs.readFileSync('EvoCasa.Category.json', 'utf8'));

// console.log("Data loaded:", data);
// for (const item of data) {
//   const id = item._id['$oid']; // Lấy ObjectId làm document ID
//   console.log(`Adding document ${id}...`);
//   db.collection('Category').doc(id).set(item)
//     .then(() => console.log(`Added document ${id} successfully`))
//     .catch(error => console.error(`Error adding ${id}:`, error));
// }
// const admin = require('firebase-admin');
// const serviceAccount = require('./serviceAccountKey.json');
// const fs = require('fs');

// console.log("Initializing Firebase...");
// admin.initializeApp({
//   credential: admin.credential.cert(serviceAccount),
//   databaseURL: "https://evocasa-da7f2.firebaseio.com"
// });

// console.log("Firebase initialized, reading file...");
// const db = admin.firestore();

// // Đọc file JSON chứa dữ liệu khuyến mãi
// const data = JSON.parse(fs.readFileSync('EvoCasa.Voucher.json', 'utf8'));

// console.log("Data loaded:", data);

// // Duyệt từng phần tử để xử lý ngày tháng và upload
// for (const item of data) {
//   const id = item._id['$oid']; // ObjectId làm document ID
//   const expireDateStr = item.ExpireDate?.["$date"];
//   const converted = {
//     ...item,
//     ExpireDate: new Date(expireDateStr)  // convert "$date" thành Date object
//   };

//   console.log(`Adding document ${id}...`);
//   db.collection('Voucher').doc(id).set(converted)
//     .then(() => console.log(`✅ Added document ${id} successfully`))
//     .catch(error => console.error(`❌ Error adding ${id}:`, error));
// }
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Khởi tạo Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(require('./serviceAccountKey.json')),
  databaseURL: "https://evocasa-da7f2.firebaseio.com"
});

const db = admin.firestore();

// Đọc file JSON wishlist
const dataPath = path.join(__dirname, 'EvoCasa.Wishlist.json');
const data = JSON.parse(fs.readFileSync(dataPath, 'utf8'));

console.log("Uploading wishlist...");

for (const item of data) {
  const id = item._id["$oid"];
  const customerId = item.Customer_id["$oid"];
  const productIds = item.Product_id.map(p => p["$oid"]);
  const createdAt = new Date(item.CreatedAt["$date"]);

  const formatted = {
    Customer_id: customerId,
    Product_id: productIds,
    CreatedAt: createdAt
  };

  db.collection("Wishlist").doc(id).set(formatted)
    .then(() => console.log(`✅ Uploaded wishlist ${id}`))
    .catch(error => console.error(`❌ Failed to upload ${id}:`, error));
}
