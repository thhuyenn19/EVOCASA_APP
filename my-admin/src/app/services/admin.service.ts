import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Admin } from '../interfaces/admin';

import { collection, getDocs, getFirestore } from 'firebase/firestore';
import { db } from '../firebase-config'; // ⚠️ Import db đã khởi tạo

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private currentAdminSubject = new BehaviorSubject<Admin | null>(null);
  public currentAdmin$ = this.currentAdminSubject.asObservable();

  constructor() {
    const storedAdmin = localStorage.getItem('currentAdmin');
    if (storedAdmin) {
      this.currentAdminSubject.next(JSON.parse(storedAdmin));
    }
  }

  // ⚠️ Lấy danh sách Admin từ Firestore thay vì HTTP
  getAllAdmins(): Observable<Admin[]> {
  return new Observable(observer => {
    getDocs(collection(db, 'Admin'))
      .then(snapshot => {
        const admins: Admin[] = [];
        snapshot.forEach(doc => {
          const data = doc.data();
          admins.push({ ...(data as Admin), _id: doc.id });
        });

        console.log('📦 Admins from Firestore:', admins); // ✅ Log dữ liệu toàn bộ
        observer.next(admins);
        observer.complete();
      })
      .catch(error => {
        console.error('❌ Firestore error:', error);
        observer.error(error);
      });
  });
}

  login(employeeId: string, password: string): Observable<Admin | null> {
    return this.getAllAdmins().pipe(
      map(admins => {
        const admin = admins.find(a =>
          a.employeeid.toLowerCase() === employeeId.toLowerCase() &&
          a.Password === password
        );

        if (admin) {
          localStorage.setItem('currentAdmin', JSON.stringify(admin));
          this.currentAdminSubject.next(admin);
          return admin;
        }

        return null;
      }),
      catchError(() => of(null))
    );
  }

  logout(): void {
    localStorage.removeItem('currentAdmin');
    this.currentAdminSubject.next(null);
  }

  getCurrentAdmin(): Admin | null {
    return this.currentAdminSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.currentAdminSubject.value;
  }

  validateCurrentAdmin(): Observable<boolean> {
    const admin = this.getCurrentAdmin();
    if (!admin) return of(false);
    return this.getAllAdmins().pipe(
      map(admins => admins.some(a => a._id === admin._id)),
      catchError(() => of(false))
    );
  }
}
