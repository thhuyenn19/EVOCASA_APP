import { Injectable } from '@angular/core';
import { getDownloadURL, ref, uploadBytes, deleteObject } from 'firebase/storage';
import { storage } from '../firebase-config'; // cấu hình Firebase Storage của bạn
import { from, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  constructor() {}

  // Upload 1 ảnh
  uploadImage(file: File, folder: string = 'category-images'): Observable<string> {
    const filePath = `${folder}/${Date.now()}_${file.name}`;
    const imageRef = ref(storage, filePath);

    return from(
      uploadBytes(imageRef, file).then(() =>
        getDownloadURL(imageRef)
      )
    );
  }

  // Xóa ảnh theo URL
  deleteImage(imageUrl: string): Observable<void> {
    const imageRef = ref(storage, imageUrl);
    return from(deleteObject(imageRef));
  }

  // Cập nhật ảnh: xóa cũ và upload mới
  updateImage(oldImageUrl: string, newFile: File, folder: string = 'category-images'): Observable<string> {
    const delete$ = this.deleteImage(oldImageUrl);
    const upload$ = this.uploadImage(newFile, folder);
    
    return new Observable<string>((observer) => {
      delete$.subscribe({
        next: () => {
          upload$.subscribe({
            next: (newUrl) => {
              observer.next(newUrl);
              observer.complete();
            },
            error: (uploadErr) => observer.error(uploadErr)
          });
        },
        error: (deleteErr) => observer.error(deleteErr)
      });
    });
  }

}
