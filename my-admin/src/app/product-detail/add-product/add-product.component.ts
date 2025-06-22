import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../interfaces/product';
import { Category } from '../../interfaces/category';
import { CategoryService } from '../../services/category.service';
import { ImageService } from '../../services/image.service';

@Component({
  selector: 'app-add-product',
  standalone: false,
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.css'
})
export class AddProductComponent {
  product = new Product();
  selectedFiles: File[] = [];
  previewImages: string[] = [];
  maxImages: number = 5;
  categories: Category[] = [];
  DimensionType?: string;
  errMessage: string = '';

  constructor(
    private ProductService: ProductService,
    private CategoryService: CategoryService,
    private ImageService: ImageService,
    private router: Router,
    private activateRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.product.category_id = '';
    if (!this.product.Image) {
      this.product.Image = '';
    }
    this.loadAllSubcategories();
  }

  loadAllSubcategories() {
    this.CategoryService.getMainCategories().subscribe({
      next: (mainCategories) => {
        let allSubcategories: Category[] = [];
        mainCategories.forEach((parent) => {
          this.CategoryService.getSubcategories(parent.id).subscribe({
            next: (subcategories) => {
              allSubcategories = [...allSubcategories, ...subcategories];
              this.categories = allSubcategories;
            },
            error: () => console.error(`Error loading subcategories for ${parent.id}`),
          });
        });
      },
      error: () => (this.errMessage = 'Error loading main categories'),
    });
  }

  getCategoryIdByName(categoryName: string): string | null {
    const category = this.categories.find((cat) => cat.name === categoryName);
    return category ? category.id : null;
  }

  onFilesSelected(event: any) {
    const files: FileList = event.target.files;
    const currentImages: string = this.product.Image || '';
    let imageArray: string[] = currentImages ? currentImages.split(',') : [];

    if (files.length + imageArray.length > this.maxImages) {
      alert('Only 5 images allowed.');
      return;
    }

    Array.from(files).forEach((file) => {
      if (!file.type.startsWith('image/')) {
        alert('Only image files are allowed.');
        return;
      }

      this.ImageService.uploadImage(file).subscribe((downloadURL: string) => {
        imageArray.push(downloadURL);
        this.product.Image = JSON.stringify(imageArray);
        console.log('Updated Image:', this.product.Image);
      });
    });
  }

  triggerFileInput() {
    document.getElementById('file-upload')?.click();
  }

  removeImage(index: number) {
    const images = this.imageList;
    if (images.length > index) {
      images.splice(index, 1);
      this.product.Image = JSON.stringify(images);
    }
  }

  get imageList(): string[] {
    try {
      return JSON.parse(this.product.Image || '[]');
    } catch {
      return [];
    }
  }

  postProduct() {
    if (!this.isValidProduct()) {
      alert('Please fill in all required fields.');
      return;
    }

    const cleanProduct = this.sanitizeProductBeforeSave(this.product);
    cleanProduct.Image = JSON.stringify(this.imageList);

    this.ProductService.createProduct(cleanProduct)
      .then(() => {
        alert('Product added successfully!');
        this.resetForm();
        this.router.navigate(['/admin-product']);
      })
      .catch((err) => {
        console.error('Error adding product:', err);
        alert('Failed to add product.');
      });
  }

  sanitizeProductBeforeSave(product: Product): Product {
    const cleaned = new Product(product);

    cleaned.Image = JSON.stringify(this.imageList);

    cleaned.category_id = typeof product.category_id === 'object' && product.category_id !== null
      ? (product.category_id as any).$oid || ''
      : product.category_id || '';

    if (typeof product.Dimension !== 'string' && this.DimensionType === 'string') {
      cleaned.Dimension = JSON.stringify(product.Dimension);
    }

    return cleaned;
  }

  resetForm(): void {
    this.product = new Product();
    this.product.Image = '';
    this.previewImages = [];
  }

  isValidProduct(): boolean {
    return !!(
      this.product.Name &&
      this.product.Price &&
      this.product.category_id &&
      this.product.Description &&
      this.imageList.length > 0
    );
  }

  goBack() {
    this.router.navigate(['/admin-product']);
  }
}
