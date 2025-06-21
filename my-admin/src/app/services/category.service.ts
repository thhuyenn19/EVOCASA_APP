import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, retry, catchError, throwError, tap, from } from 'rxjs';
import { Category } from '../interfaces/category';

import { getDocs } from 'firebase/firestore';
import { db } from '../firebase-config'; // Đã sửa đúng đường dẫn
import {
  collection,
  doc,
  getDoc,
  updateDoc,
  setDoc,
  deleteDoc,
} from 'firebase/firestore';

interface CategoryHierarchy extends Category {
  children: CategoryHierarchy[];
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  // private apiUrl = 'http://localhost:3002/categories';
  // private baseUrl = 'http://localhost:3002';

  // constructor(private _http: HttpClient) {}

  constructor() {}

  getCategories(): Observable<Category[]> {
    // console.log('Fetching categories from:', this.apiUrl);
    // return this._http.get<any[]>(this.apiUrl).pipe(
    //   tap(response => console.log('Raw categories response:', response)),
    //   map(response => {
    //     return response.map(item => this.normalizeCategoryData(item));
    //   }),
    //   tap(categories => console.log('Processed categories:', categories)),
    //   retry(3),
    //   catchError(this.handleError)
    // );
    const categoryRef = collection(db, 'Category'); 
    const categoryPromise = getDocs(categoryRef).then(snapshot => {
      return snapshot.docs.map(doc => {
        const data = doc.data() as any;
        return {
          id: (data._id && data._id.$oid) || doc.id,
          name: data.Name,
          description: data.Description,
          image: [data.Image], // convert to array as expected
          parentCategory: data.ParentCategory,
          slug: data.Slug,
        } as Category;
      });
    });

    return from(categoryPromise);
  }

   getCategory(id: string): Observable<Category> {
    // console.log(`Fetching category with ID: ${id}`);
    // return this._http.get<Category>(`${this.apiUrl}/${id}`).pipe(
    //   tap(response => console.log('Raw category response:', response)),
    //   map(response => this.normalizeCategoryData(response)),
    //   map(category => this.processCategoryImage(category)),
    //   retry(3),
    //   catchError(this.handleError)
    // );
    console.log(`Fetching category with ID: ${id}`);
    const docRef = doc(db, 'Category', id);
    const docPromise = getDoc(docRef).then((docSnap) => {
      if (docSnap.exists()) {
        const data = docSnap.data() as any;
        return {
          id: docSnap.id,
          name: data.Name,
          description: data.Description,
          image: [data.Image],
          parentCategory: (
        data.ParentCategory && typeof data.ParentCategory === 'object' && '$oid' in data.ParentCategory
          ? data.ParentCategory.$oid
          : data.ParentCategory || null
      ),
          slug: data.Slug,
        } as Category;
      } else {
        throw new Error('Category not found');
      }
    });

    return from(docPromise);
  }

  putCategory(category: Category): Observable<void> {
    // console.log('Updating category:', category);
    // const headers = new HttpHeaders().set("Content-Type", "application/json");
    // return this._http.put<any[]>(this.apiUrl, category, { headers }).pipe(
    //   tap(response => console.log('Category update response:', response)),
    //   retry(3),
    //   catchError(this.handleError)
    // );
    console.log('Updating category:', category);
    const docRef = doc(db, 'Category', category.id);
    const updateData = {
      Name: category.name,
      Description: category.description,
      Image: category.image?.[0] || '',
      ParentCategory: category.parentCategory
        ? { $oid: category.parentCategory }
        : null,
      Slug: category.slug,
    };

    return from(updateDoc(docRef, updateData));
  }

  createCategory(category: Category): Observable<void> {
    // console.log('Creating new category:', category);
    // const headers = new HttpHeaders().set("Content-Type", "application/json");
    // return this._http.post<any>(this.apiUrl, category, { headers }).pipe(
    //   tap(response => console.log('Category creation response:', response)),
    //   map(response => this.normalizeCategoryData(response)),
    //   map(category => this.processCategoryImage(category)),
    //   retry(3),
    //   catchError(this.handleError)
    // );
    console.log('Creating new category:', category);
    const newDocRef = doc(collection(db, 'Category'));
    const categoryData = {
      Name: category.name,
      Description: category.description,
      Image: category.image?.[0] || '',
      ParentCategory: category.parentCategory
        ? { $oid: category.parentCategory }
        : null,
      Slug: category.slug,
    };

    return from(setDoc(newDocRef, categoryData));
  }

  deleteCategory(categoryId: string): Observable<void> {
    // console.log(`Deleting category with ID: ${categoryId}`);
    // const headers = new HttpHeaders().set("Content-Type", "application/json");
    // return this._http.delete<any>(`${this.apiUrl}/${categoryId}`, { headers }).pipe(
    //   tap(response => console.log('Category deletion response:', response)),
    //   retry(3),
    //   catchError(this.handleError)
    // );
    console.log(`Deleting category with ID: ${categoryId}`);
    const docRef = doc(db, 'Category', categoryId);
    return from(deleteDoc(docRef));
  }

  getMainCategories(): Observable<Category[]> {
    console.log('Fetching main categories');
    return this.getCategories().pipe(
      map(categories => {
        const mainCats = categories.filter(category => category.parentCategory === null);
        console.log(`Found ${mainCats.length} main categories`);
        return mainCats;
      })
    );
  }

  getSubcategories(parentCategoryId: string): Observable<Category[]> {
    console.log(`Fetching subcategories for parent ID: ${parentCategoryId}`);
    return this.getCategories().pipe(
      map(categories => {
        const subCats = categories.filter(category => 
          category.parentCategory === parentCategoryId
        );
        console.log(`Found ${subCats.length} subcategories for parent ${parentCategoryId}`);
        return subCats;
      })
    );
  }


  getCategoryPath(categoryId: string): Observable<Category[]> {
    console.log(`Building category path for ID: ${categoryId}`);
    return this.getCategories().pipe(
      map(categories => {
        const path: Category[] = [];
        let currentCategory = categories.find(c => c._id === categoryId);
        
        if (!currentCategory) {
          console.warn(`Category with ID ${categoryId} not found`);
          return path;
        }
  
        path.unshift(currentCategory);
        
        while (currentCategory && currentCategory.parentCategory) {
          let parentId: string | null = null;
          if (typeof currentCategory.parentCategory === 'object' && currentCategory.parentCategory !== null && '$oid' in currentCategory.parentCategory) {
            parentId = currentCategory.parentCategory.$oid;
          } else {
            parentId = currentCategory.parentCategory;
          }
          currentCategory = categories.find(c => c._id === parentId);
          if (currentCategory) {
            path.unshift(currentCategory);
          } else {
            console.warn(`Parent category with ID ${parentId} not found`);
          }
        }
        
        console.log(`Category path: ${path.map(c => c.name).join(' > ')}`);
        return path;
      })
    );
  }

  getCategoryHierarchy(): Observable<CategoryHierarchy[]> {
    console.log('Building category hierarchy');
    return this.getCategories().pipe(
      map(categories => {
        const mainCategories = categories.filter(c => c.parentCategory === null);
        
        const buildHierarchy = (category: Category): CategoryHierarchy => {
          const children = categories.filter(c => 
            c.parentCategory === category._id
          );
          
          return {
            ...category,
            children: children.map(buildHierarchy)
          };
        };
        
        const hierarchy = mainCategories.map(buildHierarchy);
        console.log(`Built hierarchy with ${hierarchy.length} main categories`);
        return hierarchy;
      })
    );
  }


  private normalizeCategoryData(item: any): Category {

    let id = item._id;
    if (typeof id === 'object' && id && '$oid' in id) {
      id = id.$oid;
    }
    
    return {
      id: item.id || id || '',
      _id: id || item.id || '',
      name: item.name || item.Name || 'Unnamed Category',
      description: item.description || item.Description || '',
      slug: item.slug || item.Slug || '',
      parentCategory: item.parentCategory || item.ParentCategory || null,
      image: item.image || item.Image || ''
    };
  }


  handleError(error: HttpErrorResponse) {
    let errorMessage = '';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      errorMessage = `Server Error: ${error.status} - ${error.statusText || ''}\nMessage: ${error.message}`;
    }
    
    console.error('CategoryService Error:', errorMessage);
    console.error('Full error:', error);
    
    return throwError(() => new Error(errorMessage));
  }


  private processCategoryImages(categories: Category[]): Category[] {
    return categories.map(category => this.processCategoryImage(category));
  }
  processCategoryImage(category: Category): Category {
    if (category.image && typeof category.image === 'string') {
      try {
        category.image = JSON.parse(category.image as unknown as string);
      } catch (e) {
        category.image = [category.image as unknown as string];
      }
    }
    return category;
  }
  getCategoryIdByName(name: string): Observable<string | null> {
  return this.getCategories().pipe(
    map(categories => {
      const found = categories.find(cat => cat.name === name);
      if (!found) return null;
      if (typeof found._id === 'object' && found._id !== null && '$oid' in found._id) {
        return found._id.$oid as string;
      }
      return found._id as string;
    })
  );
}


}