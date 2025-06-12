export interface Order {
  _id: string;
  Customer_id: string;
  TrackingNumber: string;
  OrderDate: string;
  ShipDate: string;
  Status: 'Cancelled' | 'In transit' | 'Delivered' | 'Completed';
  Phone: string;
  Email: string;
  Address: string;
  PaymentMethod: 'Cash on Delivery' | 'Credit Card' | 'Bank Transfer';
  TotalPrice: number;
  PrePrice: number;
  DeliveryFee: number;
  OrderProduct: {
    _id: string;
    Quantity: number;
  }[];
}
