import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../interfaces/product';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';


@Component({
  selector: 'app-view-product',
  standalone: false,
  templateUrl: './view-product.component.html',
  styleUrl: './view-product.component.css'
})
export class ViewProductComponent {
 product = new Product();
  selectedFiles: File[] = []; // Danh sách file ảnh đã chọn
  previewImages: string[] = []; // Ảnh xem trước
  maxImages: number = 5; // Giới hạn số ảnh tải lên
  categories: Category[] = []; // Danh mục sản phẩm
  errMessage: string = '';
  productId: string = '';

  constructor(
    private productService: ProductService,
    private CategoryService: CategoryService,
    private router: Router,
    private activateRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.product.Image = this.product.Image || []; // Đảm bảo mảng ảnh tồn tại
    this.loadAllSubcategories();
    
    // Lấy ID sản phẩm từ URL và tải dữ liệu sản phẩm
    this.activateRoute.paramMap.subscribe((param) => {
      let id = param.get('id');
      if (id) {
        this.productId = id;
        this.loadProduct(id);
      }
    });
  }

   // Lấy tất cả danh mục cha, sau đó gọi API để lấy danh mục con của từng danh mục cha
loadAllSubcategories() {
  this.CategoryService.getMainCategories().subscribe({
    next: (mainCategories) => {
      if (mainCategories.length > 0) {
        let allSubcategories: Category[] = [];

        // Dùng forEach để lấy danh mục con của từng danh mục cha
        mainCategories.forEach((parent) => {
          this.CategoryService.getSubcategories(parent.id).subscribe({
            next: (subcategories) => {
              allSubcategories = [...allSubcategories, ...subcategories]; // Gom tất cả danh mục con
              this.categories = allSubcategories; // Cập nhật danh mục con hiển thị
            },
            error: () => console.error(`Error loading subcategories for ${parent.id}`)
          });
        });
      }
    },
    error: () => (this.errMessage = 'Error loading main categories')
  });
}

  // Lấy dữ liệu sản phẩm từ API theo ID
  loadProduct(identifier: string) {
    this.productService.getProductByIdentifier(identifier).subscribe({
      next: (data) => {
        this.product = data;
      },
      error: (err) => {
        this.errMessage = err.error?.message || 'Error loading product';
      }
    });
  }

  // Xử lý khi chọn file ảnh
  onFilesSelected(event: any) {
    if (event.target.files && event.target.files.length) {
      for (let file of event.target.files) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.product.Image.push(e.target.result); // Đẩy URL ảnh vào mảng
        };
        reader.readAsDataURL(file);
      }
    }
  }

  // Kích hoạt input file khi nhấn nút "+"
  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  // Xóa ảnh theo index
  removeImage(index: number) {
    if (this.product.Image.length > index) {
      this.product.Image.splice(index, 1);
    }
  }

  goBack() {
    this.router.navigate(['/admin-product']);
  }
}

