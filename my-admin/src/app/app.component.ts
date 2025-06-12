import { Component, OnInit } from '@angular/core';
import { AdminService } from './services/admin.service';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  isLoggedIn = false;

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit() {
    if (this.adminService.isLoggedIn()) {
      this.adminService.validateCurrentAdmin().subscribe(isValid => {
        if (!isValid) {
          this.adminService.logout();
          this.router.navigate(['/login-page']);
        } else {
          if (this.router.url === '/' || this.router.url === '/login') {
            this.router.navigate(['/dashboard-page']);
          }
        }
      });
    }

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.checkLoginStatus();

      if (this.isLoggedIn && (this.router.url === '/' || this.router.url === '/login')) {
        this.router.navigate(['/dashboard-page']);
      }
    });

    this.checkLoginStatus();
    

    this.adminService.currentAdmin$.subscribe(admin => {
      this.isLoggedIn = !!admin;
    });
  }

  private checkLoginStatus() {
    this.isLoggedIn = this.adminService.isLoggedIn();
  }
  

  logout() {
    this.adminService.logout();
    this.router.navigate(['/login-page']);
  }
}