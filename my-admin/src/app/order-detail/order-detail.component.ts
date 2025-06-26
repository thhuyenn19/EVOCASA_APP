import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../services/order.service';
import { CustomerService } from '../services/customer.service';
import { ProductService } from '../services/product.service';
import { Order } from '../interfaces/order';
import { Customer } from '../interfaces/customer';
import { IProduct } from '../interfaces/product';
import { forkJoin } from 'rxjs';
import { catchError, of } from 'rxjs';

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

  // Lưu địa chỉ giao hàng (nếu có)
  shippingAddress: any = null;

  // Theo dõi thay đổi trạng thái
  originalStatus: string | null = null;
  isDirty: boolean = false;

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

  /**
   * Chuẩn hoá ObjectId/string trả về từ Firestore thành chuỗi thuần
   */
  private normalizeId(raw: any): string {
    if (!raw) return '';
    if (typeof raw === 'string') return raw;
    if (typeof raw === 'object') {
      return raw.$oid || raw._id || raw.id || JSON.stringify(raw);
    }
    return String(raw);
  }

  /**
   * Chuyển raw OrderProduct (array hoặc object) → mảng chuẩn {_id, Quantity}
   */
  private transformOrderProducts(
    raw: any
  ): { _id: string; Quantity: number }[] {
    if (!raw) return [];

    const toItem = (obj: any): { _id: string; Quantity: number } => {
      const idField = obj._id ?? obj.id ?? obj.ProductId ?? obj.Product_id;
      const _id = this.normalizeId(idField);

      let qtyRaw: any = obj.Quantity;
      if (qtyRaw === undefined && obj.Customize) {
        qtyRaw = obj.Customize.Quantity;
      }
      const Quantity = Number(
        qtyRaw?.$numberInt ?? qtyRaw?.$numberDouble ?? qtyRaw ?? 0
      );
      return { _id, Quantity };
    };

    if (Array.isArray(raw)) {
      return raw.map((item) => toItem(item));
    }

    // nếu là object đơn sản phẩm
    if (typeof raw === 'object') {
      // Trường hợp tương tự screenshot: { id: { $oid }, Customize: { Quantity } }
      const merged = { ...(raw.Customize || {}), ...raw };
      return [toItem(merged)];
    }

    return [];
  }

  loadOrderDetails() {
    this.loading = true;
    this.error = '';

    this.orderService.getOrderById(this.orderId).subscribe({
      next: (orderData) => {
        // ✅ Chuẩn hoá danh sách sản phẩm (_id thành chuỗi thuần)
        orderData.OrderProduct = this.transformOrderProducts(
          orderData.OrderProduct
        );

        // Lưu đơn hàng sau khi chuẩn hoá
        this.order = orderData;

        // Lấy thông tin khách hàng (Name, address,...)
        if (orderData.Customer_id) {
          const customerId = this.normalizeId(orderData.Customer_id);
          this.loadCustomerDetails(customerId);

          // Lấy ShippingAddresses
          this.customerService.getShippingAddresses(customerId).subscribe({
            next: (addresses) => {
              if (addresses && addresses.length > 0) {
                const addr = addresses.find((a) => a.IsDefault) || addresses[0];
                this.shippingAddress = addr;

                // Gán vào order để template hiển thị
                if (this.order) {
                  this.order.Address = addr.Address || this.order.Address;
                  this.order.Phone = addr.Phone || this.order.Phone;
                  (this.order as any).Email =
                    (addr as any).Email || this.order.Email;
                }
              }
            },
            error: (err) =>
              console.error('Error loading shipping addresses', err),
          });
        }

        // 🔢 Chuẩn hoá lại Quantity và DeliveryFee (có thể bị bọc số)
        if (this.order) {
          this.order.DeliveryFee = Number(
            (this.order.DeliveryFee as any)?.$numberInt ??
              this.order.DeliveryFee ??
              0
          );

          this.order.OrderProduct = (this.order.OrderProduct || []).map(
            (item: any) => {
              const qtyRaw = item.Quantity;
              const qtyNum = Number(
                qtyRaw?.$numberInt ?? qtyRaw?.$numberDouble ?? qtyRaw ?? 0
              );
              return { ...item, Quantity: qtyNum };
            }
          );
        }

        this.loadProductDetails(this.order?.OrderProduct as any);

        this.originalStatus = orderData.Status;
        this.isDirty = false;
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

  loadProductDetails(orderProducts: any[]) {
    if (!orderProducts || orderProducts.length === 0) {
      this.loading = false;
      return;
    }

    const productIds: string[] = orderProducts.map((item) =>
      this.normalizeId(item._id)
    );

    const productObservables = productIds.map((pid) =>
      this.productService.getProductByIdentifier(pid).pipe(
        catchError((err) => {
          console.warn('Product not found', pid, err);
          return of(null);
        })
      )
    );

    forkJoin(productObservables).subscribe({
      next: (products) => {
        products.forEach((product, index) => {
          if (product) {
            this.products[productIds[index]] = product;
          }
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
    newStatus:
     | 'Pending'
      | 'Pick Up'
      | 'In Transit'
      | 'Review'
      | 'Cancelled'
      | 'Completed'
  ) {
    if (!this.order) return;

    this.order.Status = newStatus;
    this.isDirty = this.order.Status !== this.originalStatus;
  }

  // Xử lý khi nhấn Save
  onSave() {
    if (!this.isDirty || !this.order) {
      alert('Không có thay đổi để lưu.');
      return;
    }

    const agree = confirm('Do you want to save the changes?');
    if (!agree) return;

    this.orderService
      .updateOrderStatus(this.orderId, this.order.Status)
      .subscribe({
        next: (updated) => {
          this.originalStatus = updated.Status;
          this.isDirty = false;
          alert('Lưu thay đổi thành công.');
        },
        error: (err) => {
          console.error('Error saving order:', err);
          alert('Lưu thất bại, vui lòng thử lại.');
        },
      });
  }

  // Quay lại trang danh sách đơn hàng
  goBack() {
    this.router.navigate(['/admin-order']);
  }

  // Format date string
  formatDate(raw: any): string {
    if (!raw) return 'N/A';

    let dateObj: Date | null = null;

    // Nếu đã là Date
    if (raw instanceof Date) {
      dateObj = raw;
    }
    // Firestore Timestamp { seconds, nanoseconds }
    else if (raw.seconds !== undefined && raw.nanoseconds !== undefined) {
      dateObj = new Date(raw.seconds * 1000);
    }
    // Định dạng MongoDB Export { $date: '2025-05-26T14:51:00.203Z' }
    else if (typeof raw === 'object' && raw.$date) {
      dateObj = new Date(raw.$date);
    }
    // Chuỗi ISO
    else if (typeof raw === 'string') {
      dateObj = new Date(raw);
    }

    if (!dateObj || isNaN(dateObj.getTime())) {
      return 'N/A';
    }

    return dateObj.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }

  // Format currency
  formatCurrency(amount: number): string {
  return '$' + amount
    .toFixed(2)                                  // luôn 2 chữ số sau dấu .
    .replace(/\B(?=(\d{3})+(?!\d))/g, ',');     // thêm dấu ngăn hàng nghìn
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
