import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute} from '@angular/router';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-edit-category',
  standalone: false,
  templateUrl: './edit-category.component.html',
  styleUrl: './edit-category.component.css'
})
export class EditCategoryComponent {
  category: Category = {
    _id: '',
    id: '',
    name: '',
    description: '',
    slug: '',
    parentCategory: null,
    image: ''
  };

  selectedFile: File | null = null;
  previewImage: string | null = null;
  categories: Category[] = [];
  errMessage: string = '';

  constructor(
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // Lấy ID từ URL
    const categoryId = this.route.snapshot.paramMap.get('id');
    if (categoryId) {
      this.loadCategory(categoryId);
    }
    
 // Lấy danh mục cha hợp lệ
this.categoryService.getMainCategories().subscribe({
  next: (data) => {
    // Lọc danh mục cha: Không cho phép danh mục hiện tại là danh mục cha của chính nó
    this.categories = data.filter(cat => cat._id !== categoryId);
    
    // Thêm option "None" để người dùng có thể chọn không có danh mục cha
    this.categories.unshift({
      _id: null,
      name: 'None',
      description: '',
      image: null,
      parentCategory: null
    } as unknown as Category);
    
    // Nếu danh mục hiện tại có parentCategory, đảm bảo dropdown hiển thị đúng giá trị
    // Nếu không có parentCategory, set giá trị mặc định là null (None)
    if (this.category.parentCategory) {
      this.category.parentCategory = this.category.parentCategory;
    } else {
      this.category.parentCategory = null; // Set to null to select "None" option
    }
  },
  error: () => (this.errMessage = 'Error loading categories'),
})}

  /**
   * Lấy thông tin danh mục từ API theo ID
   */
  loadCategory(id: string) {
    this.categoryService.getCategory(id).subscribe({
      next: (data) => {
        this.category = data;

        // Xử lý ảnh xem trước
        this.previewImage = Array.isArray(this.category.image) ? this.category.image[0] : this.category.image;

        console.log('Loaded category:', this.category);
      },
      error: () => {
        this.errMessage = 'Error loading category data';
      }
    });
  }

  /**
   * Lấy danh sách danh mục cha
   */
  loadMainCategories() {
    this.categoryService.getMainCategories().subscribe({
      next: (data) => (this.categories = data),
      error: () => (this.errMessage = 'Error loading main categories')
    });
  }

  getParentCategoryName(parentCategoryId: string): string {
    if (!parentCategoryId) return "Root Category"; // Nếu không có parentCategory, trả về Root
    const parentCategory = this.categories.find(cat => cat.id === parentCategoryId);
    return parentCategory ? parentCategory.name : "Unknown"; // Nếu không tìm thấy, trả về Unknown
}

  /**
   * Xử lý khi chọn file ảnh
   */
  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImage = e.target.result;
      };
      if (this.selectedFile) {
        reader.readAsDataURL(this.selectedFile);
      }
    }
  }

  /**
   * Kích hoạt input file khi nhấn vào nút "+"
   */
  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  /**
   * Xóa ảnh
   */
  removeImage() {
    this.selectedFile = null;
    this.previewImage = null;
  }

  /**
   * Tạo slug tự động từ tên danh mục
   */
  generateSlug(name: string): string {
    return name.toLowerCase().trim().replace(/\s+/g, '-');
  }

  /**
   * Cập nhật danh mục
   */
  putCategory() {
    if (!this.isValidCategory()) {
      alert('Vui lòng điền đầy đủ thông tin danh mục trước khi cập nhật!');
      return;
    }

    if (!this.category.slug) {
      this.category.slug = this.generateSlug(this.category.name);
    }

    if (this.previewImage) {
      this.category.image = this.previewImage;
    }

    // Đảm bảo ID được gửi đi
    const categoryData: Category = {
      _id: this.category._id,
      id: this.category.id,
      name: this.category.name,
      description: this.category.description,
      slug: this.category.slug,
      parentCategory: this.category.parentCategory,
      image: this.category.image
    };

    this.categoryService.putCategory(categoryData).subscribe({
      next: () => {
        alert('Category updated successfully');
        this.goBack();
      },
      error: (err) => {
        console.error('API Error:', err);
        this.errMessage = err.error?.message || 'Error updating category';
        alert(this.errMessage);
      }
    });
  }

  /**
   * Kiểm tra xem các trường bắt buộc đã được điền đầy đủ chưa
   */
  isValidCategory(): boolean {
    return !!(this.category.name && this.category.description);
  }

  /**
   * Quay lại trang danh sách danh mục
   */
  goBack() {
    this.router.navigate(['/admin-category']);
  }
}