import { Injectable } from '@angular/core';
// import { Observable } from 'rxjs';
import { Customer, CartItem1 } from '../interfaces/customer';
import { Observable, from, map, catchError, throwError } from 'rxjs';

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

import { writeBatch } from 'firebase/firestore';


// Interface for Shipping Address
interface ShippingAddress {
  id?: string;
  Address: string;
  Name: string;
  Phone: string;
  IsDefault: boolean;
}

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
      const customerRef = doc(db, 'Customers', id);
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
      const q = query(collection(db, 'Customers'), where('Phone', '==', phone));
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
      const q = query(collection(db, 'Customers'), where('Mail', '==', email));
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
      const customerRef = doc(db, 'Customers', customer._id);
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
      const customerRef = doc(db, 'Customers', customerId);
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
      const customerRef = doc(db, 'Customers', customerId);
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
      addDoc(collection(db, 'Customers'), customer)
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



// Method to get shipping addresses for a specific customer
getShippingAddresses(customerId: string): Observable<ShippingAddress[]> {
  console.log('Fetching shipping addresses for customer:', customerId);
  
  // Try multiple possible collection paths
  const possiblePaths = [
    collection(db, 'Customers', customerId, 'ShippingAddresses'),
    collection(db, 'customers', customerId, 'ShippingAddresses'),
    collection(db, 'Customer', customerId, 'ShippingAddresses'),
    collection(db, 'Customers', customerId, 'shippingAddresses')
  ];
  
  const tryPath = async (pathIndex: number): Promise<ShippingAddress[]> => {
    if (pathIndex >= possiblePaths.length) {
      // If all subcollection paths fail, try the alternative method
      console.log('All subcollection paths failed, trying document field...');
      return this.getShippingAddressesFromDocument(customerId);
    }
    
    try {
      const snapshot = await getDocs(possiblePaths[pathIndex]);
      console.log(`Path ${pathIndex} snapshot size:`, snapshot.size);
      console.log(`Path ${pathIndex} docs:`, snapshot.docs.length);
      
      if (snapshot.size > 0) {
        const addresses = snapshot.docs.map(doc => {
          const data = doc.data() as Record<string, any>;
          console.log('Raw address data:', data);
          return {
            id: doc.id,
            Address: data['Address'] || '',
            Name: data['Name'] || '',
            Phone: data['Phone'] || '',
            IsDefault: data['IsDefault'] || false
          } as ShippingAddress;
        });
        
        console.log('Shipping addresses loaded from path', pathIndex, ':', addresses);
        return addresses;
      } else {
        console.log(`Path ${pathIndex} returned empty, trying next...`);
        return tryPath(pathIndex + 1);
      }
    } catch (error) {
      console.error(`Error with path ${pathIndex}:`, error);
      return tryPath(pathIndex + 1);
    }
  };

  return from(tryPath(0));
}

// Helper method to get addresses from document field
private getShippingAddressesFromDocument(customerId: string): Promise<ShippingAddress[]> {
  const customerDocRef = doc(db, 'Customers', customerId);
  
  return getDoc(customerDocRef).then(docSnap => {
    if (docSnap.exists()) {
      const data = docSnap.data() as Record<string, any>;
      console.log('Full customer document data:', data);
      
      // Try different possible field names
      const possibleFields = ['ShippingAddresses', 'shippingAddresses', 'shipping_addresses', 'addresses'];
      let shippingAddresses: any[] = [];
      
      for (const field of possibleFields) {
        if (data[field] && Array.isArray(data[field])) {
          shippingAddresses = data[field];
          console.log(`Found addresses in field '${field}':`, shippingAddresses);
          break;
        }
      }
      
      if (shippingAddresses.length === 0) {
        console.log('No shipping addresses found in any field');
        return [];
      }
      
      return shippingAddresses.map((addr: any, index: number) => ({
        id: addr['id'] || index.toString(),
        Address: addr['Address'] || '',
        Name: addr['Name'] || '',
        Phone: addr['Phone'] || '',
        IsDefault: addr['IsDefault'] || false
      })) as ShippingAddress[];
    } else {
      console.log('Customer document not found');
      return [];
    }
  }).catch(error => {
    console.error('Error fetching customer document:', error);
    return [];
  });
}

// Method to delete a shipping address
deleteShippingAddress(customerId: string, addressId: string): Observable<void> {
  console.log('Deleting shipping address:', { customerId, addressId });
  
  const addressDocRef = doc(db, 'Customers', customerId, 'ShippingAddresses', addressId);
  
  return from(deleteDoc(addressDocRef)).pipe(
    catchError(error => {
      console.error('Error deleting shipping address:', error);
      return throwError(() => error);
    })
  );
}

// Method to set default address
setDefaultAddress(customerId: string, addressId: string): Observable<void> {
  console.log('Setting default address:', { customerId, addressId });
  
  // First, get all addresses and update them
  const shippingAddressesRef = collection(db, 'Customers', customerId, 'ShippingAddresses');
  
  const updatePromise = getDocs(shippingAddressesRef).then(async (snapshot) => {
    const batch = [];
    
    // Update all addresses to set IsDefault = false/true
    for (const docSnap of snapshot.docs) {
      const addressDocRef = doc(db, 'Customers', customerId, 'ShippingAddresses', docSnap.id);
      const isDefault = docSnap.id === addressId;
      batch.push(updateDoc(addressDocRef, { IsDefault: isDefault }));
    }
    
    // Execute all updates
    await Promise.all(batch);
  }).catch(error => {
    console.error('Error setting default address:', error);
    throw error;
  });

  return from(updatePromise);
}

// Alternative method if your data structure is different
getShippingAddressesAlternative(customerId: string): Observable<ShippingAddress[]> {
  console.log('Fetching shipping addresses (alternative method) for customer:', customerId);
  
  // If shipping addresses are stored as an array field in the customer document
  const customerDocRef = doc(db, 'Customers', customerId);
  
  const addressesPromise = getDoc(customerDocRef).then(docSnap => {
    if (docSnap.exists()) {
      const data = docSnap.data() as Record<string, any>;
      const shippingAddresses = data['ShippingAddresses'] || data['shippingAddresses'] || [];
      
      console.log('Shipping addresses from customer document:', shippingAddresses);
      return shippingAddresses.map((addr: any, index: number) => ({
        id: addr['id'] || index.toString(),
        Address: addr['Address'] || '',
        Name: addr['Name'] || '',
        Phone: addr['Phone'] || '',
        IsDefault: addr['IsDefault'] || false
      })) as ShippingAddress[];
    } else {
      console.log('Customer document not found');
      return [];
    }
  }).catch(error => {
    console.error('Error fetching shipping addresses (alternative):', error);
    return [];
  });

  return from(addressesPromise);
}

}
