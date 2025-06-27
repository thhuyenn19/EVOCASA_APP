import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { VoucherService, Voucher } from '../services/voucher.service'; // Import service và interface

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
  isLoading: boolean = false; // Thêm loading state

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalItems: number = 0;
  totalPages: number = 0;
  pageNumbers: number[] = [];

  constructor(
    private router: Router,
    private voucherService: VoucherService // Inject service
  ) {}

  async ngOnInit() {
    await this.loadVouchers();
  }

  /**
   * Load vouchers from Firestore
   */
  async loadVouchers() {
    try {
      this.isLoading = true;
      
      // Lấy dữ liệu từ Firestore thông qua service
      this.vouchers = await this.voucherService.getAllVouchers();
      
      // Cập nhật filtered vouchers và pagination
      this.filteredVouchers = [...this.vouchers];
      this.totalItems = this.filteredVouchers.length;
      this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
      this.updatePageNumbers();
      this.updatePagedVouchers();
      
    } catch (error) {
      console.error('❌ Error loading vouchers:', error);
      alert('Error loading vouchers from Firestore. Please check your connection and try again.');
      
      // Khởi tạo arrays rỗng nếu có lỗi
      this.vouchers = [];
      this.filteredVouchers = [];
      this.pagedVouchers = [];
      this.totalItems = 0;
      this.totalPages = 0;
      this.pageNumbers = [];
      
    } finally {
      this.isLoading = false;
    }
  }

  async deleteVoucher(id: string) {
    try {
      const confirmed = confirm('Are you sure you want to delete this voucher?');
      if (!confirmed) return;

      this.isLoading = true;
      
      // Xóa từ Firestore
      await this.voucherService.deleteVoucher(id);
      
      // Reload lại dữ liệu từ Firestore để đảm bảo đồng bộ
      await this.loadVouchers();
      
      alert('Voucher deleted successfully!');
      
    } catch (error) {
      console.error('❌ Error deleting voucher:', error);
      alert('Error deleting voucher. Please try again.');
    } finally {
      this.isLoading = false;
    }
  }

  /**
   * Refresh data from Firestore
   */
  async refreshData() {
    await this.loadVouchers();
  }

  /* ===== Pagination helpers (giữ nguyên logic cũ) ===== */
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

  /* ===== Placeholder actions (giữ nguyên logic cũ) ===== */
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