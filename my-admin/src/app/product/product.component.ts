import { Component, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { IProduct } from '../interfaces/product';
import { Category } from '../interfaces/category';
import { CategoryService } from '../services/category.service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-product',
  standalone: false,
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css'],
})
export class ProductComponent implements OnInit {
  products: IProduct[] = [];
  categories: Category[] = [];
  filterForm: FormGroup;
  filteredProducts: IProduct[] = []; // Danh sách sản phẩm hiển thị trên trang
  displayProducts: IProduct[] = []; // Sản phẩm đã được lọc theo các tiêu chí
  showFilter: boolean = false; // Toggle filter panel
  errorMessage: string = '';
  Math = Math;

  // Phân trang
  currentPage: number = 1;
  itemsPerPage: number = 5; // Số sản phẩm mỗi trang
  totalItems: number = 0; // Tổng số sản phẩm
  totalPages: number = 0; // Tổng số trang
  pageNumbers: number[] = []; // Mảng số trang để hiển thị

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      category: [''],
      minPrice: [''],
      maxPrice: [''],
      minInventory: [''],
      maxInventory: [''],
    });
  }

  ngOnInit(): void {
    this.loadCategories(); // Đảm bảo danh mục được tải trước
    // Subscribe to all form control changes
    this.filterForm.valueChanges.subscribe((values) => {
      if (
        values.category ||
        values.minPrice ||
        values.maxPrice ||
        values.minInventory ||
        values.maxInventory
      ) {
        this.applyFilterChanges();
      }
    });
  }

  // Load danh sách sản phẩm
  loadProducts(): void {
    this.productService.getProducts().subscribe({
      next: (data: IProduct[]) => {
        this.products = data;
        this.displayProducts = [...this.products]; // Initialize display products with all products
        this.totalItems = this.products.length;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.updatePageNumbers();
        this.mapCategoryNames(); // Ánh xạ tên danh mục sau khi tải sản phẩm
      },
      error: (err) => {
        this.errorMessage = err.message;
        console.error('Error loading products:', err);
      },
    });
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (data: any[]) => {
        console.log('Raw categories data:', data);
        console.log('First category sample:', data[0]);

        // Transform category data with flexible property name handling
        this.categories = data.map((cat) => {
          const formattedCat: Category = {
            _id: cat._id || cat.id || cat._id?.$oid,
            id: cat.id || cat._id || '',
            name: cat.name || cat.Name || 'Unnamed Category',
            description: cat.description || cat.Description || '',
            slug: cat.slug || cat.Slug || '',
            parentCategory: cat.parentCategory || cat.ParentCategory || null,
            image: cat.image || cat.Image || '',
          };

          // Handle ObjectId format if present
          if (
            typeof formattedCat._id === 'object' &&
            formattedCat._id &&
            '$oid' in formattedCat._id
          ) {
            formattedCat._id = formattedCat._id.$oid;
          }

          return formattedCat;
        });

        console.log('Processed categories:', this.categories);
        console.log(
          'Category IDs:',
          this.categories.map((c) => c._id)
        );
        this.loadProducts(); // Load products after categories
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to load categories';
        console.error('Error loading categories:', err);
      },
    });
  }

  mapCategoryNames(): void {
    if (!this.products.length || !this.categories.length) {
      console.warn('No products or categories available for mapping');
      return;
    }
    const categoryMap: { [key: string]: string } = {};
    this.categories.forEach((category) => {
      console.log('Processing category:', category);
      console.log('Category ID type:', typeof category._id);

      const categoryName = category.name || (category as any).Name || 'Unnamed';

      if (category._id) {
        if (typeof category._id === 'string') {
          categoryMap[category._id] = categoryName;
          categoryMap[category._id.toLowerCase()] = categoryName;
        } else if (typeof category._id === 'object') {
          if ('$oid' in category._id) {
            categoryMap[category._id.$oid] = categoryName;
            categoryMap[category._id.$oid.toLowerCase()] = categoryName;
          }
        }

        categoryMap[String(category._id)] = categoryName;
        categoryMap[String(category._id).toLowerCase()] = categoryName;
      }
    });

    console.log('Category Map:', categoryMap);

    if (this.products.length > 0) {
      const firstProduct = this.products[0];
      console.log('First product category_id:', firstProduct.category_id);
      console.log(
        'First product category_id type:',
        typeof firstProduct.category_id
      );
      console.log(
        'First product category_id stringified:',
        JSON.stringify(firstProduct.category_id)
      );
    }

    this.products.forEach((product) => {
      let categoryFound = false;

      if (product.category_id) {
        const categoryIdStr =
          typeof product.category_id === 'string'
            ? product.category_id
            : (product.category_id as any).$oid || String(product.category_id);

        console.log(
          `Looking for category match for product "${product.Name}" with ID: ${categoryIdStr}`
        );

        if (categoryMap[categoryIdStr]) {
          product.category_name = categoryMap[categoryIdStr];
          categoryFound = true;
        } else if (categoryMap[categoryIdStr.toLowerCase()]) {
          product.category_name = categoryMap[categoryIdStr.toLowerCase()];
          categoryFound = true;
        } else {
          const categoryKeys = Object.keys(categoryMap);
          for (const key of categoryKeys) {
            if (key.includes(categoryIdStr) || categoryIdStr.includes(key)) {
              product.category_name = categoryMap[key];
              categoryFound = true;
              break;
            }
          }
        }
      }

      if (!categoryFound) {
        console.warn(
          `Category not found for product "${product.Name}" with ID: ${product.category_id}`
        );
        product.category_name = 'Unknown';
      }
    });

    this.processProductImages();
    this.displayProducts = [...this.products];
    this.updateFilteredProducts();
  }

  // Hàm xử lý hình ảnh sản phẩm
  processProductImages(): void {
    this.products.forEach((product) => {
      // Đảm bảo Image là một chuỗi JSON
      if (typeof product.Image === 'string') {
        try {
          const parsed = JSON.parse(product.Image);
          if (!Array.isArray(parsed)) {
            product.Image = JSON.stringify([product.Image]); // Convert single string to JSON array
          }
        } catch (e) {
          console.error(`Error parsing image for product ${product.Name}:`, e);
          product.Image = JSON.stringify([product.Image as string]); // Fallback to single-item array
        }
      } else if (!product.Image) {
        product.Image = JSON.stringify([]); // Set empty array as JSON string
      }
    });
  }

  /**
   * Helper: return first image path (or placeholder) for table display
   */
  getProductImageForTable(product: IProduct): string {
    try {
      let imgArray: string[] = [];
      const rawImage: any = (product as any).Image;

      if (typeof rawImage === 'string' && rawImage.trim().startsWith('[')) {
        imgArray = JSON.parse(rawImage);
      } else if (Array.isArray(rawImage)) {
        imgArray = rawImage;
      }

      if (imgArray.length > 0) {
        return imgArray[0];
      }
    } catch (e) {
      console.error('Error getting image for product', product.Name, e);
    }

    return 'assets/images/product-placeholder.png';
  }

  // Cập nhật danh sách sản phẩm hiển thị theo trang
  updateFilteredProducts(): void {
    this.totalItems = this.displayProducts.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);

    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = 1;
    }

    this.updatePageNumbers();

    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;

    this.filteredProducts = this.displayProducts.slice(startIndex, endIndex);

    console.log(
      `Showing products ${startIndex + 1} to ${Math.min(
        endIndex,
        this.totalItems
      )} of ${this.totalItems}`
    );
  }

  updatePageNumbers(): void {
    this.pageNumbers = [];

    if (this.totalPages <= 6) {
      for (let i = 1; i <= this.totalPages; i++) {
        this.pageNumbers.push(i);
      }
    } else {
      if (this.currentPage <= 3) {
        this.pageNumbers = [1, 2, 3, 4, 5, 6];
      } else if (this.currentPage >= this.totalPages - 2) {
        this.pageNumbers = [
          this.totalPages - 5,
          this.totalPages - 4,
          this.totalPages - 3,
          this.totalPages - 2,
          this.totalPages - 1,
          this.totalPages,
        ];
      } else {
        this.pageNumbers = [
          this.currentPage - 2,
          this.currentPage - 1,
          this.currentPage,
          this.currentPage + 1,
          this.currentPage + 2,
          this.currentPage + 3,
        ];
      }
    }

    console.log('Page numbers updated:', this.pageNumbers);
  }

  // Chuyển đến trang cụ thể
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updateFilteredProducts();
    }
  }

  // Trang trước
  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updateFilteredProducts();
    }
  }

  // Trang sau
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updateFilteredProducts();
    }
  }

  addProduct(): void {
    this.router.navigate(['/admin-product-add']);
  }

  viewProduct(product: IProduct): void {
    let identifier: string = '';

    if (product?._id) {
      if (typeof product._id === 'string') {
        identifier = product._id;
      } else if (typeof product._id === 'object') {
        const idObj: any = product._id;
        identifier = idObj.$oid || idObj.oid || idObj._id || '';

        if (!identifier) {
          try {
            identifier = JSON.stringify(idObj);
          } catch (_) {
            identifier = String(idObj);
          }
        }
      }
    }

    if (!identifier && (product as any).id) {
      identifier = (product as any).id;
    }

    if (!identifier) {
      console.warn(
        'Can not navigate to product view page – missing identifier',
        product
      );
      return;
    }

    this.router.navigate(['/admin-product-view', identifier]);
  }

  editProduct(product: IProduct): void {
    let identifier: string | undefined;

    if (product?._id) {
      identifier =
        typeof product._id === 'string'
          ? product._id
          : (product._id as any).$oid || String(product._id);
    }

    if (!identifier && (product as any).id) {
      identifier = (product as any).id;
    }

    if (!identifier) {
      console.warn('Missing product identifier, cannot navigate', product);
      return;
    }

    this.router.navigate([`/admin-product-edit/${identifier}`]);
  }

  deleteProduct(identifier: string): void {
    if (
      confirm(`Are you sure you want to delete product with ID ${identifier}?`)
    ) {
      this.productService.deleteProduct(identifier).subscribe({
        next: () => {
          this.products = this.products.filter((p) => p._id !== identifier);
          this.totalItems = this.products.length;
          this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
          this.updatePageNumbers();
          this.updateFilteredProducts();
          alert('Product deleted successfully.');
        },
        error: (err) => {
          this.errorMessage = err.message;
          console.error('Error deleting product:', err);
        },
      });
    }
  }

  // Toggle the filter panel
  applyFilter(): void {
    this.showFilter = !this.showFilter;
  }

  // Add this new method to handle immediate category filtering
  onCategoryChange(): void {
    const selectedCategory = this.filterForm.get('category')?.value;
    console.log('Category changed to:', selectedCategory);
    this.applyFilterChanges();
  }

  // Apply the filter criteria
  applyFilterChanges(): void {
    const { category, minPrice, maxPrice, minInventory, maxInventory } =
      this.filterForm.value;

    console.log('Filter values:', {
      category,
      minPrice,
      maxPrice,
      minInventory,
      maxInventory,
    });

    this.displayProducts = this.products.filter((product) => {
      let matchesCategory = true;
      let matchesPrice = true;
      let matchesInventory = true;

      if (category && category !== '') {
        const selectedCategory = this.categories.find(
          (cat) => cat.name === category
        );

        if (selectedCategory) {
          const childCategories = this.categories.filter((cat) => {
            let parentCategoryId: string | null = null;

            if (typeof cat.parentCategory === 'string') {
              parentCategoryId = cat.parentCategory;
            } else if (
              cat.parentCategory &&
              typeof cat.parentCategory === 'object'
            ) {
              const parentObj = cat.parentCategory as any;
              if (parentObj.$oid) {
                parentCategoryId = parentObj.$oid;
              } else if (parentObj._id) {
                parentCategoryId = parentObj._id;
              } else {
                parentCategoryId = String(parentObj);
              }
            }
            return parentCategoryId === String(selectedCategory._id);
          });

          const categoryIds = [
            String(selectedCategory._id),
            ...childCategories.map((cat) => String(cat._id)),
          ];
          const categoryNames = [
            selectedCategory.name,
            ...childCategories.map((cat) => cat.name),
          ];

          console.log('Matching product against categories:', categoryNames);

          matchesCategory =
            categoryIds.includes(String(product.category_id)) ||
            categoryNames.includes(product.category_name ?? '');
        } else {
          matchesCategory = product.category_name === category;
        }
      }

      const productPrice = Number(product.Price);

      if (minPrice !== null && minPrice !== '') {
        const minPriceValue = Number(minPrice);
        console.log(
          `Comparing product ${product.Name} price: ${productPrice} >= ${minPriceValue}`
        );
        matchesPrice = matchesPrice && productPrice >= minPriceValue;
      }

      if (maxPrice !== null && maxPrice !== '') {
        const maxPriceValue = Number(maxPrice);
        console.log(
          `Comparing product ${product.Name} price: ${productPrice} <= ${maxPriceValue}`
        );
        matchesPrice = matchesPrice && productPrice <= maxPriceValue;
      }

      if (minInventory !== null && minInventory !== '') {
        matchesInventory =
          matchesInventory && Number(product.Quantity) >= Number(minInventory);
      }

      if (maxInventory !== null && maxInventory !== '') {
        matchesInventory =
          matchesInventory && Number(product.Quantity) <= Number(maxInventory);
      }

      return matchesCategory && matchesPrice && matchesInventory;
    });

    console.log('Filtered products count:', this.displayProducts.length);

    this.currentPage = 1;
    this.updateFilteredProducts();
  }

  // Reset all filters
  resetFilter(): void {
    this.filterForm.reset();
    this.displayProducts = [...this.products];
    this.currentPage = 1;
    this.updateFilteredProducts();
  }

  // Get the last item index for pagination display
  getLastItemIndex(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.totalItems);
  }

  exportProducts() {
    const productsToExport =
      this.displayProducts.length > 0 ? this.displayProducts : this.products;

    if (!productsToExport || productsToExport.length === 0) {
      alert('No data available for export!');
      return;
    }

    const headers = ['No', 'Product Name', 'Price', 'Category', 'Inventory'];
    const csvRows = productsToExport.map((product, index) => [
      index + 1,
      `"${product.Name}"`,
      product.Price,
      `"${product.category_name || 'N/A'}"`,
      product.Quantity,
    ]);

    const csvContent = [headers, ...csvRows].map((e) => e.join(',')).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'product_list.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
  getProductId(product: IProduct): string {
  if (typeof product._id === 'string') {
    return product._id;
  } else if (product._id && typeof product._id === 'object' && '$oid' in product._id) {
    return product._id.$oid;
  }
  return '';
}
}