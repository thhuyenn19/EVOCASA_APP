export interface IProduct {
  _id?: string; 
  category_id?: string | { $oid: string } | null; 
  category_name?: string;
  Name: string; 
  Price: number; 
  Image: string; // ✅ Chuỗi JSON dạng: "["url1","url2"]"
  Description: string;
  Origin: string; 
  Uses: string; 
  Store: string; 
  Quantity: number; 
  Create_date: Date; 
  Dimension?:
    | string
    | {
        Width?: number;
        Length?: number;
        Height?: number;
        Depth?: number;
        unit?: string; 
      };
  Story?: string; 
  ProductCare?: string; 
  ShippingReturn?: string; 
}

export class Product implements IProduct {
  _id?: string;
  category_id?: string | { $oid: string } | null; 
  category_name?: string;
  Name: string;
  Price: number;
  Image: string; // Luôn là chuỗi JSON array
  Description: string;
  Origin: string;
  Uses: string;
  Store: string;
  Quantity: number;
  Create_date: Date;
  Dimension?:
    | string
    | {
        Width?: number;
        Length?: number;
        Height?: number;
        Depth?: number;
        unit?: string;
      };
  Story?: string;
  ProductCare?: string;
  ShippingReturn?: string;

  constructor(product?: Partial<IProduct>) {
    this._id = product?._id || undefined;
    this.category_name = product?.category_name;
    
    if (typeof product?.category_id === 'string') {
      this.category_id = product.category_id;
    } else if (product?.category_id && typeof product.category_id === 'object' && '$oid' in product.category_id) {
      this.category_id = (product.category_id as { $oid: string }).$oid;
    } else {
      this.category_id = undefined;
    }
    
    this.Name = product?.Name || '';
    this.Price = product?.Price || 0;

    // Xử lý Image - luôn chuyển về JSON string
    if (product?.Image) {
      if (typeof product.Image === 'string') {
        try {
          // Kiểm tra xem đã là JSON string chưa
          const parsed = JSON.parse(product.Image);
          if (Array.isArray(parsed)) {
            this.Image = product.Image; // Đã là JSON string array
          } else {
            this.Image = JSON.stringify([product.Image]); // Chuyển single string thành array
          }
        } catch (e) {
          // Nếu không parse được, coi như single string
          this.Image = JSON.stringify([product.Image]);
        }
      } else if (Array.isArray(product.Image)) {
        this.Image = JSON.stringify(product.Image);
      } else {
        this.Image = JSON.stringify([]);
      }
    } else {
      this.Image = JSON.stringify([]);
    }

    this.Description = product?.Description || '';
    this.Origin = product?.Origin || '';
    this.Uses = product?.Uses || '';
    this.Store = product?.Store || '';
    this.Quantity = product?.Quantity || 0;
    this.Create_date = product?.Create_date || new Date();

    if (product?.Dimension) {
      if (typeof product.Dimension === 'string') {
        this.Dimension = product.Dimension;
      } else {
        this.Dimension = {
          Width: product.Dimension.Width,
          Length: product.Dimension.Length,
          Height: product.Dimension.Height,
          Depth: product.Dimension.Depth,
          unit: product.Dimension.unit || 'in',
        };
      }
    }

    this.Story = product?.Story;
    this.ProductCare = product?.ProductCare;
    this.ShippingReturn = product?.ShippingReturn;
  }

  // Phương thức để lấy mảng images từ JSON string
  getImageArray(): string[] {
    try {
      const parsed = JSON.parse(this.Image);
      return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
      console.error('Error parsing image string:', error);
      return [];
    }
  }

  // Phương thức để set mảng images (chuyển thành JSON string)
  setImageArray(imageArray: string[]): void {
    this.Image = JSON.stringify(imageArray);
  }

  // Phương thức để thêm image
  addImage(url: string) {
  try {
    const current = JSON.parse(this.Image || '[]'); // Parse ra mảng
    current.push(url);
    this.Image = JSON.stringify(current); // Gán lại thành chuỗi
  } catch {
    this.Image = JSON.stringify([url]); // Nếu lỗi thì khởi tạo mới
  }
}
  // Phương thức để xóa image
  removeImage(index: number) {
  try {
    const current = JSON.parse(this.Image || '[]');
    if (Array.isArray(current) && index >= 0 && index < current.length) {
      current.splice(index, 1);
      this.Image = JSON.stringify(current);
    }
  } catch {
    this.Image = '[]';
  }
}

  // Phương thức để lấy image đầu tiên
  getFirstImage(): string {
    const images = this.getImageArray();
    return images.length > 0 ? images[0] : '';
  }

  // Phương thức để lấy số lượng images
  getImageCount(): number {
    return this.getImageArray().length;
  }

  // Validate image string format
  isValidImageFormat(): boolean {
    try {
      const parsed = JSON.parse(this.Image);
      return Array.isArray(parsed) && parsed.every(item => typeof item === 'string');
    } catch {
      return false;
    }
  }

  toFirestoreObject(): IProduct {
    return {
      Name: this.Name,
      Price: Number(this.Price),
      Image: this.Image, // Đã là JSON string array
      Description: this.Description,
      Origin: this.Origin,
      Uses: this.Uses,
      Store: this.Store,
      Quantity: Number(this.Quantity),
      Create_date: this.Create_date || new Date(),
      category_id: this.category_id,
      Dimension: typeof this.Dimension === 'string' ? this.Dimension : this.Dimension ? this.Dimension : '',
      Story: this.Story || '',
      ProductCare: this.ProductCare || '',
      ShippingReturn: this.ShippingReturn || '',
    };
  }

  // Static helper methods
  static arrayToImageString(imageArray: string[]): string {
    return JSON.stringify(imageArray);
  }

  static imageStringToArray(imageString: string): string[] {
    try {
      const parsed = JSON.parse(imageString);
      return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
      console.error('Error parsing image string:', error);
      return [];
    }
  }

  static isValidImageString(imageString: string): boolean {
    try {
      const parsed = JSON.parse(imageString);
      return Array.isArray(parsed) && parsed.every(item => typeof item === 'string');
    } catch {
      return false;
    }
  }
}