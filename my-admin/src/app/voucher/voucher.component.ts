import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

interface Voucher {
  id: number; // internal identifier for editing/navigation
  voucherId: string; // public-facing voucher code / ID
  name: string; // voucher name or title
  discountPercent: number; // percentage discount (e.g., 10 for 10%)
  expireDate: Date; // expiry date
  category: string; // campaign/category label
}

@Component({
  selector: 'app-voucher',
  standalone: false,
  templateUrl: './voucher.component.html',
  styleUrls: ['./voucher.component.css'],
})
export class VoucherComponent implements OnInit {
  vouchers: Voucher[] = [];
  filteredVouchers: Voucher[] = [];
  pagedVouchers: Voucher[] = [];
  showFilter: boolean = false;

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalItems: number = 0;
  totalPages: number = 0;
  pageNumbers: number[] = [];

  constructor(private router: Router) {}

  ngOnInit() {
    // 📝 Fake data – replace with real API later
    this.vouchers = [
      {
        id: 1,
        voucherId: 'WELCOME10',
        name: 'Welcome 10% Off',
        discountPercent: 10,
        expireDate: new Date('2024-07-01'),
        category: 'EvoCasa Birthday',
      },
      {
        id: 2,
        voucherId: 'SUMMER20',
        name: 'Summer Sale 20%',
        discountPercent: 20,
        expireDate: new Date('2024-08-15'),
        category: 'Summer Sale',
      },
      {
        id: 3,
        voucherId: 'BLACKFRIDAY50',
        name: 'Black Friday 50%',
        discountPercent: 50,
        expireDate: new Date('2024-12-01'),
        category: 'Black Friday',
      },
    ];

    this.filteredVouchers = [...this.vouchers];
    this.totalItems = this.filteredVouchers.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    this.updatePageNumbers();
    this.updatePagedVouchers();
  }

  deleteVoucher(id: number) {
    this.vouchers = this.vouchers.filter((v) => v.id !== id);
    this.filteredVouchers = this.filteredVouchers.filter((v) => v.id !== id);
    this.totalItems = this.filteredVouchers.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages || 1;
    }
    this.updatePageNumbers();
    this.updatePagedVouchers();
  }

  /* ===== Pagination helpers ===== */
  updatePageNumbers() {
    this.pageNumbers = Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  updatePagedVouchers() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.pagedVouchers = this.filteredVouchers.slice(startIndex, endIndex);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updatePagedVouchers();
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagedVouchers();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagedVouchers();
    }
  }

  /* ===== Placeholder actions ===== */
  toggleFilter() {
    this.showFilter = !this.showFilter;
  }

  addVoucher() {
    this.router.navigate(['/admin-voucher-add']);
  }

  exportVouchers() {
    alert('Export feature coming soon!');
  }

  viewVoucher(voucher: Voucher) {
    alert(`View voucher ${voucher.voucherId}`);
  }

  editVoucher(voucher: Voucher) {
    this.router.navigate(['/admin-voucher-edit', voucher.id]);
  }
}
