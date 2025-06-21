import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';
import { ImageService } from '../../services/image.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-edit-category',
  standalone: false,
  templateUrl: './edit-category.component.html',
  styleUrl: './edit-category.component.css'
})
export class EditCategoryComponent implements OnInit {
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
    private imageService: ImageService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
  const categoryId = this.route.snapshot.paramMap.get('id');
  
  // Load categories trước
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

      // Load category sau khi đã có danh sách categories
      if (categoryId) {
        this.loadCategory(categoryId);
      }
    },
    error: () => (this.errMessage = 'Error loading categories'),
  });
}


  loadCategory(id: string) {
    this.categoryService.getCategory(id).subscribe({
      next: (data) => {
        this.category = data;
        this.previewImage = Array.isArray(this.category.image) ? this.category.image[0] : this.category.image;
        // Xử lý parentCategory để đảm bảo binding đúng
      if (this.category.parentCategory) {
        // Nếu parentCategory là object có $oid
        if (typeof this.category.parentCategory === 'object' && '$oid' in this.category.parentCategory) {
          this.category.parentCategory = this.category.parentCategory.$oid;
        }
      } else {
        this.category.parentCategory = null;
      }
        console.log('Loaded category:', this.category);
      },
      error: () => {
        this.errMessage = 'Error loading category data';
      }
    });
  }

  loadMainCategories() {
    this.categoryService.getMainCategories().subscribe({
      next: (data) => (this.categories = data),
      error: () => (this.errMessage = 'Error loading main categories')
    });
  }

  getParentCategoryName(parentCategoryId: string): string {
    if (!parentCategoryId) return "Root Category";
    const parentCategory = this.categories.find(cat => cat.id === parentCategoryId);
    return parentCategory ? parentCategory.name : "Unknown";
  }

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

  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  removeImage() {
    this.selectedFile = null;
    this.previewImage = null;
  }

  generateSlug(name: string): string {
    return name.toLowerCase().trim().replace(/\s+/g, '-');
  }
    handleUpdateCategory() {
      if (!this.isValidCategory()) {
        alert('Vui lòng điền đầy đủ thông tin danh mục trước khi cập nhật!');
        return;
      }

      if (!this.category.slug) {
        this.category.slug = this.generateSlug(this.category.name);
      }

      this.putCategoryWithImage(this.category, this.selectedFile).subscribe({
        next: () => {
          alert('Cập nhật danh mục thành công!');
          this.selectedFile = null;
          this.previewImage = null;
          this.goBack();
        },
        error: (err) => {
          console.error('Lỗi khi cập nhật danh mục:', err);
          alert('Đã xảy ra lỗi khi cập nhật danh mục!');
        }
      });
    }
  putCategoryWithImage(category: Category, newImageFile: File | null): Observable<void> {
    if (!newImageFile) {
      return this.categoryService.putCategory(category);
    }

    const oldImageUrl = Array.isArray(category.image) ? category.image[0] : category.image;

    return new Observable<void>((observer) => {
      this.imageService.deleteImage(oldImageUrl).subscribe({
        next: () => {
          this.imageService.uploadImage(newImageFile).subscribe({
            next: (downloadUrl) => {
              category.image = [downloadUrl];
              this.categoryService.putCategory(category).subscribe({
                next: () => {
                  observer.next();
                  observer.complete();
                },
                error: (err) => observer.error(err)
              });
            },
            error: (err) => observer.error(err)
          });
        },
        error: () => {
          this.imageService.uploadImage(newImageFile).subscribe({
            next: (downloadUrl) => {
              category.image = [downloadUrl];
              this.categoryService.putCategory(category).subscribe({
                next: () => {
                  observer.next();
                  observer.complete();
                },
                error: (err) => observer.error(err)
              });
            },
            error: (err) => observer.error(err)
          });
        }
      });
    });
  }

  isValidCategory(): boolean {
    return !!(this.category.name && this.category.description);
  }

  goBack() {
    this.router.navigate(['/admin-category']);
  }
}
