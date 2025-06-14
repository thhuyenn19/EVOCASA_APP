import { Component, OnInit } from '@angular/core';
import { Customer } from '../interfaces/customer';
import { CustomerService } from '../services/customer.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-customer',
  standalone: false,
  templateUrl: './customer.component.html',
  styleUrl: './customer.component.css'
})
export class CustomerComponent implements OnInit {
  customers: Customer[] = [];
  displayedCustomers: Customer[] = [];
  selectedCustomer: Customer | null = null;
  currentPage: number = 1;
  itemsPerPage: number = 4;
  totalCustomers: number = 0;


  constructor(private customerService: CustomerService,  private router: Router) {}

  ngOnInit(): void {
    this.loadCustomers();
  }
  loadCustomers(): void {
    this.customerService.getAllCustomers().subscribe(
      (data) => {
        this.customers = data;
        this.totalCustomers = data.length;
        this.updateDisplayedCustomers();
      },
      (error) => {
        console.error('Error fetching customers:', error);
      }
    );
  }

  updateDisplayedCustomers(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedCustomers = this.customers.slice(startIndex, endIndex);
  }

  viewCustomerDetails(id: string): void {
    this.customerService.getCustomerById(id).subscribe(
      (data) => {
        this.selectedCustomer = data; // Store the selected customer's details
      },
      (error) => {
        console.error('Error fetching customer details:', error);
      }
    );
    this.router.navigate([`/customer-detail/${id}`]);
  }

  // Pagination: change the page
  changePage(page: number): void {
    this.currentPage = page;
    this.updateDisplayedCustomers();
  }

  // Calculate the range of customers to display
  get startCustomer(): number {
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  get endCustomer(): number {
    const end = this.currentPage * this.itemsPerPage;
    return end > this.totalCustomers ? this.totalCustomers : end;
  }

  get totalPages(): number {
    return Math.ceil(this.totalCustomers / this.itemsPerPage);
  }

  exportCustomers(): void {
    const customersToExport = this.customers;
  
    if (!customersToExport || customersToExport.length === 0) {
      alert('No data available for export!');
      return;
    }
  
    const headers = ['No', 'Customer ID', 'Name', 'Gender', 'Email', 'Phone'];
    const csvRows = customersToExport.map((customer, index) => [
      index + 1,
      `"${customer._id}"`,
      `"${customer.Name}"`,
      `"${customer.Gender}"`,
      `"${customer.Mail}"`,
      `"${customer.Phone}"`
    ]);
  
    const csvContent = [headers, ...csvRows].map(row => row.join(',')).join('\n');
  
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'customer_list.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

}
  // Thực hiện các thao tác như xóa hoặc chỉnh sửa khách hàng
  // editCustomer(id: string) {
  //   console.log('Editing customer with ID:', id);
  // }

  // deleteCustomer(id: string) {
  //   console.log('Deleting customer with ID:', id);
  //   this.customers = this.customers.filter(customer => customer.id !== id);
  // }

