import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { IProduct } from '../interfaces/product';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private apiUrl = 'http://localhost:3002/products';
  constructor(private http: HttpClient) {}

  getProducts(): Observable<IProduct[]> {
    return this.http.get<IProduct[]>(this.apiUrl).pipe(
      map((products) => this.processProductImages(products)),
      catchError(this.handleError)
    );
  }

  getProductByIdentifier(identifier: string): Observable<IProduct> {
    return this.http
      .get<IProduct>(`${this.apiUrl}/${encodeURIComponent(identifier)}`)
      .pipe(
        map((product) => this.processProductImage(product)),
        catchError(this.handleError)
      );
  }

  createProduct(product: IProduct): Observable<IProduct> {
    return this.http.post<IProduct>(this.apiUrl, product).pipe(
      map((product) => this.processProductImage(product)),
      catchError(this.handleError)
    );
  }

  updateProduct(identifier: string, product: IProduct): Observable<IProduct> {
    return this.http
      .put<IProduct>(
        `${this.apiUrl}/${encodeURIComponent(identifier)}`,
        product
      )
      .pipe(
        map((product) => this.processProductImage(product)),
        catchError(this.handleError)
      );
  }
  deleteProduct(identifier: string): Observable<any> {
    return this.http
      .delete<any>(`${this.apiUrl}/${encodeURIComponent(identifier)}`)
      .pipe(catchError(this.handleError));
  }


  private processProductImages(products: IProduct[]): IProduct[] {
    return products.map((product) => this.processProductImage(product));
  }


  private processProductImage(product: IProduct): IProduct {
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
  
    return this.http.post<{ filename: string }>('http://localhost:3002/upload', formData).pipe(
      map(response => `http://localhost:3002/image/${response.filename}`)
    );
}}
