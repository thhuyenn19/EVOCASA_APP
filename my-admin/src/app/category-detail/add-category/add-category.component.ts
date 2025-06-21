import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';
import { getDownloadURL, ref, uploadBytes } from 'firebase/storage';
import { storage } from '../../firebase-config';

@Component({
  selector: 'app-add-category',
  standalone: false,
  templateUrl: './add-category.component.html',
  styleUrl: './add-category.component.css'
})
export class AddCategoryComponent implements OnInit {
  category: Category = {
    _id: '',
    name: '',
    description: '',
    slug: '',
    parentCategory: null,
    image: [],
    id: ''
  };
  selectedFile: File | null = null;
  previewImage: string | null = null;
  categories: Category[] = []; // ✅ Fixed: Changed from category to categories
  errMessage: string = '';
  isUploading: boolean = false;
  isLoading: boolean = false; // ✅ Fixed: Added missing isLoading property
  selectedParentId: string | null = null;

  constructor(
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadMainCategories();
  }

  // Lấy danh sách danh mục cha
  loadMainCategories() {
    this.isLoading = true;
    this.categoryService.getMainCategories().subscribe({
      next: (data) => {
        // Fixed: Assign to categories instead of category
        this.categories = data.map(cat => {
          let id = cat._id;
          // Xử lý ObjectId từ MongoDB
          if (typeof id === 'object' && id !== null && '$oid' in id) {
            id = (id as any).$oid;
          }
          let parentCategory = cat.parentCategory;
          if (typeof parentCategory === 'object' && parentCategory !== null && '$oid' in parentCategory) {
            parentCategory = (parentCategory as any).$oid;
          }
          return {
            ...cat,
            _id: id || cat.id || '',
            id: id || cat.id || '',
            parentCategory: parentCategory || null,
            image: Array.isArray(cat.image) ? cat.image : (cat.image ? [cat.image] : [])
          } as Category;
        });

        console.log('Loaded main categories:', this.categories);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading main categories:', error);
        this.errMessage = 'Error loading main categories';
        this.isLoading = false;
      }
    });
  }

  onParentCategoryChange() {
    console.log('selectedParentId changed to:', this.selectedParentId);
    console.log('Type of selectedParentId:', typeof this.selectedParentId);
    
    // Tìm category được chọn để debug
    const selectedCategory = this.categories.find(cat => cat._id === this.selectedParentId);
    console.log('Selected category:', selectedCategory);
    
    // Cập nhật category.parentCategory
    this.category.parentCategory = this.selectedParentId || null;
  }

  // Chọn file ảnh
  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
      if (!this.selectedFile?.type.startsWith('image/')) {
        alert('Vui lòng chọn file hình ảnh!');
        this.selectedFile = null;
        return;
      }
      if (this.selectedFile.size > 5 * 1024 * 1024) {
        alert('Kích thước file không được vượt quá 5MB!');
        this.selectedFile = null;
        return;
      }
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImage = e.target.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  removeImage() {
    this.selectedFile = null;
    this.previewImage = null;
  }

  generateSlug(name: string): string {
    return name.toLowerCase()
      .trim()
      .replace(/[^\w\s-]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-');
  }

  // Upload ảnh lên Firebase Storage
  private async uploadImageToFirebase(file: File): Promise<string> {
    try {
      const timestamp = Date.now();
      const fileName = `categories/${timestamp}_${file.name}`;
      const storageRef = ref(storage, fileName);
      const snapshot = await uploadBytes(storageRef, file);
      const downloadURL = await getDownloadURL(snapshot.ref);
      return downloadURL;
    } catch (error) {
      throw new Error('Lỗi khi upload ảnh. Vui lòng thử lại!');
    }
  }

  async postCategory() {
    if (!this.isValidCategory()) {
      alert('Vui lòng điền đầy đủ thông tin danh mục trước khi thêm!');
      return;
    }

    console.log('Before submit - selectedParentId:', this.selectedParentId);
    console.log('Before submit - category.parentCategory:', this.category.parentCategory);

    this.isUploading = true;
    this.errMessage = '';

    try {
      if (!this.category.slug) {
        this.category.slug = this.generateSlug(this.category.name);
      }

      let imageUrl = '';
      if (this.selectedFile) {
        imageUrl = await this.uploadImageToFirebase(this.selectedFile);
      }

      // Đảm bảo parentCategory được set đúng
      const parentCategoryId = this.selectedParentId === '' ? null : this.selectedParentId;
      
      const categoryData: Partial<Category> = {
        name: this.category.name,
        description: this.category.description,
        slug: this.category.slug,
        parentCategory: parentCategoryId,
        image: imageUrl ? [imageUrl] : [],
      };

      console.log('Sending category data:', categoryData);

      // Gọi API tạo category
      this.categoryService.createCategory(categoryData as Category).subscribe({
        next: () => {
          alert('Category added successfully');
          this.goBack();
        },
        error: (err) => {
          console.error('Error creating category:', err);
          this.errMessage = err.error?.message || 'Error adding category';
          alert(this.errMessage);
          this.isUploading = false;
        }
      });

    } catch (error: any) {
      console.error('Error in postCategory:', error);
      this.errMessage = error.message || 'Có lỗi xảy ra khi thêm category';
      alert(this.errMessage);
      this.isUploading = false;
    }
  }

  isValidCategory(): boolean {
    const isValid = !!(
      this.category.name &&
      this.category.description !== ""
    );
    if (!isValid) {
      console.log('Category validation failed:', {
        name: this.category.name,
        description: this.category.description,
        parentCategory: this.category.parentCategory
      });
    }
    return isValid;
  }

  goBack() {
    this.router.navigate(['/admin-category']);
  }
}