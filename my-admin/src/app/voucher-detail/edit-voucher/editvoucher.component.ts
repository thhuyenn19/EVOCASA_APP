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
      
      // Lấy tất cả vouchers và tìm voucher theo ID
      const vouchers = await this.voucherService.getAllVouchers();
      this.voucher = vouchers.find(v => v.id === this.voucherId) || null;
      
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
    return new Date(dateString);
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

    try {
      this.isLoading = true;
      
      // Prepare updated voucher data
      const updatedVoucher: Partial<Voucher> = {
        name: this.voucherName.trim(),
        category: this.category.trim(),
        discountPercent: this.discountPercent,
        maximumThreshold: this.maximumThreshold,
        minimumOrderValue: this.minimumOrderValue,
        expireDate: this.indefinitely ? new Date(2099, 11, 31) : this.parseInputDate(this.expireDate)
      };
      
      // TODO: Implement update voucher in service
      // await this.voucherService.updateVoucher(this.voucherId, updatedVoucher);
      
      console.log('Updated voucher data:', updatedVoucher);
      alert('Voucher updated successfully!');
      
      // Navigate back to voucher list
      this.router.navigate(['/admin-voucher']);
      
    } catch (error) {
      console.error('❌ Error saving voucher:', error);
      alert('Error saving voucher. Please try again.');
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
    
    return (
      this.voucherName !== this.voucher.name ||
      this.category !== this.voucher.category ||
      this.discountPercent !== this.voucher.discountPercent ||
      this.maximumThreshold !== (this.voucher.maximumThreshold || 0) ||
      this.minimumOrderValue !== (this.voucher.minimumOrderValue || 0) ||
      this.expireDate !== this.formatDateForInput(this.voucher.expireDate)
    );
  }
}