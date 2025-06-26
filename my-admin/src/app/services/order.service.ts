import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
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
import { Order } from '../interfaces/order';
import { Customer } from '../interfaces/customer';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  constructor() {}

  /** 🔹 Lấy tất cả đơn hàng */
  getAllOrders(): Observable<Order[]> {
    return new Observable((observer) => {
      getDocs(collection(db, 'Order'))
        .then((snapshot) => {
          const orders: Order[] = [];
          snapshot.forEach((docSnap) => {
            const data = docSnap.data();
            orders.push({ ...(data as Order), _id: docSnap.id });
          });
          observer.next(orders);
          observer.complete();
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Lấy đơn hàng theo ID */
  getOrderById(orderId: string): Observable<Order> {
    return new Observable((observer) => {
      const orderRef = doc(db, 'Order', orderId);
      getDoc(orderRef)
        .then((docSnap) => {
          if (docSnap.exists()) {
            observer.next({ ...(docSnap.data() as Order), _id: docSnap.id });
          } else {
            observer.error(new Error('Order not found'));
          }
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Lấy đơn hàng theo customer ID */
  getOrdersByCustomer(customerId: string): Observable<Order[]> {
    return new Observable((observer) => {
      getDocs(collection(db, 'Order'))
        .then((snapshot) => {
          const orders: Order[] = [];
          snapshot.forEach((docSnap) => {
            const data = docSnap.data();
            const orderCustomerId = data['Customer_id']?.['$oid'];
            if (orderCustomerId === customerId) {
              orders.push({ ...(data as Order), _id: docSnap.id });
            }
          });
          observer.next(orders);
          observer.complete();
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Lấy thông tin customer (dùng cho UI nếu cần) */
  getCustomerById(customerId: string): Observable<Customer> {
    return new Observable((observer) => {
      const customerRef = doc(db, 'Customer', customerId);
      getDoc(customerRef)
        .then((docSnap) => {
          if (docSnap.exists()) {
            observer.next({ ...(docSnap.data() as Customer), _id: docSnap.id });
          } else {
            observer.error(new Error('Customer not found'));
          }
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Tạo đơn hàng mới */
  createOrder(order: Omit<Order, '_id'>): Observable<Order> {
    return new Observable((observer) => {
      addDoc(collection(db, 'Order'), order)
        .then((docRef) => {
          observer.next({ ...order, _id: docRef.id });
          observer.complete();
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Cập nhật đơn hàng */
  updateOrder(
    orderId: string,
    updatedOrder: Partial<Order>
  ): Observable<Order> {
    return new Observable((observer) => {
      const orderRef = doc(db, 'Order', orderId);
      updateDoc(orderRef, updatedOrder)
        .then(() => {
          this.getOrderById(orderId).subscribe({
            next: (updated) => observer.next(updated),
            error: (err) => observer.error(err),
            complete: () => observer.complete(),
          });
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Xóa đơn hàng */
  deleteOrder(orderId: string): Observable<void> {
    return new Observable((observer) => {
      const orderRef = doc(db, 'Order', orderId);
      deleteDoc(orderRef)
        .then(() => {
          observer.next();
          observer.complete();
        })
        .catch((error) => observer.error(error));
    });
  }

  /** 🔹 Cập nhật trạng thái đơn hàng */
  updateOrderStatus(
    orderId: string,
    status:
      | 'Pending'
      | 'Pick Up'
      | 'In Transit'
      | 'Review'
      | 'Cancelled'
      | 'Completed'
  ): Observable<Order> {
    return this.updateOrder(orderId, { Status: status });
  }

  /** 🔹 Thêm sản phẩm vào đơn hàng */
  addProductToOrder(
    orderId: string,
    productId: string,
    quantity: number
  ): Observable<Order> {
    return new Observable((observer) => {
      this.getOrderById(orderId).subscribe({
        next: (order) => {
          const updatedProducts = [...(order.OrderProduct || [])];
          const existingProduct = updatedProducts.find(
            (p) => p._id === productId
          );

          if (existingProduct) {
            existingProduct.Quantity += quantity;
          } else {
            updatedProducts.push({ _id: productId, Quantity: quantity });
          }

          this.updateOrder(orderId, {
            OrderProduct: updatedProducts,
          }).subscribe({
            next: (updated) => observer.next(updated),
            error: (err) => observer.error(err),
            complete: () => observer.complete(),
          });
        },
        error: (err) => observer.error(err),
      });
    });
  }
}
