import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../services/order.service';
import { CustomerService } from '../services/customer.service';
import { ProductService } from '../services/product.service';
import { Order } from '../interfaces/order';
import { Customer } from '../interfaces/customer';
import { IProduct } from '../interfaces/product';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-order-detail',
  standalone: false,
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css',
})
export class OrderDetailComponent implements OnInit {
  orderId: string = '';
  order: Order | null = null;
  customer: Customer | null = null;
  products: { [key: string]: IProduct } = {};
  loading: boolean = true;
  error: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private customerService: CustomerService,
    private productService: ProductService
  ) {}

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.orderId = params['id'];
      if (this.orderId) {
        this.loadOrderDetails();
      }
    });
  }

  loadOrderDetails() {
    this.loading = true;
    this.error = '';

    this.orderService.getOrderById(this.orderId).subscribe({
      next: (orderData) => {
        this.order = orderData;
        if (orderData.Customer_id) {
          this.loadCustomerDetails(orderData.Customer_id);
        }
        this.loadProductDetails(orderData.OrderProduct);
      },
      error: (err) => {
        this.error = 'Không thể tải thông tin đơn hàng. Vui lòng thử lại sau.';
        this.loading = false;
        console.error('Error loading order:', err);
      },
    });
  }

  loadCustomerDetails(customerId: string) {
    this.customerService.getCustomerById(customerId).subscribe({
      next: (customerData) => {
        this.customer = customerData;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không thể tải thông tin khách hàng.';
        this.loading = false;
        console.error('Error loading customer:', err);
      },
    });
  }

  loadProductDetails(orderProducts: { _id: string; Quantity: number }[]) {
    const productObservables = orderProducts.map((item) =>
      this.productService.getProductByIdentifier(item._id)
    );

    forkJoin(productObservables).subscribe({
      next: (products) => {
        products.forEach((product, index) => {
          this.products[orderProducts[index]._id] = product;
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không thể tải thông tin sản phẩm.';
        this.loading = false;
        console.error('Error loading products:', err);
      },
    });
  }

  // Xử lý cập nhật trạng thái đơn hàng MỚI THÊM
  updateOrderStatus(
    newStatus: 'Cancelled' | 'In transit' | 'Delivered' | 'Completed'
  ) {
    if (!this.order) return;

    // Cập nhật trạng thái ngay trên giao diện
    this.order.Status = newStatus;

    // Gọi API để cập nhật trạng thái trên server
    this.orderService.updateOrderStatus(this.orderId, newStatus).subscribe({
      next: (updatedOrder) => {
        console.log('Order status updated successfully:', updatedOrder);
      },
      error: (err) => {
        console.error('Error updating order status:', err);
        alert('Cập nhật trạng thái thất bại! Vui lòng thử lại.');
      },
    });
  }

  // Quay lại trang danh sách đơn hàng
  goBack() {
    this.router.navigate(['/admin-order']);
  }

  // Format date string
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  // Format currency
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  }

  // Calculate total price of all products
  calculateTotalProductPrice(): number {
    if (!this.order?.OrderProduct) return 0;
    return this.order.OrderProduct.reduce((total, item) => {
      const product = this.products[item._id];
      return total + (product?.Price || 0) * item.Quantity;
    }, 0);
  }

  // Calculate final total including delivery fee
  calculateFinalTotal(): number {
    const productTotal = this.calculateTotalProductPrice();
    const deliveryFee = this.order?.DeliveryFee || 0;
    return productTotal + deliveryFee;
  }

  // Get product image safely
  getProductImage(productId: string): string | null {
    const product = this.products[productId];
    if (
      product?.Image &&
      Array.isArray(product.Image) &&
      product.Image.length > 0
    ) {
      return product.Image[0];
    }
    return null;
  }

  // Check if product has image
  hasProductImage(productId: string): boolean {
    return this.getProductImage(productId) !== null;
  }
}
