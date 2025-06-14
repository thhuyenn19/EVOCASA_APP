export interface Category {
  id: string;
  _id: string | { $oid: string };  
  name: string;
  description: string;
  slug: string;
  parentCategory: string | null;
  image: string | string[];
}