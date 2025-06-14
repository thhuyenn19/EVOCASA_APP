import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../services/category.service';
import { Category } from '../interfaces/category';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';


@Component({
  selector: 'app-category',
  standalone: false,
  templateUrl: './category.component.html',
  styleUrl: './category.component.css',
})
export class CategoryComponent implements OnInit {
  categories: Category[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';
  mainCategories: Category[] = [];
  subCategories: { [key: string]: Category[] } = {};

  paginatedCategories: Category[] = [];
  currentPage: number = 1;
  pageSize: number = 6;
  totalPages: number = 0;
  filterForm: FormGroup; // Form lọc danh mục
  showFilter: boolean = false;

  filteredCategories: Category[] = []; // Sửa lại kiểu dữ liệu
  parentCategories: string[] = []; // Danh mục cha

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      category: [''], // Chỉ lọc theo danh mục cha
    });
  }

  ngOnInit(): void {
    this.fetchCategories();
  }

  

  fetchCategories(): void {
    this.isLoading = true;
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        console.log('Categories fetched successfully:', data);
        this.categories = data;
        
        this.totalPages = Math.ceil(this.categories.length / this.pageSize);
        this.updatePaginatedCategories();
        // Organize into main categories and subcategories
        this.mainCategories = this.categories.filter(cat => cat.parentCategory === null);
        console.log('Main categories:', this.mainCategories);
        
        // Get subcategories for each main category
        this.mainCategories.forEach(mainCat => {
          const mainCatId = this.getCategoryIdAsString(mainCat._id);
          const subs = this.categories.filter(cat => {
            const parentId = this.getCategoryIdAsString(cat.parentCategory);
            return parentId === mainCatId;
          });
          
          if (subs.length > 0) {
            this.subCategories[mainCatId] = subs;
          }
        });
        
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to load categories';
        console.error('Error fetching categories:', err);
        this.isLoading = false;
      }
    });
  }
  updatePaginatedCategories(): void {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = this.currentPage * this.pageSize;
    this.paginatedCategories = this.categories.slice(startIndex, endIndex);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedCategories();
    }
  }

  pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  /**
   * Helper method to convert ID to string format regardless of type
   */
  getCategoryIdAsString(id: string | { $oid: string } | null): string {
    if (!id) return '';
    if (typeof id === 'string') return id;
    if (typeof id === 'object' && '$oid' in id) return id.$oid;
    return '';
  }

  /**
   * Get subcategories for a given parent category
   */
  getSubcategories(parentId: string | { $oid: string }): Category[] {
    const parentIdStr = this.getCategoryIdAsString(parentId);
    return this.subCategories[parentIdStr] || [];
  }

  viewCategory(category: Category): void {
      this.router.navigate([`/admin-category-view/${category.id}`]);
    }

  editCategory(category: Category): void {
      this.router.navigate([`/admin-category-edit/${category.id}`]);
    }

  /**
   * Delete a category
   */
  deleteCategory(category: Category): void {
    if (confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      const categoryId = this.getCategoryIdAsString(category.id);
      
      this.categoryService.deleteCategory(categoryId).subscribe({
        next: () => {
          console.log('Category deleted:', category);
  
          // Cập nhật danh sách ngay lập tức mà không cần reload trang
          this.categories = this.categories.filter(c => this.getCategoryIdAsString(c.id) !== categoryId);
          
          // Cập nhật lại danh sách phân trang
          this.totalPages = Math.ceil(this.categories.length / this.pageSize);
          this.updatePaginatedCategories(); // Cập nhật danh sách hiển thị
  
          this.organizeCategories(); // Cập nhật danh mục cha - con
          alert('Category deleted successfully!');
        },
        error: (err) => {
          console.error('Error deleting category:', err);
          alert('Failed to delete category: ' + (err.message || 'Unknown error'));
        }
      });
    }
  }


  /**
   * Organize categories into main categories and subcategories
   */
  private organizeCategories(): void {
    this.mainCategories = this.categories.filter(cat => cat.parentCategory === null);
    
    this.subCategories = {};
    this.mainCategories.forEach(mainCat => {
      const mainCatId = this.getCategoryIdAsString(mainCat._id);
      const subs = this.categories.filter(cat => {
        const parentId = this.getCategoryIdAsString(cat.parentCategory);
        return parentId === mainCatId;
      });
      
      if (subs.length > 0) {
        this.subCategories[mainCatId] = subs;
      }
    });
  }

  /**
 * Process image paths for categories 
 */
processCategoryImages(): void {
  this.categories.forEach(category => {
    // Handle the uppercase Image property from API
    const imageData = category.image || (category as any).Image;
    
    // Store the processed image data back in the category object
    if (typeof imageData === 'string') {
      category.image = imageData; // Store as is for string paths
    } else if (Array.isArray(imageData)) {
      category.image = imageData; // Keep array format
    } else if (!imageData) {
      category.image = ''; // Empty string if no image
    }
    
    console.log(`Processed image for category ${category.name}:`, category.image);
  });
}

/**
 * Get image URL for display in tables or lists
 */
getCategoryImageForTable(category: Category): string {
  if (!category) return 'assets/images/category-placeholder.jpg';
  
  // Get image from either lowercase or uppercase property
  const imageData = category.image || (category as any).Image;
  
  if (!imageData) {
    return 'assets/images/category-placeholder.jpg';
  }
  
  // Handle string path (like "/images/Furniture/Ambiance Coffee Table1.jpg")
  if (typeof imageData === 'string') {
    return this.getFullImagePath(imageData);
  }
  
  // Handle array of images
  if (Array.isArray(imageData) && imageData.length > 0) {
    return this.getFullImagePath(imageData[0]);
  }
  
  return 'assets/images/category-placeholder.jpg';
}

/**
 * Get full image path with base URL
 */
getFullImagePath(imagePath: string): string {
  if (!imagePath) return 'assets/images/category-placeholder.jpg';
  
  // If it's already a data URL or absolute path, return as is
  if (imagePath.startsWith('data:')) {
    return imagePath;
  }
  
  // Use relative paths to access local images in your project
  // Remove any server-specific path components
  const cleanPath = imagePath.replace(/^\//, ''); // Remove leading slash if present
  return `assets/images/${cleanPath}`;
}


  /**
   * Get the name of the parent category by its ID
   */
  getParentCategoryName(parentCategoryId: string | { $oid: string }): string {
    const parentIdStr = this.getCategoryIdAsString(parentCategoryId);
    const parentCategory = this.categories.find(category => this.getCategoryIdAsString(category.id) === parentIdStr);
    return parentCategory ? parentCategory.name : 'Unknown';
  }

  exportCategories(): void {
    // Xuất tất cả danh mục thay vì chỉ hiển thị trong trang hiện tại
    const categoriesToExport = this.categories;
  
    if (!categoriesToExport || categoriesToExport.length === 0) {
      alert('No data available for export!');
      return;
    }
  
    const headers = ['No', 'Category Name', 'Parent Category', 'Description'];
    const csvRows = categoriesToExport.map((category, index) => [
      index + 1,
      `"${category.name}"`,
      category.parentCategory ? `"${this.getParentCategoryName(category.parentCategory)}"` : 'Root Category',
      `"${category.description}"`,
    ]);
  
    const csvContent = [headers, ...csvRows].map((e) => e.join(',')).join('\n');
  
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'category_list.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}