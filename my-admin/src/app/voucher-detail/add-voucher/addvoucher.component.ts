import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { VoucherService, Voucher } from '../../services/voucher.service';

@Component({
  selector: 'app-add-voucher',
  standalone: false,
  templateUrl: './addvoucher.component.html',
  styleUrls: ['./addvoucher.component.css'],
})
export class AddVoucherComponent {
  isLoading: boolean = false;

  // Form fields
  voucherName: string = '';
  category: string = '';
  discountPercent: number = 0;
  maximumThreshold: number = 0;
  minimumOrderValue: number = 0;
  expireDate: string = '';
  indefinitely: boolean = false;

  constructor(
    private router: Router,
    private location: Location,
    private voucherService: VoucherService
  ) {}

  /**
   * Validate form data
   */
  private validateForm(): boolean {
    if (!this.voucherName.trim()) {
      alert('Please enter voucher name');
      return false;
    }

    if (!this.category.trim()) {
      alert('Please enter category');
      return false;
    }

    if (this.discountPercent <= 0 || this.discountPercent > 100) {
      alert('Please enter valid discount percent (1-100)');
      return false;
    }

    if (!this.indefinitely && !this.expireDate) {
      alert('Please select expire date or check indefinitely');
      return false;
    }

    if (this.maximumThreshold < 0) {
      alert('Maximum threshold cannot be negative');
      return false;
    }

    if (this.minimumOrderValue < 0) {
      alert('Minimum order value cannot be negative');
      return false;
    }

    return true;
  }

  /**
   * Convert input date string to Date object
   */
  private parseInputDate(dateString: string): Date {
    if (!dateString) return new Date();
    const date = new Date(dateString);
    date.setHours(23, 59, 59, 999); // End of day
    return date;
  }

  /**
   * Handle indefinitely checkbox change
   */
  onIndefinitelyChange() {
    if (this.indefinitely) {
      this.expireDate = '';
    }
  }

  /**
   * Handle expire date change
   */
  onExpireDateChange() {
    if (this.expireDate) {
      this.indefinitely = false;
    }
  }

  /**
   * Handle Add Voucher
   */
  async onAddVoucher() {
    if (!this.validateForm()) {
      return;
    }

    const confirmed = confirm('Are you sure you want to add this voucher?');
    if (!confirmed) {
      return;
    }

    try {
      this.isLoading = true;

      console.log('📤 Preparing to create voucher...');

      const newVoucher: Partial<Voucher> = {
        name: this.voucherName.trim(),
        category: this.category.trim(),
        discountPercent: Number(this.discountPercent),
        maximumThreshold: Number(this.maximumThreshold),
        minimumOrderValue: Number(this.minimumOrderValue),
        expireDate: this.indefinitely
          ? new Date(2099, 11, 31, 23, 59, 59, 999)
          : this.parseInputDate(this.expireDate),
      };

      console.log('📤 Sending create data:', newVoucher);

      // Call createVoucher, which returns the created document ID
      const createdVoucherId = await this.voucherService.createVoucher(newVoucher);

      console.log('✅ Voucher created successfully with ID:', createdVoucherId);
      alert(`Voucher created successfully! ID: ${createdVoucherId}`);

      // Navigate back or to voucher detail page
      this.location.back();

    } catch (error) {
      console.error('❌ Error creating voucher:', error);

      let errorMessage = 'Error creating voucher. Please try again.';
      if (error instanceof Error) {
        errorMessage = `Error: ${error.message}`;
      }

      alert(errorMessage);
    } finally {
      this.isLoading = false;
    }
  }

  onCancel() {
    const confirmed = confirm('Are you sure you want to cancel adding voucher?');
    if (!confirmed) return;

    this.location.back();
  }
}
