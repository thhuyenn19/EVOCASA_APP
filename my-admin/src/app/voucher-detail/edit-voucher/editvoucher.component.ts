import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-edit-voucher',
  standalone: false,
  templateUrl: './editvoucher.component.html',
  styleUrls: ['./editvoucher.component.css'],
})
export class EditVoucherComponent implements OnInit {
  voucherId: string = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private location: Location
  ) {}

  ngOnInit() {
    // Get voucher ID from route params
    this.route.params.subscribe((params) => {
      this.voucherId = params['id'];
      // TODO: Load voucher data using this.voucherId
    });
  }

  onSaveDraft() {
    // TODO: Save draft implementation
    console.log('Save draft clicked');
  }

  onSaveChanges() {
    // TODO: Save changes implementation
    console.log('Save changes clicked');
  }

  onCancel() {
    this.location.back();
  }
}
