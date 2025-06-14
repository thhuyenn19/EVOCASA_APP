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
          // Handle properties that might be capitalized differently
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
    // Create a categoryMap for faster lookups by ID with more variants
    const categoryMap: { [key: string]: string } = {};
    this.categories.forEach((category) => {
      // Debug each category's structure
      console.log('Processing category:', category);
      console.log('Category ID type:', typeof category._id);

      // Check for Name vs name (case sensitivity in property names)
      const categoryName = category.name || (category as any).Name || 'Unnamed';

      if (category._id) {
        // Store the ID in multiple formats
        if (typeof category._id === 'string') {
          categoryMap[category._id] = categoryName;
          // Store lowercase version for case-insensitive matching
          categoryMap[category._id.toLowerCase()] = categoryName;
        } else if (typeof category._id === 'object') {
          if ('$oid' in category._id) {
            categoryMap[category._id.$oid] = categoryName;
            // Store lowercase version
            categoryMap[category._id.$oid.toLowerCase()] = categoryName;
          }
        }

        // Always store stringified version
        categoryMap[String(category._id)] = categoryName;
        categoryMap[String(category._id).toLowerCase()] = categoryName;
      }
    });

    console.log('Category Map:', categoryMap);

    // Check first product's category_id format for debugging
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
        // Try various formats
        const categoryIdStr =
          typeof product.category_id === 'string'
            ? product.category_id
            : (product.category_id as any).$oid || String(product.category_id);

        console.log(
          `Looking for category match for product "${product.Name}" with ID: ${categoryIdStr}`
        );

        // Try direct match
        if (categoryMap[categoryIdStr]) {
          product.category_name = categoryMap[categoryIdStr];
          categoryFound = true;
        }
        // Try lowercase match
        else if (categoryMap[categoryIdStr.toLowerCase()]) {
          product.category_name = categoryMap[categoryIdStr.toLowerCase()];
          categoryFound = true;
        }
        // Try all keys for potential partial matches
        else {
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

    // After mapping categories, update display
    this.processProductImages();
    this.displayProducts = [...this.products]; // Initialize display products
    this.updateFilteredProducts();
  }

  // Hàm xử lý hình ảnh sản phẩm
  processProductImages(): void {
    this.products.forEach((product) => {
      // Đảm bảo Image là một mảng
      if (typeof product.Image === 'string') {
        try {
          product.Image = JSON.parse(product.Image);
        } catch (e) {
          console.error(`Error parsing image for product ${product.Name}:`, e);
          product.Image = [];
        }
      } else if (!Array.isArray(product.Image)) {
        product.Image = [];
      }
    });
  }

  // Cập nhật danh sách sản phẩm hiển thị theo trang
  updateFilteredProducts(): void {
    // Calculate pagination based on displayProducts (filtered or all products)
    this.totalItems = this.displayProducts.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);

    // Ensure current page is valid
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = 1;
    }

    // Update page numbers
    this.updatePageNumbers();

    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;

    // Here's the fix - use displayProducts instead of products
    this.filteredProducts = this.displayProducts.slice(startIndex, endIndex);

    console.log(
      `Showing products ${startIndex + 1} to ${Math.min(
        endIndex,
        this.totalItems
      )} of ${this.totalItems}`
    );
  }

  // Cập nhật mảng số trang
  updatePageNumbers(): void {
    this.pageNumbers = [];
    // Show only 2 pages at a time
    if (this.totalPages <= 2) {
      // If total pages is 2 or less, show all pages
      for (let i = 1; i <= this.totalPages; i++) {
        this.pageNumbers.push(i);
      }
    } else {
      // If current page is 1, show pages 1 and 2
      if (this.currentPage === 1) {
        this.pageNumbers = [1, 2];
      }
      // If current page is the last page, show last two pages
      else if (this.currentPage === this.totalPages) {
        this.pageNumbers = [this.totalPages - 1, this.totalPages];
      }
      // Otherwise show current page and next page
      else {
        this.pageNumbers = [this.currentPage, this.currentPage + 1];
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
    this.router.navigate([`/admin-product-view/${product._id}`]);
  }

  editProduct(product: IProduct): void {
    this.router.navigate([`/admin-product-edit/${product._id}`]);
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
    // Get the selected category value
    const selectedCategory = this.filterForm.get('category')?.value;
    console.log('Category changed to:', selectedCategory);

    // Apply the filter immediately when category changes
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

    // Filter the products based on criteria
    this.displayProducts = this.products.filter((product) => {
      let matchesCategory = true;
      let matchesPrice = true;
      let matchesInventory = true;

      // Category filter - enhanced to handle parent categories
      if (category && category !== '') {
        // Get all child categories of the selected category
        const selectedCategory = this.categories.find(
          (cat) => cat.name === category
        );

        if (selectedCategory) {
          // Find all subcategories that have this category as parent
          const childCategories = this.categories.filter((cat) => {
            // Get the parent category ID regardless of its format
            let parentCategoryId: string | null = null;

            if (typeof cat.parentCategory === 'string') {
              parentCategoryId = cat.parentCategory;
            } else if (
              cat.parentCategory &&
              typeof cat.parentCategory === 'object'
            ) {
              // Handle MongoDB ObjectId format
              const parentObj = cat.parentCategory as any;
              if (parentObj.$oid) {
                parentCategoryId = parentObj.$oid;
              } else if (parentObj._id) {
                parentCategoryId = parentObj._id;
              } else {
                parentCategoryId = String(parentObj);
              }
            }
            // Compare with the selected category ID (ensure both are strings)
            return parentCategoryId === String(selectedCategory._id);
          });

          // Collect all relevant category IDs and names
          const categoryIds = [
            String(selectedCategory._id),
            ...childCategories.map((cat) => String(cat._id)),
          ];

          const categoryNames = [
            selectedCategory.name,
            ...childCategories.map((cat) => cat.name),
          ];

          console.log('Matching product against categories:', categoryNames);

          // Check if product's category matches the selected category or any child category
          matchesCategory =
            categoryIds.includes(String(product.category_id)) ||
            categoryNames.includes(product.category_name ?? '');
        } else {
          // Fallback to exact match if category not found
          matchesCategory = product.category_name === category;
        }
      }

      // Price range filter - Fix the comparison
      const productPrice = Number(product.Price); // Ensure conversion to number

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

      // Inventory range filter
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

    // Reset to first page and update display
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
    // Use displayProducts (filtered products) instead of all products
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
}
