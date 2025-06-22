import { Component } from '@angular/core';
import { Order } from '../interfaces/order';
import { OrderService } from '../services/order.service';
import { Router } from '@angular/router';
import { CustomerService } from '../services/customer.service';
import { Customer } from '../interfaces/customer';

@Component({
  selector: 'app-order',
  standalone: false,
  templateUrl: './order.component.html',
  styleUrl: './order.component.css',
})
export class OrderComponent {
  orders: Order[] = [];
  displayedOrders: Order[] = [];
  totalOrders: number = 0;

  customerNames: { [key: string]: string } = {};

  selectedOrder: Order | null = null;

  currentPage: number = 1;
  itemsPerPage: number = 5;

  constructor(
    private orderService: OrderService,
    private customerService: CustomerService,
    private router: Router
  ) {}

  /** Chuyển mọi kiểu Customer_id thành chuỗi thuần */
  private normalizeId(raw: any): string {
    if (!raw) return '';
    if (typeof raw === 'string') return raw;
    if (typeof raw === 'object') {
      return raw.$oid || raw._id || raw.id || JSON.stringify(raw);
    }
    return String(raw);
  }

  ngOnInit(): void {
    this.loadOrders();
  }
  loadOrders(): void {
    this.orderService.getAllOrders().subscribe(
      (data) => {
        // Chuẩn hoá Customer_id để luôn là string
        this.orders = data.map((o) => ({
          ...o,
          Customer_id: this.normalizeId((o as any).Customer_id),
        }));
        this.totalOrders = this.orders.length;
        this.fetchCustomerNames();
        this.updateDisplayedOrders();
      },
      (error) => {
        console.error('Error fetching orders:', error);
      }
    );
  }
  updateDisplayedOrders(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedOrders = this.orders.slice(startIndex, endIndex);
  }
  // Pagination: change the page
  changePage(page: number): void {
    this.currentPage = page;
    this.updateDisplayedOrders();
  }

  // Calculate the range of customers to display
  get startOrder(): number {
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  get endOrder(): number {
    const end = this.currentPage * this.itemsPerPage;
    return end > this.totalOrders ? this.totalOrders : end;
  }

  get totalPages(): number {
    return Math.ceil(this.totalOrders / this.itemsPerPage);
  }

  // Fetch customer names based on customer_id and store them temporarily
  fetchCustomerNames(): void {
    this.orders.forEach((order) => {
      const cid = order.Customer_id;
      if (!cid) return;

      if (!this.customerNames[cid]) {
        this.customerService.getCustomerById(cid).subscribe(
          (customer: Customer) => {
            this.customerNames[cid] = customer.Name;
          },
          (error) => {
            console.error('Error fetching customer details:', error);
          }
        );
      }
    });
  }
  formatDate(dateField: any): string {
    const raw = dateField?.$date || dateField;
    return new Date(raw).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }

  viewOrderDetails(id: string): void {
    this.orderService.getOrderById(id).subscribe(
      (data) => {
        this.selectedOrder = data; // Store the selected customer's details
      },
      (error) => {
        console.error('Error fetching order details:', error);
      }
    );
    this.router.navigate([`/order-detail/${id}`]);
  }
  exportOrders(): void {
    if (!this.orders || this.orders.length === 0) {
      alert('No data available for export!');
      return;
    }

    const headers = [
      'No',
      'Order ID',
      'Customer Name',
      'Order Date',
      'Total Price',
      'Status',
    ];
    const csvRows = this.orders.map((order, index) => [
      index + 1,
      `"${order._id}"`,
      `"${this.customerNames[order.Customer_id] || 'Unknown'}"`,
      `"${order.OrderDate}"`,
      `"${order.TotalPrice}"`,
      `"${order.Status}"`,
    ]);

    const csvContent = [headers, ...csvRows]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'order_list.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
