import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { collection, getDocs, doc, deleteDoc, query, orderBy, updateDoc, setDoc, getDoc } from 'firebase/firestore';
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
      console.log('🔄 Updating voucher with ID:', voucherId);
      console.log('📝 Voucher data to update:', voucherData);

      const voucherDocRef = doc(db, this.collectionName, voucherId);

      // Kiểm tra xem document có tồn tại không
      const docSnapshot = await getDoc(voucherDocRef);
      if (!docSnapshot.exists()) {
        throw new Error(`Voucher with ID ${voucherId} does not exist`);
      }

      // Chuyển đổi dữ liệu sang Firestore field mapping
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
        // Đảm bảo expireDate là Date object hợp lệ
        const expireDate = voucherData.expireDate instanceof Date 
          ? voucherData.expireDate 
          : new Date(voucherData.expireDate);
        firestoreData['ExpireDate'] = expireDate;
      }
      if (voucherData.maximumThreshold !== undefined) {
        firestoreData['Maximum threshold'] = voucherData.maximumThreshold;
      }
      if (voucherData.minimumOrderValue !== undefined) {
        firestoreData['Minimum order value'] = voucherData.minimumOrderValue;
      }

      console.log('🔥 Firestore data to save:', firestoreData);

      // Thực hiện cập nhật
      await updateDoc(voucherDocRef, firestoreData);
      
      console.log('✅ Voucher updated successfully in Firestore');

      // Verify update bằng cách đọc lại document
      const updatedDoc = await getDoc(voucherDocRef);
      if (updatedDoc.exists()) {
        console.log('✅ Verified updated document:', updatedDoc.data());
      }

    } catch (error) {
      console.error('❌ Error updating voucher:', error);
      throw error;
    }
  }

  /**
 * Tạo voucher mới
 * @param voucherData Dữ liệu voucher cần tạo
 * @returns Promise<void>
 */
async createVoucher(voucherData: Partial<Voucher>): Promise<string> {
  try {
    const vouchersCollection = collection(db, this.collectionName);
    const newDocRef = doc(vouchersCollection);

    const firestoreData: any = {
      Name: voucherData.name || '',
      DiscountPercent: voucherData.discountPercent || 0,
      ExpireDate: voucherData.expireDate || new Date(),
      Category: voucherData.category || '',
      'Maximum threshold': voucherData.maximumThreshold || 0,
      'Minimum order value': voucherData.minimumOrderValue || 0,
      voucherId: newDocRef.id // save document ID as voucherId
    };

    await setDoc(newDocRef, firestoreData);

    console.log('✅ Voucher created successfully with ID:', newDocRef.id);
    return newDocRef.id;

  } catch (error) {
    console.error('❌ Error creating voucher:', error);
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
   * Lấy một voucher theo ID trực tiếp từ Firestore
   * @param voucherId Document ID của voucher
   * @returns Promise<Voucher | null>
   */
  async getVoucherById(voucherId: string): Promise<Voucher | null> {
    try {
      const voucherDocRef = doc(db, this.collectionName, voucherId);
      const docSnapshot = await getDoc(voucherDocRef);
      
      if (!docSnapshot.exists()) {
        console.log('❌ Voucher not found with ID:', voucherId);
        return null;
      }

      const data = docSnapshot.data();
      const voucher: Voucher = {
        id: docSnapshot.id,
        voucherId: docSnapshot.id,
        name: data['Name'] || '',
        discountPercent: data['DiscountPercent'] || 0,
        expireDate: this.convertFirestoreTimestamp(data['ExpireDate']),
        category: data['Category'] || '',
        maximumThreshold: data['Maximum threshold'] || 0,
        minimumOrderValue: data['Minimum order value'] || 0
      };

      console.log('✅ Voucher found:', voucher);
      return voucher;

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