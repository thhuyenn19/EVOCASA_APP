import { Component, OnInit } from '@angular/core';
import { Router} from '@angular/router';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-add-category',
  standalone: false,
  templateUrl: './add-category.component.html',
  styleUrl: './add-category.component.css'
})
export class AddCategoryComponent {
  category: Category = {
    _id: '', // Để server tự động tạo ID
    name: '',
    description: '',
    slug: '',
    parentCategory: null,
    image: [],
    id: ''
  }; // Đối tượng category mới
  selectedFile: File | null = null; // Chỉ chọn 1 ảnh duy nhất
  previewImage: string | null = null; // Ảnh xem trước
  categories: Category[] = []; // Lưu danh sách category từ API
  errMessage: string = ''; // Biến lưu trữ thông báo lỗi

  constructor(
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit() {
    this.category.parentCategory = "";
    this.loadMainCategories();
  }

  // Lấy danh sách danh mục cha
  loadMainCategories() {
    this.categoryService.getMainCategories().subscribe({
      next: (data) => (this.categories = data),
      error: () => (this.errMessage = 'Error loading main categories')
    });
  }

  // Xử lý khi chọn file ảnh
  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0]; // Chỉ lấy file đầu tiên
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImage = e.target.result; // Hiển thị ảnh xem trước
      };
      if (this.selectedFile) {
        reader.readAsDataURL(this.selectedFile);
      }
    }
  }

  // Kích hoạt input file khi nhấn vào dấu "+"
  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  // Xóa ảnh
  removeImage() {
    this.selectedFile = null;
    this.previewImage = null;
  }

  // Tạo slug tự động
  generateSlug(name: string): string {
    return name.toLowerCase().trim().replace(/\s+/g, '-');
  }

  postCategory() {
    if (!this.isValidCategory()) {
      alert('Vui lòng điền đầy đủ thông tin danh mục trước khi thêm!');
      return;
    }
  
    if (!this.category.slug) {
      this.category.slug = this.generateSlug(this.category.name);
    }
  
    if (this.previewImage) {
      this.category.image = [this.previewImage];
    }
  
    // Tạo một bản sao hợp lệ của category và loại bỏ `_id`, `id`
    const categoryData: Partial<Category> = {
      name: this.category.name,
      description: this.category.description,
      slug: this.category.slug,
      parentCategory: this.category.parentCategory,
      image: this.category.image,
    };
  
    this.categoryService.createCategory(categoryData as Category).subscribe({
      next: () => {
        alert('Category added successfully');
        this.goBack();
      },
      error: (err) => {
        console.error('API Error:', err);
        this.errMessage = err.error?.message || 'Error adding category';
        alert(this.errMessage);
      }
    });
  }

  // Kiểm tra xem các trường bắt buộc đã được điền đầy đủ chưa
  isValidCategory(): boolean {
    return !!(
      this.category.name && 
      this.category.description &&
      this.category.parentCategory // Danh mục cha bắt buộc
    );
  }

  // Quay lại trang danh sách danh mục
  goBack() {
    this.router.navigate(['/admin-category']);
  }
}