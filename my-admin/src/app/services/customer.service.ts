import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of, retry } from 'rxjs';
import { catchError, map, tap, switchMap } from 'rxjs/operators';
import { Customer, ICustomer, CartItem1 } from '../interfaces/customer';

@Injectable({
  providedIn: 'root',
})
export class CustomerService {
  private apiUrl = 'http://localhost:3002';

  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}


  getAllCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/customers`).pipe(
      tap((_) => console.log('Fetched all customers')),
      catchError(this.handleError<Customer[]>('getAllCustomers', []))
    );
  }

  getCustomerById(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/customer/${id}`).pipe(
      tap((_) => console.log(`Fetched customer id=${id}`)),
      catchError(this.handleError<Customer>('getCustomerById'))
    );
  }

  getCustomerByPhone(phone: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/customers/${phone}`).pipe(
      tap((_) => console.log(`Fetched customer with phone=${phone}`)),
      catchError(this.handleError<Customer>('getCustomerByPhone'))
    );
  }

  getCustomerByEmail(email: string): Observable<Customer> {
    return this.http.get<Customer[]>(`${this.apiUrl}/customers`).pipe(
      map((customers) => {
        const foundCustomer = customers.find(
          (customer) => customer.Mail === email
        );
        if (!foundCustomer) {
          throw new Error(`No customer found with email=${email}`);
        }
        return foundCustomer;
      }),
      tap((customer) => console.log(`Found customer with email=${email}`)),
      catchError(this.handleError<Customer>('getCustomerByEmail'))
    );
  }


  updateCustomer(customer: Customer): Observable<Customer> {
    return this.http
      .put<Customer>(
        `${this.apiUrl}/customers/${customer._id}`,
        customer,
        this.httpOptions
      )
      .pipe(
        tap((_) => console.log(`Updated customer id=${customer._id}`)),
        catchError(this.handleError<Customer>('updateCustomer'))
      );
  }


  updateCart(customerId: string, cart: CartItem1[]): Observable<Customer> {
    return this.http
      .put<Customer>(
        `${this.apiUrl}/customers/${customerId}/cart`,
        { Cart: cart },
        this.httpOptions
      )
      .pipe(
        tap((_) => console.log(`Updated cart for customer id=${customerId}`)),
        catchError(this.handleError<Customer>('updateCart'))
      );
  }


  addToCart(
    customerId: string,
    productId: string,
    quantity: number = 1
  ): Observable<Customer> {
    return this.getCustomerById(customerId).pipe(
      switchMap((customer) => {
        const cart = [...customer.Cart];
        const existingItem = cart.find((item) => item.ProductId === productId);

        if (existingItem) {
          existingItem.Quantity += quantity;
        } else {
          cart.push({ ProductId: productId, Quantity: quantity });
        }

        return this.updateCart(customerId, cart);
      }),
      catchError(this.handleError<Customer>('addToCart'))
    );
  }

  deleteCustomer(customerId: string): Observable<any> {
    return this.http
      .delete(`${this.apiUrl}/customers/${customerId}`, this.httpOptions)
      .pipe(
        tap((_) => console.log(`Deleted customer id=${customerId}`)),
        catchError(this.handleError<any>('deleteCustomer'))
      );
  }
  postCustomer(customer: any): Observable<any> {
    const headers = new HttpHeaders().set(
      'Content-Type',
      'application/json;charset=utf-8'
    );
    const requestOptions: Object = {
      headers: headers,
      responseType: 'text',
    };
    return this.http
      .post<any>(`${this.apiUrl}/customers`, customer, requestOptions)
      .pipe(
        map((res) => JSON.parse(res) as Array<Customer>),
        retry(3),
        catchError(this.handleError<Array<Customer>>('postCustomer', []))
      );
  }


  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      console.error(error);
      return of(result as T);
    };
  }
}