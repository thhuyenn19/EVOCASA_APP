import { Component, OnInit } from '@angular/core';
import { Customer } from '../interfaces/customer';
import { CustomerService } from '../services/customer.service';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../services/order.service'; 
import { Order } from '../interfaces/order';

// Interface for Shipping Address
interface ShippingAddress {
  id?: string;
  Address: string;
  Name: string;
  Phone: string;
  IsDefault: boolean;
}

@Component({
  selector: 'app-customer-detail',
  standalone: false,
  templateUrl: './customer-detail.component.html',
  styleUrl: './customer-detail.component.css'
})
export class CustomerDetailComponent implements OnInit {
  customers: Customer[] = [];
  displayedCustomers: Customer[] = [];
  selectedCustomer: Customer | null = null;
  displayedCustomer: Customer | null = null;
  orders: Order[] = []; 
  filteredOrders: any[] = [];
  customerNames: { [key: string]: string } = {};
  currentPage: number = 1;
  itemsPerPage: number = 4; 
  
  // Thêm thuộc tính để điều khiển popup
  showPopup: boolean = false;

  // Thêm thuộc tính cho shipping addresses
  shippingAddresses: ShippingAddress[] = [];
  isLoadingAddresses: boolean = false;

  constructor(private customerService: CustomerService,  private router: Router, private route: ActivatedRoute, private orderService: OrderService) {}

  ngOnInit(): void {
    // this.loadCustomers();
    // this.route.paramMap.subscribe(params => {
    //   const customerId = params.get('id');
    //   if (customerId) {
    //     this.loadCustomerDetails(customerId); 
    //     this.fetchOrders(customerId);
    //   }
    // });
    this.route.paramMap.subscribe(params => {
      const customerId = params.get('id');
      if (customerId) {
        this.loadCustomerDetails(customerId); 
        this.fetchOrders(customerId);
        this.loadShippingAddresses(customerId);
      }
    });
  }

  loadCustomerDetails(customerId: string): void {
    // this.customerService.getCustomerById(customerId).subscribe(
    //   (data) => {
    //     this.selectedCustomer = data;  // Lưu thông tin khách hàng vào biến selectedCustomer
    //   },
    //   (error) => {
    //     console.error('Error fetching customer details:', error);
    //   }
    // );
    this.customerService.getCustomerById(customerId).subscribe({
      next: (data) => {
        this.selectedCustomer = data;
        console.log('Customer details loaded:', this.selectedCustomer);
      },
      error: (error) => {
        console.error('Error fetching customer details:', error);
      }
    });
  }
  
  fetchOrders(customerId: string): void {
    // this.orderService.getOrdersByCustomer(customerId).subscribe(
    //   (response: any) => {
    //     console.log("Raw API response:", response);
    //     if (response && response.success && response.data) {
    //       this.orders = response.data; // Chỉ lấy `data` từ API
    //     } else {
    //       this.orders = [];
    //     }
    //     console.log('Orders after processing:', this.orders);
    //   },
    //   (error) => {
    //     console.error('Lỗi khi lấy đơn hàng:', error);
    //   }
    // );
    this.orderService.getOrdersByCustomer(customerId).subscribe({
      next: (orders) => {
        this.orders = orders;
        console.log('Orders loaded:', this.orders);
        this.updateDisplayedOrders();
      },
      error: (error) => {
        console.error('Lỗi khi lấy đơn hàng:', error);
        this.orders = [];
      }
    });
  }

  // Thêm hàm để load shipping addresses từ Firestore
  loadShippingAddresses(customerId: string): void {
    this.isLoadingAddresses = true;
    this.customerService.getShippingAddresses(customerId).subscribe({
      next: (addresses) => {
        this.shippingAddresses = addresses.sort((a, b) => {
          // Sắp xếp địa chỉ mặc định lên đầu
          if (a.IsDefault && !b.IsDefault) return -1;
          if (!a.IsDefault && b.IsDefault) return 1;
          return 0;
        });
        console.log('Shipping addresses loaded:', this.shippingAddresses);
        this.isLoadingAddresses = false;
      },
      error: (error) => {
        console.error('Error fetching shipping addresses:', error);
        this.shippingAddresses = [];
        this.isLoadingAddresses = false;
      }
    });
  }

  // Hàm để xóa shipping address - FIXED VERSION
deleteShippingAddress(addressId: string): void {
  if (confirm('Bạn có chắc chắn muốn xóa địa chỉ này?')) {
    // Lấy customerId từ route params
    const customerId = this.route.snapshot.paramMap.get('id');
    
    if (!customerId) {
      console.error('Customer ID not found');
      alert('Không tìm thấy thông tin khách hàng!');
      return;
    }

    // Gọi service với cả customerId và addressId
    this.customerService.deleteShippingAddress(customerId, addressId).subscribe({
      next: () => {
        this.shippingAddresses = this.shippingAddresses.filter(addr => addr.id !== addressId);
        console.log('Address deleted successfully');
      },
      error: (error) => {
        console.error('Error deleting address:', error);
        alert('Có lỗi xảy ra khi xóa địa chỉ!');
      }
    });
  }
}

  // Hàm để đặt địa chỉ mặc định
  setDefaultAddress(addressId: string): void {
    const customerId = this.route.snapshot.paramMap.get('id');
    if (!customerId) return;

    this.customerService.setDefaultAddress(customerId, addressId).subscribe({
      next: () => {
        // Cập nhật local state
        this.shippingAddresses.forEach(addr => {
          addr.IsDefault = addr.id === addressId;
        });
        // Sắp xếp lại để địa chỉ mặc định lên đầu
        this.shippingAddresses.sort((a, b) => {
          if (a.IsDefault && !b.IsDefault) return -1;
          if (!a.IsDefault && b.IsDefault) return 1;
          return 0;
        });
        console.log('Default address updated successfully');
      },
      error: (error) => {
        console.error('Error setting default address:', error);
        alert('Có lỗi xảy ra khi đặt địa chỉ mặc định!');
      }
    });
  }
  
  getTotalAmount(): number {
    return this.orders.reduce((total, order) => total + order.TotalPrice, 0);
  }

  getOrderQuantity(order: Order): number {
    return order.OrderProduct.reduce((total, product) => total + product.Quantity, 0);
  }
  
  get totalPages(): number {
    return Math.ceil(this.orders.length / this.itemsPerPage);
  }

  // Change page based on user input
  changePage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return; // Prevent invalid page number
    }
    this.currentPage = page;
    this.updateDisplayedOrders();
  }

  // Update displayed orders based on current page
  updateDisplayedOrders(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
  }

  // Thêm methods để điều khiển popup
  // openPopup(): void {
  //   this.showPopup = true;
  // }

  // closePopup(): void {
  //   this.showPopup = false;
  // }
  openPopup(): void {
    this.showPopup = true;
    // Load shipping addresses khi mở popup nếu chưa load
    if (this.shippingAddresses.length === 0) {
      const customerId = this.route.snapshot.paramMap.get('id');
      if (customerId) {
        this.loadShippingAddresses(customerId);
      }
    }
  }

  closePopup(): void {
    this.showPopup = false;
  }

  // Method để đóng popup khi click outside
  onOverlayClick(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('popup-overlay')) {
      this.closePopup();
    }
  }

  // Method để format date từ các định dạng khác nhau - IMPROVED VERSION
  formatDate(raw: any): string {
    if (!raw) return 'N/A';

    let dateObj: Date | null = null;

    try {
      // Nếu đã là Date object
      if (raw instanceof Date) {
        dateObj = raw;
      }
      // Firestore Timestamp format { seconds, nanoseconds }
      else if (typeof raw === 'object' && raw.seconds !== undefined && raw.nanoseconds !== undefined) {
        dateObj = new Date(raw.seconds * 1000);
      }
      // MongoDB Export format { $date: '2025-05-26T14:51:00.203Z' }
      else if (typeof raw === 'object' && raw.$date) {
        dateObj = new Date(raw.$date);
      }
      // ISO string format
      else if (typeof raw === 'string') {
        dateObj = new Date(raw);
      }
      // Timestamp number (milliseconds)
      else if (typeof raw === 'number') {
        dateObj = new Date(raw);
      }

      // Kiểm tra nếu date hợp lệ
      if (!dateObj || isNaN(dateObj.getTime())) {
        console.warn('Invalid date format:', raw);
        return 'Invalid Date';
      }

      // Format date theo định dạng Việt Nam
      return dateObj.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    } catch (error) {
      console.error('Error formatting date:', error, raw);
      return 'Error';
    }
  }

  // Alternative format method for different display needs
  formatDateTime(raw: any): string {
    if (!raw) return 'N/A';

    let dateObj: Date | null = null;

    try {
      // Same parsing logic as formatDate
      if (raw instanceof Date) {
        dateObj = raw;
      } else if (typeof raw === 'object' && raw.seconds !== undefined) {
        dateObj = new Date(raw.seconds * 1000);
      } else if (typeof raw === 'object' && raw.$date) {
        dateObj = new Date(raw.$date);
      } else if (typeof raw === 'string') {
        dateObj = new Date(raw);
      } else if (typeof raw === 'number') {
        dateObj = new Date(raw);
      }

      if (!dateObj || isNaN(dateObj.getTime())) {
        return 'Invalid Date';
      }

      // Format with time included
      return dateObj.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      console.error('Error formatting datetime:', error, raw);
      return 'Error';
    }
  }
}