import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, from } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { IProduct } from '../interfaces/product';

// Firebase Firestore imports
import {
  collection,
  getDocs,
  doc,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
  addDoc,
} from 'firebase/firestore';
import { db } from '../firebase-config';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private firestore = db; // Reference to Firestore

  constructor(private http: HttpClient) {}

  /**
   * Fetch all products from Firestore collection "Product".
   */
  getProducts(): Observable<IProduct[]> {
    const productsCollection = collection(this.firestore, 'Product');
    return from(getDocs(productsCollection)).pipe(
      map((snapshot) => {
        const products: IProduct[] = snapshot.docs.map((doc) => {
          const data = doc.data() as IProduct;
          return {
            ...data,
            _id: doc.id, // Add the document ID as _id
          };
        });
        return this.processProductImages(products);
      }),
      catchError(this.handleError)
    );
  }

  getProductByIdentifier(identifier: string): Observable<IProduct> {
  const productDoc = doc(this.firestore, 'Product', identifier);

  return from(getDoc(productDoc)).pipe(
    map((docSnap) => {
      if (docSnap.exists()) {
        const data = docSnap.data() as IProduct;

        // ✅ Chuyển _id và category_id từ {$oid: "..."} về string
        const converted: IProduct = {
          ...data,
          _id: this.extractOid(data._id) || docSnap.id,
          category_id: this.extractOid(data.category_id),
        };

        return this.processProductImage(converted); // nếu bạn cần xử lý ảnh riêng
      } else {
        throw new Error('Product not found');
      }
    }),
    catchError(this.handleError)
  );
}
private extractOid(field: any): string {
  return (field && typeof field === 'object' && '$oid' in field) ? field.$oid : field;
}

  /**
   * Create product and stringify image array
   */
  async createProduct(productData: Partial<IProduct>): Promise<string> {
  try {
    // Tạo ID Firestore
    const newDocRef = doc(collection(db, 'Product'));
    const newId = newDocRef.id;

    // Tạo dữ liệu đúng định dạng
    const firestoreData = {
      ...productData,
      _id: { $oid: newId },
      category_id: typeof productData.category_id === 'string'
        ? { $oid: productData.category_id }
        : productData.category_id || '',
      Image: productData.Image || '',
      Create_date: productData.Create_date || new Date(),
    };

    // Ghi document
    await setDoc(newDocRef, firestoreData);

    console.log('Product added with ID:', newId);
    return newId;
  } catch (error) {
    console.error('Error adding product:', error);
    throw error;
  }
}

  updateProduct(identifier: string, product: IProduct): Observable<IProduct> {
  const docRef = doc(this.firestore, 'Product', identifier);

  // ✅ Ép ảnh thành chuỗi nếu là mảng
  const preparedImage = Array.isArray(product.Image)
    ? JSON.stringify(product.Image)
    : product.Image || '[]';

  // ✅ Tạo dữ liệu raw có định dạng $oid
  const rawPayload: any = {
    ...product,
    Image: preparedImage,
    Create_date: product.Create_date || new Date(),

    // ✅ category_id dạng map
    category_id:
      typeof product.category_id === 'string'
        ? { $oid: product.category_id }
        : product.category_id,

    // ✅ _id dạng map (dựa vào identifier truyền vào)
    _id:
      typeof product._id === 'string'
        ? { $oid: product._id }
        : product._id || { $oid: identifier }
  };

  // ✅ Loại bỏ tất cả field undefined
  const payload = Object.fromEntries(
    Object.entries(rawPayload).filter(([_, value]) => value !== undefined)
  );

  // ✅ Ghi dữ liệu lên Firestore
  const promise = setDoc(docRef, payload).then(() => ({
    ...product,
    _id: identifier
  }));

  return from(promise);
}

  deleteProduct(identifier: string): Observable<any> {
    const docRef = doc(this.firestore, 'Product', identifier);
    return from(deleteDoc(docRef));
  }

  private processProductImages(products: IProduct[]): IProduct[] {
    return products.map((product) => this.processProductImage(product));
  }

  private processProductImage(product: IProduct): IProduct {
  // Convert _id.$oid to string
  if (product._id && typeof product._id === 'object' && '$oid' in product._id) {
    product._id = product._id.$oid;
  }

  // Convert category_id.$oid to string
  if (product.category_id && typeof product.category_id === 'object' && '$oid' in product.category_id) {
    product.category_id = product.category_id.$oid;
  }

  return product;
}

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }

  // Keep uploadImage if still needed for Firebase Storage
  uploadImage(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('image', file);

    return this.http
      .post<{ filename: string }>('http://localhost:3002/upload', formData)
      .pipe(
        map((response) => `http://localhost:3002/image/${response.filename}`)
      );
  }
}