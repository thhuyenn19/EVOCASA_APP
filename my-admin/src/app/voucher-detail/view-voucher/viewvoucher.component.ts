import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { VoucherService, Voucher } from '../../services/voucher.service';

@Component({
  selector: 'app-view-voucher',
  standalone: true,
  imports: [CommonModule, FormsModule], // ✅ thêm FormsModule
  templateUrl: './viewvoucher.component.html',
  styleUrls: ['./viewvoucher.component.css'],
})
export class ViewVoucherComponent implements OnInit {
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
    private route: ActivatedRoute,
    private location: Location,
    private voucherService: VoucherService
  ) {}

  async ngOnInit() {
    this.route.params.subscribe(async (params) => {
      this.voucherId = params['id'];
      if (this.voucherId) {
        await this.loadVoucherData();
      }
    });
  }

  async loadVoucherData() {
    try {
      this.isLoading = true;
      this.voucher = await this.voucherService.getVoucherById(this.voucherId);

      if (this.voucher) {
        this.voucherName = this.voucher.name;
        this.category = this.voucher.category;
        this.discountPercent = this.voucher.discountPercent;
        this.maximumThreshold = this.voucher.maximumThreshold || 0;
        this.minimumOrderValue = this.voucher.minimumOrderValue || 0;

        if (this.voucher.expireDate) {
          this.expireDate = this.formatDateForInput(this.voucher.expireDate);
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

  private formatDateForInput(date: Date): string {
    if (!date) return '';
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onBack() {
    this.location.back();
  }
}
