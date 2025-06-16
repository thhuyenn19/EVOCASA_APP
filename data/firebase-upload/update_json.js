// const admin = require('firebase-admin');
// const serviceAccount = require('./serviceAccountKey.json');
// const fs = require('fs');
// const path = require('path');

// // Khởi tạo Firebase
// admin.initializeApp({
//   credential: admin.credential.cert(serviceAccount),
//   storageBucket: "evocasa-da7f2.firebasestorage.app"
// });

// const bucket = admin.storage().bucket();
// const jsonFilePath = path.join(__dirname, 'EvoCasa.Category.json'); // Đường dẫn file JSON
// const db = admin.firestore();

// // Đọc file JSON
// const data = JSON.parse(fs.readFileSync(jsonFilePath, 'utf8'));

// // Hàm lấy URL từ Storage dựa trên đường dẫn cục bộ
// async function getPublicUrl(localPath) {
//   // Làm sạch URL hoặc đường dẫn, giữ lại phần file path
//   let storagePath = localPath;
//   if (localPath.startsWith('http://') || localPath.startsWith('https://')) {
//     // Nếu là URL, trích xuất phần đường dẫn file
//     const url = new URL(localPath);
//     storagePath = decodeURIComponent(url.pathname.slice(1)); // Loại bỏ "/" đầu và giải mã
//   } else {
//     storagePath = localPath.replace(/^\//, ''); // Loại bỏ "/" đầu nếu là đường dẫn cục bộ
//   }
//   if (storagePath.length > 1024) {
//     throw new Error(`Storage path ${storagePath} exceeds 1024 characters`);
//   }
//   const file = bucket.file(storagePath);
//   try {
//     const [exists] = await file.exists(); // Kiểm tra file tồn tại
//     if (exists) {
//       return `https://firebasestorage.googleapis.com/v0/b/evocasa-da7f2.firebasestorage.app/o/${encodeURIComponent(storagePath)}?alt=media`;
//     } else {
//       throw new Error(`File not found: ${storagePath}`);
//     }
//   } catch (error) {
//     console.error(`Error checking URL for ${storagePath}:`, error);
//     return localPath; // Giữ nguyên nếu lỗi
//   }
// }

// // Cập nhật đường dẫn ảnh trong mỗi item
// async function updateImageUrls() {
//   for (const item of data) {
//     if (item.Image) {
//       const updatedImage = await getPublicUrl(item.Image);
//       if (updatedImage !== item.Image) {
//         item.Image = updatedImage;
//         console.log(`Updated ${item.Image} for ${item.Name}`);

//         // Cập nhật Firestore
//         const id = item._id['$oid'];
//         await db.collection('Category').doc(id).set(item, { merge: true })
//           .then(() => console.log(`Updated Firestore document ${id}`))
//           .catch(error => console.error(`Error updating Firestore ${id}:`, error));
//       } else {
//         console.log(`No change for ${item.Image} in ${item.Name}`);
//       }
//     }
//   }

//   // Lưu lại file JSON đã cập nhật
//   fs.writeFileSync(jsonFilePath, JSON.stringify(data, null, 2));
//   console.log("JSON file updated successfully");
// }

// updateImageUrls().catch(error => console.error("Error in update process:", error));
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');
const fs = require('fs');
const path = require('path');

// Khởi tạo Firebase
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: "evocasa-da7f2.firebasestorage.app"
});

const bucket = admin.storage().bucket();
const jsonFilePath = path.join(__dirname, 'EvoCasa.Product.json'); // Đường dẫn file JSON sản phẩm
const db = admin.firestore();

// Đọc file JSON
const data = JSON.parse(fs.readFileSync(jsonFilePath, 'utf8'));

// Hàm lấy URL từ Storage dựa trên đường dẫn cục bộ
async function getPublicUrl(localPath) {
  const storagePath = localPath.replace(/^\//, ''); // Loại bỏ "/" đầu tiên
  const file = bucket.file(storagePath);
  try {
    const [exists] = await file.exists(); // Kiểm tra file tồn tại
    if (exists) {
      return `https://firebasestorage.googleapis.com/v0/b/evocasa-da7f2.firebasestorage.app/o/${encodeURIComponent(storagePath)}?alt=media`;
    } else {
      throw new Error(`File not found: ${storagePath}`);
    }
  } catch (error) {
    console.error(`Error checking URL for ${storagePath}:`, error);
    return localPath; // Giữ nguyên nếu lỗi
  }
}

// Cập nhật đường dẫn ảnh trong mỗi item
async function updateImageUrls() {
  for (const item of data) {
    if (item.Image) {
      // Parse chuỗi JSON trong Image thành mảng
      let imageArray = [];
      try {
        imageArray = JSON.parse(item.Image.replace(/'/g, '"')); // Thay ' bằng " nếu cần
      } catch (e) {
        console.error(`Error parsing Image for ${item.Name}:`, e);
        imageArray = [item.Image]; // Nếu không parse được, dùng giá trị gốc
      }

      const updatedImages = await Promise.all(imageArray.map(async (localPath) => {
        return await getPublicUrl(localPath);
      }));

      const hasChanged = updatedImages.some((url, index) => url !== imageArray[index]);
      if (hasChanged) {
        item.Image = JSON.stringify(updatedImages); // Ghi lại thành chuỗi JSON
        console.log(`Updated Images for ${item.Name}:`, updatedImages);

        // Cập nhật Firestore
        const id = item._id['$oid'];
        await db.collection('Product').doc(id).set(item, { merge: true })
          .then(() => console.log(`Updated Firestore document ${id}`))
          .catch(error => console.error(`Error updating Firestore ${id}:`, error));
      } else {
        console.log(`No change for Images in ${item.Name}`);
      }
    }
  }

  // Lưu lại file JSON đã cập nhật
  fs.writeFileSync(jsonFilePath, JSON.stringify(data, null, 2));
  console.log("JSON file updated successfully");
}

updateImageUrls().catch(error => console.error("Error in update process:", error));