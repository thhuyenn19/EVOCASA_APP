export interface Order {
  _id: string;
  Customer_id: string;
  TrackingNumber: string;
  OrderDate: string;
  ShipDate: string;
  Status:
    | 'Pending'
    | 'Pick Up'
    | 'In Transit'
    | 'Review'
    | 'Cancelled'
    | 'Completed';
  Phone: string;
  Email: string;
  Address: string;
  PaymentMethod: 'Cash on Delivery' | 'Credit/Debit Card' | 'Internet Banking' | 'Momo';
  TotalPrice: number;
  PrePrice: number;
  DeliveryFee: number;
  OrderProduct: {
    _id: string;
    Quantity: number;
  }[];
}
