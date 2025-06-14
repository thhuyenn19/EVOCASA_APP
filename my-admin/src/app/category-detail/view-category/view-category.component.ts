import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-view-category',
  standalone: false,
  templateUrl: './view-category.component.html',
  styleUrl: './view-category.component.css'
})
export class ViewCategoryComponent implements OnInit {
  category: Category = {
    _id: '',
    name: '',
    description: '',
    slug: '',
    parentCategory: null,
    image: [],
    id: ''
  };
  
  previewImage: string | null = null;
  categories: Category[] = [];
  parentCategoryName: string = '';
  errMessage: string = '';
  categoryId: string = '';
  isImageLoading: boolean = true;

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.categoryId = params['id'];
      if (this.categoryId) {
        this.loadCategoryDetails();
        this.loadAllCategories();
      } else {
        this.errMessage = 'Category ID not provided';
        alert(this.errMessage);
        this.goBack();
      }
    });
  }

  loadCategoryDetails() {
    this.isImageLoading = true;
    this.categoryService.getCategory(this.categoryId).subscribe({
      next: (data) => {
        this.category = data;
        console.log('Loaded category details:', this.category);
        
        // Set the preview image from the processed category data
        this.previewImage = Array.isArray(this.category.image) ? this.category.image[0] : this.category.image;
        this.isImageLoading = false;
        
        if (this.category.parentCategory) {
          this.findParentCategoryName();
        }
      },
      error: (err) => {
        console.error('Error loading category details:', err);
        this.errMessage = 'Error loading category details';
        alert(this.errMessage);
        this.isImageLoading = false;
      }
    });
  }

  loadAllCategories() {
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        console.log('Loaded all categories:', this.categories);
        this.findParentCategoryName();
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.errMessage = 'Error loading categories';
      }
    });
  }

  findParentCategoryName() {
    if (this.category.parentCategory && this.categories.length > 0) {
      const parentCategory = this.categories.find(cat => cat._id === this.category.parentCategory);
      if (parentCategory) {
        this.parentCategoryName = parentCategory.name;
        console.log('Found parent category name:', this.parentCategoryName);
      } else {
        console.log('Parent category not found in loaded categories');
      }
    }
  }

  onImageError() {
    // Handle image loading errors
    console.error('Error loading image');
    this.previewImage = 'assets/images/category-placeholder.png';
  }

  goBack() {
    this.router.navigate(['/admin-category']);
  }
}