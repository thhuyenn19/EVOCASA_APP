export interface IProduct {
  _id?: string; 
  category_id?: string; 
  category_name?: string;
  Name: string; 
  Price: number; 
  Image: string[]; 
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
  category_id?: string;
  Name: string;
  Price: number;
  Image: string[];
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
    this.category_id = product?.category_id || undefined;
    this.Name = product?.Name || '';
    this.Price = product?.Price || 0;

    if (product?.Image) {
      if (typeof product.Image === 'string') {
        try {
          this.Image = JSON.parse(product.Image);
        } catch (e) {
          this.Image = [product.Image as string];
        }
      } else {
        this.Image = product.Image;
      }
    } else {
      this.Image = [];
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
}
