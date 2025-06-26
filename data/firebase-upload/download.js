const admin = require('firebase-admin');
const fs = require('fs');

admin.initializeApp({
  credential: admin.credential.cert(require('./serviceAccountKey.json'))
});

const db = admin.firestore();

// 🔁 Hàm tải từng collection
async function downloadCollection(collectionName, outputFileName) {
  const snapshot = await db.collection(collectionName).get();
  const data = [];

  snapshot.forEach(doc => {
    data.push({
      _id: doc.id,
      ...doc.data()
    });
  });

  fs.writeFileSync(outputFileName, JSON.stringify(data, null, 2), 'utf8');
  console.log(`✅ Downloaded ${data.length} docs from '${collectionName}' → ${outputFileName}`);
}

// 📦 Danh sách collections cần tải
const collectionsToDownload = [
  { name: "Order", output: "downloaded_orders.json" }

];

// 🔽 Gọi tải tất cả
(async () => {
  for (const col of collectionsToDownload) {
    await downloadCollection(col.name, col.output);
  }
  console.log("🎉 All collections downloaded successfully.");
  process.exit(0);
})();
