import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { collection, getDocs, doc, deleteDoc, query, orderBy, updateDoc, setDoc } from 'firebase/firestore';
import { db } from '../firebase-config';

export interface Voucher {
  id: string; // Firestore document ID
  voucherId: string; // Voucher ID (sử dụng document ID từ Firestore)
  name: string; // Name field từ Firestore
  discountPercent: number; // DiscountPercent field từ Firestore
  expireDate: Date; // ExpireDate field từ Firestore
  category: string; // Category field từ Firestore
  maximumThreshold?: number; // Maximum threshold field từ Firestore
  minimumOrderValue?: number; // Minimum order value field từ Firestore
}

@Injectable({
  providedIn: 'root'
})
export class VoucherService {
  private collectionName = 'Voucher';

  constructor() { }

  /**
   * Lấy tất cả vouchers từ Firestore
   * @returns Promise<Voucher[]>
   */
  async getAllVouchers(): Promise<Voucher[]> {
    try {
      const vouchersCollection = collection(db, this.collectionName);
      const vouchersQuery = query(vouchersCollection, orderBy('Name', 'asc'));
      const querySnapshot = await getDocs(vouchersQuery);
      
      const vouchers: Voucher[] = [];
      
      querySnapshot.forEach((doc) => {
        const data = doc.data();
        
        // Chuyển đổi dữ liệu từ Firestore sang interface Voucher
        const voucher: Voucher = {
          id: doc.id, // Sử dụng document ID từ Firestore
          voucherId: doc.id, // Sử dụng document ID làm voucherId
          name: data['Name'] || '',
          discountPercent: data['DiscountPercent'] || 0,
          expireDate: this.convertFirestoreTimestamp(data['ExpireDate']),
          category: data['Category'] || '',
          maximumThreshold: data['Maximum threshold'] || 0,
          minimumOrderValue: data['Minimum order value'] || 0
        };
        
        vouchers.push(voucher);
      });
      
      console.log('✅ Vouchers loaded from Firestore:', vouchers);
      return vouchers;
      
    } catch (error) {
      console.error('❌ Error loading vouchers from Firestore:', error);
      throw error;
    }
  }

  /**
   * Cập nhật voucher theo ID
   * @param voucherId Document ID của voucher
   * @param voucherData Dữ liệu voucher cần cập nhật
   * @returns Promise<void>
   */
  async updateVoucher(voucherId: string, voucherData: Partial<Voucher>): Promise<void> {
    try {
      const voucherDocRef = doc(db, this.collectionName, voucherId);
      
      // Chuyển đổi dữ liệu từ Voucher interface sang Firestore format
      const firestoreData: any = {};
      
      if (voucherData.name !== undefined) {
        firestoreData['Name'] = voucherData.name;
      }
      if (voucherData.category !== undefined) {
        firestoreData['Category'] = voucherData.category;
      }
      if (voucherData.discountPercent !== undefined) {
        firestoreData['DiscountPercent'] = voucherData.discountPercent;
      }
      if (voucherData.expireDate !== undefined) {
        firestoreData['ExpireDate'] = voucherData.expireDate;
      }
      if (voucherData.maximumThreshold !== undefined) {
        firestoreData['Maximum threshold'] = voucherData.maximumThreshold;
      }
      if (voucherData.minimumOrderValue !== undefined) {
        firestoreData['Minimum order value'] = voucherData.minimumOrderValue;
      }
      
      await updateDoc(voucherDocRef, firestoreData);
      console.log('✅ Voucher updated successfully:', voucherId, firestoreData);
      
    } catch (error) {
      console.error('❌ Error updating voucher:', error);
      throw error;
    }
  }

  /**
   * Xóa voucher theo ID
   * @param voucherId Document ID của voucher
   * @returns Promise<void>
   */
  async deleteVoucher(voucherId: string): Promise<void> {
    try {
      const voucherDocRef = doc(db, this.collectionName, voucherId);
      await deleteDoc(voucherDocRef);
      console.log('✅ Voucher deleted successfully:', voucherId);
    } catch (error) {
      console.error('❌ Error deleting voucher:', error);
      throw error;
    }
  }

  /**
   * Lấy một voucher theo ID
   * @param voucherId Document ID của voucher
   * @returns Promise<Voucher | null>
   */
  async getVoucherById(voucherId: string): Promise<Voucher | null> {
    try {
      const vouchers = await this.getAllVouchers();
      return vouchers.find(voucher => voucher.id === voucherId) || null;
    } catch (error) {
      console.error('❌ Error getting voucher by ID:', error);
      throw error;
    }
  }

  /**
   * Chuyển đổi Firestore Timestamp sang Date
   * @param timestamp Firestore timestamp
   * @returns Date object
   */
  private convertFirestoreTimestamp(timestamp: any): Date {
    if (!timestamp) {
      return new Date();
    }
    
    // Nếu timestamp có method toDate() (Firestore Timestamp)
    if (timestamp.toDate && typeof timestamp.toDate === 'function') {
      return timestamp.toDate();
    }
    
    // Nếu timestamp là string
    if (typeof timestamp === 'string') {
      return new Date(timestamp);
    }
    
    // Nếu timestamp là số (milliseconds)
    if (typeof timestamp === 'number') {
      return new Date(timestamp);
    }
    
    // Nếu timestamp đã là Date object
    if (timestamp instanceof Date) {
      return timestamp;
    }
    
    // Fallback
    return new Date();
  }
}