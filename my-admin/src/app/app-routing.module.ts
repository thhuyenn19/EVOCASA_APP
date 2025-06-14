import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductComponent } from './product/product.component';
import { CategoryComponent } from './category/category.component';
import { OrderComponent } from './order/order.component';
import { CustomerComponent } from './customer/customer.component';
import { CustomerDetailComponent } from './customer-detail/customer-detail.component';
import { OrderDetailComponent } from './order-detail/order-detail.component';
import { AddProductComponent } from './product-detail/add-product/add-product.component';
import { EditProductComponent } from './product-detail/edit-product/edit-product.component';
import { AddCategoryComponent } from './category-detail/add-category/add-category.component';
import { EditCategoryComponent } from './category-detail/edit-category/edit-category.component';
import { LoginComponent } from './login/login.component';
import { AuthGuard } from './guards/auth.guard';
import { ViewProductComponent } from './product-detail/view-product/view-product.component';
import { ViewCategoryComponent } from './category-detail/view-category/view-category.component';

const routes: Routes = [
  { path: 'dashboard-page', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'customer-detail/:id', component: CustomerDetailComponent, canActivate: [AuthGuard]},
  { path: 'order-detail/:id', component: OrderDetailComponent, canActivate: [AuthGuard] },
  { path: 'admin-product-add', component: AddProductComponent, canActivate: [AuthGuard]},
  { path: 'admin-product-edit/:id', component: EditProductComponent, canActivate: [AuthGuard] },
  { path: 'admin-product-view/:id', component: ViewProductComponent, canActivate: [AuthGuard] },
  { path: 'admin-category-add', component: AddCategoryComponent, canActivate: [AuthGuard] },
  { path: 'admin-category-edit/:id', component: EditCategoryComponent, canActivate: [AuthGuard]},
  { path: 'admin-category-view/:id', component: ViewCategoryComponent, canActivate: [AuthGuard]},
  { path: 'admin-customer', component: CustomerComponent, canActivate: [AuthGuard] },
  { path: 'admin-product', component: ProductComponent, canActivate: [AuthGuard] },
  { path: 'admin-category', component: CategoryComponent, canActivate: [AuthGuard] },
  { path: 'admin-order', component: OrderComponent, canActivate: [AuthGuard]},
  { path: 'login-page', component: LoginComponent },
  { path: '', component: LoginComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
