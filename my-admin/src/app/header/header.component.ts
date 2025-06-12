import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService } from '../services/admin.service';
import { Admin } from '../interfaces/admin';


@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  currentAdmin: Admin | null = null;

  constructor(
    private adminService: AdminService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.adminService.currentAdmin$.subscribe(admin => {
      this.currentAdmin = admin;
    });
  }
  getLastNameUppercase(): string {
    if (!this.currentAdmin?.FullName) {
      return 'GUEST';
    }
    
    const nameParts = this.currentAdmin.FullName.trim().split(' ');
    const lastName = nameParts[nameParts.length - 1];
    return lastName.toUpperCase();
  }
  logout(): void {
    this.adminService.logout();
    this.router.navigate(['/']);
  }
}
