import { Component} from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  currentUrl: string = '';
  isDropdownOpen: boolean = false;


  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentUrl = event.urlAfterRedirects;
    });
  }

  isActive(url: string): boolean {
    return this.currentUrl === url;
  }

  // Giữ dropdown mở khi click vào mục trong dropdown
  keepDropdownOpen(event: Event) {
    event.stopPropagation(); // Ngừng sự kiện bubbling
    this.isDropdownOpen = true;
  }

  // Đóng dropdown khi click vào bất kỳ mục "navigate__bar--link" khác
  closeDropdown() {
    this.isDropdownOpen = false;
  }

}
