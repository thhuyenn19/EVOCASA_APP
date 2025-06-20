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
} from 'firebase/firestore';
import { db } from '../firebase-config';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  // REST API URL is kept only for image uploading or fallback
  private apiUrl = 'http://localhost:3002/products';

  constructor(private http: HttpClient) {}

  /**
   * Fetch all products from Firestore collection "Product".
   */
  getProducts(): Observable<IProduct[]> {
    const productRef = collection(db, 'Product');
    const promise = getDocs(productRef).then((snapshot) => {
      return snapshot.docs.map((d) => {
        const data = d.data() as any;
        const product: IProduct = {
          _id: d.id,
          Name: data.Name,
          Price: data.Price,
          Image: this.parseImageField(data.Image),
          Description: data.Description,
          Quantity: data.Quantity,
          category_id:
            data.category_id ||
            data.Category_id ||
            data.categoryId ||
            data.CategoryId,
          Origin: data.Origin || '',
          Uses: data.Uses || '',
          Store: data.Store || '',
          Create_date: data.Create_date
            ? new Date(data.Create_date)
            : new Date(),
        } as IProduct;

        return this.processProductImage(product);
      });
    });

    return from(promise);
  }

  /**
   * Fetch a single product by document ID (identifier)
   */
  getProductByIdentifier(identifier: string): Observable<IProduct> {
    const docRef = doc(db, 'Product', identifier);
    const promise = getDoc(docRef).then((docSnap) => {
      if (!docSnap.exists()) {
        throw new Error('Product not found');
      }

      const data = docSnap.data() as any;
      const product: IProduct = {
        _id: docSnap.id,
        Name: data.Name,
        Price: data.Price,
        Image: this.parseImageField(data.Image),
        Description: data.Description,
        Quantity: data.Quantity,
        category_id:
          data.category_id ||
          data.Category_id ||
          data.categoryId ||
          data.CategoryId,
        Origin: data.Origin || '',
        Uses: data.Uses || '',
        Store: data.Store || '',
        Create_date: data.Create_date ? new Date(data.Create_date) : new Date(),
      } as IProduct;

      return this.processProductImage(product);
    });

    return from(promise);
  }

  /**
   * Create a product document in Firestore.
   */
  createProduct(product: IProduct): Observable<IProduct> {
    // Auto-generated document ID
    const newDocRef = doc(collection(db, 'Product'));
    const payload = {
      ...product,
      Image: product.Image, // stored as array
    } as any;

    const promise = setDoc(newDocRef, payload).then(() => {
      return { ...product, _id: newDocRef.id } as IProduct;
    });

    return from(promise);
  }

  /**
   * Update an existing product document.
   */
  updateProduct(identifier: string, product: IProduct): Observable<IProduct> {
    const docRef = doc(db, 'Product', identifier);
    const promise = updateDoc(docRef, { ...product }).then(() => product);
    return from(promise);
  }

  /**
   * Delete a product document.
   */
  deleteProduct(identifier: string): Observable<any> {
    const docRef = doc(db, 'Product', identifier);
    return from(deleteDoc(docRef));
  }

  private processProductImages(products: IProduct[]): IProduct[] {
    return products.map((product) => this.processProductImage(product));
  }

  private processProductImage(product: IProduct): IProduct {
    // Normalize _id: convert MongoDB ObjectId object to string if necessary
    if (product && product._id && typeof product._id === 'object') {
      const maybeOid = product._id as any;
      if (maybeOid.$oid) {
        product._id = maybeOid.$oid;
      } else if (maybeOid.toString) {
        product._id = maybeOid.toString();
      }
    }

    if (product.Image && typeof product.Image === 'string') {
      try {
        product.Image = JSON.parse(product.Image as unknown as string);
      } catch (e) {
        product.Image = [product.Image as unknown as string];
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

  uploadImage(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('image', file);

    return this.http
      .post<{ filename: string }>('http://localhost:3002/upload', formData)
      .pipe(
        map((response) => `http://localhost:3002/image/${response.filename}`)
      );
  }

  private parseImageField(image: any): any {
    if (typeof image === 'string') {
      try {
        return JSON.parse(image);
      } catch (e) {
        return [image];
      }
    } else if (Array.isArray(image)) {
      return image;
    } else {
      throw new Error('Invalid image format');
    }
  }
}
