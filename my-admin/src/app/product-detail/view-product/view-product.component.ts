import { Component, OnInit } from '@angular/core';
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
export class ViewProductComponent implements OnInit {
  product: Product = new Product();
  selectedFiles: File[] = [];
  previewImages: string[] = [];
  maxImages: number = 5;
  categories: Category[] = [];
  errMessage: string = '';
  productId: string = '';

  constructor(
    private productService: ProductService,
    private CategoryService: CategoryService,
    private router: Router,
    private activateRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    // Initialize product properly
    this.loadAllSubcategories();

    this.activateRoute.paramMap.subscribe((param) => {
      const id = param.get('id');
      if (id) {
        this.productId = id;
        this.loadProduct(id);
      }
    });
  }

  // Lấy toàn bộ danh mục con
  loadAllSubcategories() {
    this.CategoryService.getMainCategories().subscribe({
      next: (mainCategories) => {
        let allSubcategories: Category[] = [];

        mainCategories.forEach((parent) => {
          this.CategoryService.getSubcategories(parent.id).subscribe({
            next: (subcategories) => {
              allSubcategories = [...allSubcategories, ...subcategories];
              this.categories = allSubcategories;
              console.log('Loaded subcategories:', this.categories);
            },
            error: () => console.error(`Error loading subcategories for ${parent.id}`)
          });
        });
      },
      error: () => {
        this.errMessage = 'Error loading main categories';
      }
    });
  }

  // Lấy dữ liệu sản phẩm
  loadProduct(identifier: string) {
    this.productService.getProductByIdentifier(identifier).subscribe({
      next: (data: any) => {
        // Fix category_id nếu là object { $oid: "..." }
        const categoryId =
          typeof data.category_id === 'string'
            ? data.category_id
            : data.category_id && '$oid' in data.category_id
              ? data.category_id.$oid
              : '';

        // Initialize product with data, ensuring Image is a JSON string
        this.product = new Product({
          ...data,
          category_id: categoryId,
          Dimension: data.Dimension || '',
          Image: typeof data.Image === 'string' ? data.Image : JSON.stringify(data.Image || [])
        });

        console.log('Loaded product:', this.product);
        if (this.categories.length > 0) {
          const match = this.categories.find(cat => cat.id === this.product.category_id);
          console.log('🔎 Category match found:', !!match, match || 'Không tìm thấy category tương ứng');
        }
      },
      error: (err) => {
        console.error('❌ Error loading product:', err);
        this.errMessage = err.error?.message || 'Error loading product';
      }
    });
  }

  // Chọn ảnh từ file
  onFilesSelected(event: any) {
    if (event.target.files && event.target.files.length) {
      for (let file of event.target.files) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          // Use addImage to append the image URL/data
          this.product.addImage(e.target.result);
        };
        reader.readAsDataURL(file);
      }
    }
  }

  // ✅ Kích hoạt input file
  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  // ✅ Xóa ảnh
  removeImage(index: number) {
    this.product.removeImage(index); // Use removeImage method
  }

  goBack() {
    this.router.navigate(['/admin-product']);
  }

  // Method to parse Image JSON string into array
  getImageArray(): string[] {
    try {
      const parsed = JSON.parse(this.product.Image || '[]');
      return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
      console.error('Error parsing image string:', error);
      return [];
    }
  }
}