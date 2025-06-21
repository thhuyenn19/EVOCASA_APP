import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Customer, CartItem1 } from '../interfaces/customer';
import {
  collection,
  getDocs,
  getDoc,
  doc,
  query,
  where,
  updateDoc,
  deleteDoc,
  addDoc,
} from 'firebase/firestore';
import { db } from '../firebase-config';

@Injectable({
  providedIn: 'root',
})
export class CustomerService {
  constructor() {}

  getAllCustomers(): Observable<Customer[]> {
    return new Observable(observer => {
      getDocs(collection(db, 'Customers'))
        .then(snapshot => {
          const customers: Customer[] = [];
          snapshot.forEach(docSnap => {
            customers.push({ ...(docSnap.data() as Customer), _id: docSnap.id });
          });
          observer.next(customers);
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
  }

  getCustomerById(id: string): Observable<Customer> {
    return new Observable(observer => {
      const customerRef = doc(db, 'Customer', id);
      getDoc(customerRef)
        .then(docSnap => {
          if (docSnap.exists()) {
            observer.next({ ...(docSnap.data() as Customer), _id: docSnap.id });
          } else {
            observer.error(new Error('Customer not found'));
          }
        })
        .catch(error => observer.error(error));
    });
  }

  getCustomerByPhone(phone: string): Observable<Customer> {
    return new Observable(observer => {
      const q = query(collection(db, 'Customer'), where('Phone', '==', phone));
      getDocs(q)
        .then(snapshot => {
          if (snapshot.empty) {
            observer.error(new Error('Customer with phone not found'));
            return;
          }
          const docSnap = snapshot.docs[0];
          observer.next({ ...(docSnap.data() as Customer), _id: docSnap.id });
        })
        .catch(error => observer.error(error));
    });
  }

  getCustomerByEmail(email: string): Observable<Customer> {
    return new Observable(observer => {
      const q = query(collection(db, 'Customer'), where('Mail', '==', email));
      getDocs(q)
        .then(snapshot => {
          if (snapshot.empty) {
            observer.error(new Error('Customer with email not found'));
            return;
          }
          const docSnap = snapshot.docs[0];
          observer.next({ ...(docSnap.data() as Customer), _id: docSnap.id });
        })
        .catch(error => observer.error(error));
    });
  }

  updateCustomer(customer: Customer): Observable<Customer> {
    return new Observable(observer => {
      const customerRef = doc(db, 'Customer', customer._id);
      const { _id, ...data } = customer;
      updateDoc(customerRef, data)
        .then(() => {
          observer.next(customer);
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
  }

  updateCart(customerId: string, cart: CartItem1[]): Observable<Customer> {
    return new Observable(observer => {
      const customerRef = doc(db, 'Customer', customerId);
      updateDoc(customerRef, { Cart: cart })
        .then(() => {
          this.getCustomerById(customerId).subscribe({
            next: customer => observer.next(customer),
            error: err => observer.error(err),
            complete: () => observer.complete(),
          });
        })
        .catch(error => observer.error(error));
    });
  }

  addToCart(customerId: string, productId: string, quantity: number = 1): Observable<Customer> {
    return new Observable(observer => {
      this.getCustomerById(customerId).subscribe({
        next: (customer) => {
          const cart = [...(customer.Cart || [])];
          const existingItem = cart.find(item => item.ProductId === productId);
          if (existingItem) {
            existingItem.Quantity += quantity;
          } else {
            cart.push({ ProductId: productId, Quantity: quantity });
          }

          this.updateCart(customerId, cart).subscribe({
            next: updated => observer.next(updated),
            error: err => observer.error(err),
            complete: () => observer.complete(),
          });
        },
        error: err => observer.error(err),
      });
    });
  }

  deleteCustomer(customerId: string): Observable<void> {
    return new Observable(observer => {
      const customerRef = doc(db, 'Customer', customerId);
      deleteDoc(customerRef)
        .then(() => {
          observer.next();
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
  }

  postCustomer(customer: Omit<Customer, '_id'>): Observable<Customer> {
    return new Observable(observer => {
      addDoc(collection(db, 'Customer'), customer)
        .then(docRef => {
          observer.next({ ...customer, _id: docRef.id });
          observer.complete();
        })
        .catch(error => observer.error(error));
    });
  }
  getTotalAmountByCustomerId(customerId: string): Promise<number> {
    const q = query(collection(db, 'Order')); 
  return getDocs(q)
    .then(snapshot => {
      let total = 0;
      snapshot.forEach(doc => {
        const data = doc.data();
        const orderCustomerId = data['Customer_id']?.['$oid'];

        if (orderCustomerId === customerId) {
          total += data['TotalPrice'] || 0;
        }
      });
        console.log('✅ Total for customer', customerId, '=', total);
      return total;
    })
    .catch(error => {
      console.error('❌ Error getting total amount:', error);
      return 0;
    });
}

}
