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
          return this.processProductImage({
            ...data,
            _id: docSnap.id, // Add the document ID as _id
          });
        } else {
          throw new Error('Product not found');
        }
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Create product and stringify image array
   */
  async createProduct(productData: Partial<IProduct>): Promise<string> {
    try {
      const firestoreData = {
        ...productData,
        Image: productData.Image || '', // Ensure Image is a string
      };

      const docRef = await addDoc(collection(this.firestore, 'Product'), firestoreData);
      console.log('Product added with ID:', docRef.id);
      return docRef.id;
    } catch (error) {
      console.error('Error adding product:', error);
      throw error;
    }
  }

  updateProduct(identifier: string, product: IProduct): Observable<IProduct> {
    const docRef = doc(this.firestore, 'Product', identifier);

    const rawPayload = {
      Name: product.Name,
      Price: product.Price,
      Image: typeof product.Image === 'string' ? product.Image : JSON.stringify(product.Image || []),
      Description: product.Description,
      Quantity: product.Quantity,
      Dimension: product.Dimension || '',
      category_id: product.category_id || '',
      Origin: product.Origin || '',
      Uses: product.Uses || '',
      Store: product.Store || '',
      Create_date: product.Create_date || new Date(),
    };

    const payload = Object.fromEntries(
      Object.entries(rawPayload).filter(([_, value]) => value !== undefined)
    );

    const promise = updateDoc(docRef, payload).then(() => ({
      ...product,
      _id: identifier,
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
    if (product && product._id && typeof product._id === 'object') {
      const maybeOid = product._id as any;
      if (maybeOid.$oid) {
        product._id = maybeOid.$oid;
      } else if (maybeOid.toString) {
        product._id = maybeOid.toString();
      }
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