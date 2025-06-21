import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { environment } from '../environments/environment';
import { getStorage } from 'firebase/storage'; // Thêm dòng này

const firebaseApp = initializeApp(environment.firebase);

const storage = getStorage(firebaseApp);

export { storage };

// ✅ Bạn có thể export auth/db ra để dùng
export const auth = getAuth(firebaseApp);
export const db = getFirestore(firebaseApp);

console.log('✅ Firebase connected:', firebaseApp.name);

import { collection, getDocs } from 'firebase/firestore';

async function testFirestoreCollection() {
  const querySnapshot = await getDocs(collection(db, 'Admin'));
  querySnapshot.forEach(doc => {
    console.log(`📄 ${doc.id} =>`, doc.data());
  });
}

testFirestoreCollection();

