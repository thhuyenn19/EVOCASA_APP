// ../services/admin.service.ts
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Admin } from '../interfaces/admin';
import { collection, getDocs, getFirestore } from 'firebase/firestore';
import { db } from '../firebase-config'; 

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private currentAdminSubject = new BehaviorSubject<{ admin: Admin | null, currentId: string | null }>({ admin: null, currentId: null });
  public currentAdmin$ = this.currentAdminSubject.asObservable();

  constructor() {
    const storedAdmin = localStorage.getItem('currentAdmin');
    if (storedAdmin) {
      const admin = JSON.parse(storedAdmin);
      this.currentAdminSubject.next({ admin, currentId: admin._id });
    }
  }

  getAllAdmins(): Observable<Admin[]> {
    return new Observable<Admin[]>(observer => {
      getDocs(collection(db, 'Admin'))
        .then(snapshot => {
          const admins: Admin[] = [];
          snapshot.forEach(doc => {
            const data = doc.data();
            admins.push({ ...(data as Admin), _id: doc.id });
          });
          console.log('Admins from Firestore:', admins);
          observer.next(admins);
          observer.complete();
        })
        .catch(error => {
          console.error('Firestore error:', error);
          observer.error(error);
        });
    });
  }

  login(employeeId: string, password: string): Observable<{ admin: Admin | null, currentId: string | null }> {
    return this.getAllAdmins().pipe(
      map(admins => {
        const admin = admins.find(a =>
          a.employeeid.toLowerCase() === employeeId.toLowerCase() &&
          a.Password === password
        );
        if (admin) {
          const currentId = admin._id;
          localStorage.setItem('currentAdmin', JSON.stringify(admin));
          this.currentAdminSubject.next({ admin, currentId });
          return { admin, currentId };
        }
        return { admin: null, currentId: null };
      }),
      catchError(() => of({ admin: null, currentId: null }))
    );
  }

  logout(): void {
    localStorage.removeItem('currentAdmin');
    this.currentAdminSubject.next({ admin: null, currentId: null });
  }

  getCurrentAdmin(): { admin: Admin | null, currentId: string | null } {
    return this.currentAdminSubject.value; // Trả về object { admin, currentId }
  }

  isLoggedIn(): boolean {
    return !!this.currentAdminSubject.value.admin;
  }

  validateCurrentAdmin(): Observable<boolean> {
    const { admin } = this.currentAdminSubject.value;
    if (!admin) return of(false);
    return this.getAllAdmins().pipe(
      map(admins => admins.some(a => a._id === admin._id)),
      catchError(() => of(false))
    );
  }
}