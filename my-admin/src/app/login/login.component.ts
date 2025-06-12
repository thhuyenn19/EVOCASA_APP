import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../services/admin.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  showPassword = false;
  loginError = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private adminService: AdminService
  ) { }

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      employeeId: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginError = 'Please fill in all required fields correctly.';
      return;
    }

    const { employeeId, password } = this.loginForm.value;
    

this.adminService.login(employeeId, password).subscribe({
  next: (admin) => {
    if (admin) {
      this.router.navigate(['/dashboard-page']);
    } else {
      this.loginError = 'Invalid employee ID or password.';
    }
  },
});

  }

logout() {
  this.adminService.logout();
  this.router.navigate(['/login-page']);
}
}