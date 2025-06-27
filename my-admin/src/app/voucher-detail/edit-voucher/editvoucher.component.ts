import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { VoucherService, Voucher } from '../../services/voucher.service';

@Component({
  selector: 'app-edit-voucher',
  standalone: false,
  templateUrl: './editvoucher.component.html',
  styleUrls: ['./editvoucher.component.css'],
})
export class EditVoucherComponent implements OnInit {
  voucherId: string = '';
  voucher: Voucher | null = null;
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
    private route: ActivatedRoute,
    private location: Location,
    private voucherService: VoucherService
  ) {}

  async ngOnInit() {
    // Get voucher ID from route params
    this.route.params.subscribe(async (params) => {
      this.voucherId = params['id'];
      if (this.voucherId) {
        await this.loadVoucherData();
      }
    });
  }

  /**
   * Load voucher data from Firestore
   */
  async loadVoucherData() {
    try {
      this.isLoading = true;
      
      // Sử dụng getVoucherById để lấy trực tiếp voucher theo ID
      this.voucher = await this.voucherService.getVoucherById(this.voucherId);
      
      if (this.voucher) {
        // Populate form fields with voucher data
        this.voucherName = this.voucher.name;
        this.category = this.voucher.category;
        this.discountPercent = this.voucher.discountPercent;
        this.maximumThreshold = this.voucher.maximumThreshold || 0;
        this.minimumOrderValue = this.voucher.minimumOrderValue || 0;
        
        // Convert Date to string format for input[type="date"]
        if (this.voucher.expireDate) {
          this.expireDate = this.formatDateForInput(this.voucher.expireDate);
          // Check if expire date is far in future (indefinitely)
          const farFuture = new Date(2099, 0, 1);
          this.indefinitely = this.voucher.expireDate >= farFuture;
        }
        
        console.log('✅ Voucher data loaded:', this.voucher);
      } else {
        console.error('❌ Voucher not found with ID:', this.voucherId);
        alert('Voucher not found!');
        this.location.back();
      }
      
    } catch (error) {
      console.error('❌ Error loading voucher data:', error);
      alert('Error loading voucher data. Please try again.');
      this.location.back();
    } finally {
      this.isLoading = false;
    }
  }

  /**
   * Format Date object to YYYY-MM-DD string for input[type="date"]
   */
  private formatDateForInput(date: Date): string {
    if (!date) return '';
    
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  }

  /**
   * Convert input date string to Date object
   */
  private parseInputDate(dateString: string): Date {
    if (!dateString) return new Date();
    const date = new Date(dateString);
    // Set time to end of day to avoid timezone issues
    date.setHours(23, 59, 59, 999);
    return date;
  }

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

  async onSaveDraft() {
    // TODO: Implement save draft functionality
    console.log('Save draft clicked');
    alert('Save draft feature will be implemented soon!');
  }

  async onSaveChanges() {
    if (!this.validateForm()) {
      return;
    }

    // Hiển thị confirmation dialog
    const confirmed = confirm('Are you sure you want to save changes to this voucher?');
    if (!confirmed) {
      return;
    }

    try {
      this.isLoading = true;
      
      console.log('🔄 Preparing to save voucher changes...');
      
      // Prepare updated voucher data
      const updatedVoucher: Partial<Voucher> = {
        name: this.voucherName.trim(),
        category: this.category.trim(),
        discountPercent: Number(this.discountPercent),
        maximumThreshold: Number(this.maximumThreshold),
        minimumOrderValue: Number(this.minimumOrderValue),
        expireDate: this.indefinitely 
          ? new Date(2099, 11, 31, 23, 59, 59, 999) // Far future date for indefinitely
          : this.parseInputDate(this.expireDate)
      };
      
      console.log('📤 Sending update data:', updatedVoucher);
      
      // Update voucher in Firestore
      await this.voucherService.updateVoucher(this.voucherId, updatedVoucher);
      
      console.log('✅ Voucher updated successfully');
      alert('Voucher updated successfully!');
      
      // Reload the current voucher data instead of navigating away
      await this.loadVoucherData();
      
    } catch (error) {
      console.error('❌ Error saving voucher:', error);
      
      // Hiển thị error message chi tiết hơn
      let errorMessage = 'Error saving voucher. Please try again.';
      if (error instanceof Error) {
        errorMessage = `Error: ${error.message}`;
      }
      
      alert(errorMessage);
    } finally {
      this.isLoading = false;
    }
  }

  onCancel() {
    const hasChanges = this.hasFormChanges();
    
    if (hasChanges) {
      const confirmed = confirm('You have unsaved changes. Are you sure you want to cancel?');
      if (!confirmed) return;
    }
    
    this.location.back();
  }

  /**
   * Check if form has changes
   */
  private hasFormChanges(): boolean {
    if (!this.voucher) return false;
    
    const currentExpireDate = this.indefinitely 
      ? new Date(2099, 11, 31) 
      : this.parseInputDate(this.expireDate);
    
    return (
      this.voucherName.trim() !== this.voucher.name ||
      this.category.trim() !== this.voucher.category ||
      Number(this.discountPercent) !== this.voucher.discountPercent ||
      Number(this.maximumThreshold) !== (this.voucher.maximumThreshold || 0) ||
      Number(this.minimumOrderValue) !== (this.voucher.minimumOrderValue || 0) ||
      Math.abs(currentExpireDate.getTime() - this.voucher.expireDate.getTime()) > 24 * 60 * 60 * 1000 // Allow 1 day difference
    );
  }
}