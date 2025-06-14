import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import { Admin } from '../interfaces/admin';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private apiUrl = 'http://localhost:3002'; 
  

  private currentAdminSubject = new BehaviorSubject<Admin | null>(null);
  public currentAdmin$ = this.currentAdminSubject.asObservable();

  constructor(private http: HttpClient) {

    const storedAdmin = localStorage.getItem('currentAdmin');
    if (storedAdmin) {
      this.currentAdminSubject.next(JSON.parse(storedAdmin));
    }
  }

  getAllAdmins(): Observable<Admin[]> {
    return this.http.get<Admin[]>(`${this.apiUrl}/admins`)
      .pipe(
        catchError(this.handleError<Admin[]>('getAllAdmins', []))
      );
  }


  getAdminById(id: string): Observable<Admin> {
    return this.http.get<Admin>(`${this.apiUrl}/admins/${id}`)
      .pipe(
        catchError(this.handleError<Admin>('getAdminById'))
      );
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
      catchError(this.handleError<null>('login'))
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

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      return of(result as T);
    };
  }

validateCurrentAdmin(): Observable<boolean> {
  const admin = this.getCurrentAdmin();
  if (!admin) return of(false);
  
  return this.getAdminById(admin._id).pipe(
    map(serverAdmin => !!serverAdmin),
    catchError(() => of(false))
  );
}
}