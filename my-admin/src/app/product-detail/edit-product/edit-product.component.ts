import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../interfaces/product';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';
import { ImageService } from '../../services/image.service'; // ✅ import thêm nếu chưa có

@Component({
  selector: 'app-edit-product',
  standalone: false,
  templateUrl: './edit-product.component.html',
  styleUrl: './edit-product.component.css'
})
export class EditProductComponent {
  product = new Product();
  maxImages: number = 5;
  categories: Category[] = [];
  errMessage: string = '';
  productId: string = '';

  constructor(
    private productService: ProductService,
    private CategoryService: CategoryService,
    private ImageService: ImageService, // ✅ inject đúng service
    private router: Router,
    private activateRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.loadAllSubcategories();
    this.activateRoute.paramMap.subscribe((param) => {
      let id = param.get('id');
      if (id) {
        this.productId = id;
        this.loadProduct(id);
      }
    });
  }

  // ✅ Xử lý chọn file ảnh và upload lên server
  onFilesSelected(event: any) {
    const files: FileList = event.target.files;
    const currentImages: string[] = this.product.getImageArray();
    const totalImages = currentImages.length + files.length;

    if (totalImages > this.maxImages) {
      alert('Chỉ được tải tối đa 5 ảnh.');
      return;
    }

    Array.from(files).forEach((file) => {
      if (!file.type.startsWith('image/')) {
        alert('Chỉ được chọn ảnh.');
        return;
      }

      this.ImageService.uploadImage(file).subscribe((downloadURL: string) => {
        currentImages.push(downloadURL);
        this.product.setImageArray(currentImages); // ⬅ cập nhật lại Image
      });
    });
  }

  // ✅ Hiển thị danh sách ảnh đã chọn
  get imageList(): string[] {
    return this.product.getImageArray();
  }

  // ✅ Xóa ảnh theo index
  removeImage(index: number) {
    const images = this.product.getImageArray();
    if (index >= 0 && index < images.length) {
      images.splice(index, 1);
      this.product.setImageArray(images);
    }
  }

  // ✅ Trigger input file
  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  // ✅ Load danh mục con
  loadAllSubcategories() {
    this.CategoryService.getMainCategories().subscribe({
      next: (mainCategories) => {
        if (mainCategories.length > 0) {
          let allSubcategories: Category[] = [];

          mainCategories.forEach((parent) => {
            this.CategoryService.getSubcategories(parent.id).subscribe({
              next: (subcategories) => {
                allSubcategories = [...allSubcategories, ...subcategories];
                this.categories = allSubcategories;
              },
              error: () => console.error(`Lỗi khi tải danh mục con của ${parent.id}`)
            });
          });
        }
      },
      error: () => (this.errMessage = 'Lỗi khi tải danh mục cha')
    });
  }

  // ✅ Load sản phẩm từ ID
  loadProduct(identifier: string) {
    this.productService.getProductByIdentifier(identifier).subscribe({
      next: (data) => {
        this.product = Object.assign(new Product(), data);
      },
      error: (err) => {
        this.errMessage = err.error?.message || 'Không thể tải sản phẩm';
      }
    });
  }

  // ✅ Cập nhật sản phẩm (PUT)
  updateProduct() {
    // Ép Image thành chuỗi nếu cần
    this.product.Image = JSON.stringify(this.product.getImageArray());

    this.productService.updateProduct(this.productId, this.product).subscribe({
      next: () => {
        alert('Cập nhật sản phẩm thành công');
        this.goBack();
      },
      error: (err) => {
        this.errMessage = err.error?.message || 'Lỗi khi cập nhật sản phẩm';
      }
    });
  }

  goBack() {
    this.router.navigate(['/admin-product']);
  }
}
