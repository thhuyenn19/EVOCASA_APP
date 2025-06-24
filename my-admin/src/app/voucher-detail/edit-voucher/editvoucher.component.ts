import { Component } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-edit-voucher',
  standalone: false,
  templateUrl: './editvoucher.component.html',
  styleUrls: ['./editvoucher.component.css'],
})
export class EditVoucherComponent {
  constructor(private location: Location) {}

  goBack() {
    this.location.back();
  }
}
