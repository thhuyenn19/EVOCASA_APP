package com.thanhhuyen.models;

import com.google.firebase.firestore.PropertyName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Order {
    private String orderId;

    // Firestore fields - giữ tên chính xác như trong database
    @PropertyName("OrderDate")
    private Map<String, Object> OrderDate;

    @PropertyName("TrackingNumber")
    private String TrackingNumber;

    @PropertyName("Status")
    private String Status;

    @PropertyName("TotalPrice")
    private int TotalPrice;

    @PropertyName("Customer_id")
    private Map<String, String> Customer_id;

    @PropertyName("CustomerName")
    private String CustomerName;

    @PropertyName("PaymentMethod")
    private String PaymentMethod;

    @PropertyName("ShippingMethod")
    private String ShippingMethod;

    @PropertyName("DeliveryFee")
    private Object DeliveryFee; // Có thể là Number hoặc Map

    @PropertyName("ShipDate")
    private Object ShipDate; // Có thể là Date hoặc Map

    @PropertyName("PrePrice")
    private Object PrePrice; // Có thể là Number hoặc Map

    @PropertyName("ProductId")
    private String ProductId;

    @PropertyName("Quantity")
    private int Quantity;

    @PropertyName("Voucher")
    private Map<String, Object> Voucher;

    @PropertyName("ShippingAddresses")
    private Map<String, Object> ShippingAddresses;

    @PropertyName("Note")
    private String Note;

    // Legacy fields for backward compatibility
    private String customerPhone;
    private String paymentMethod;
    private double prePrice;
    private double deliveryFee;
    private Date shipDate;
    private String shippingMethod;
    private String note;
    private String productId;
    private int quantity;

    public Order() {}

    // Basic getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @PropertyName("OrderDate")
    public Map<String, Object> getOrderDate() { return OrderDate; }
    @PropertyName("OrderDate")
    public void setOrderDate(Map<String, Object> orderDate) { OrderDate = orderDate; }

    @PropertyName("TrackingNumber")
    public String getTrackingNumber() { return TrackingNumber; }
    @PropertyName("TrackingNumber")
    public void setTrackingNumber(String trackingNumber) { TrackingNumber = trackingNumber; }

    @PropertyName("Status")
    public String getStatus() { return Status; }
    @PropertyName("Status")
    public void setStatus(String status) { Status = status; }

    @PropertyName("TotalPrice")
    public int getTotalPrice() { return TotalPrice; }
    @PropertyName("TotalPrice")
    public void setTotalPrice(int totalPrice) { TotalPrice = totalPrice; }

    @PropertyName("Customer_id")
    public Map<String, String> getCustomer_id() { return Customer_id; }
    @PropertyName("Customer_id")
    public void setCustomer_id(Map<String, String> customer_id) { Customer_id = customer_id; }

    @PropertyName("CustomerName")
    public String getCustomerName() { return CustomerName; }
    @PropertyName("CustomerName")
    public void setCustomerName(String customerName) { CustomerName = customerName; }

    @PropertyName("Voucher")
    public Map<String, Object> getVoucher() { return Voucher; }
    @PropertyName("Voucher")
    public void setVoucher(Map<String, Object> voucher) { Voucher = voucher; }

    @PropertyName("ShippingAddresses")
    public Map<String, Object> getShippingAddresses() { return ShippingAddresses; }
    @PropertyName("ShippingAddresses")
    public void setShippingAddresses(Map<String, Object> shippingAddresses) { ShippingAddresses = shippingAddresses; }

    // Smart getters that handle both Firestore and legacy fields
    public String getPaymentMethod() {
        if (PaymentMethod != null && !PaymentMethod.trim().isEmpty()) {
            return PaymentMethod;
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            return paymentMethod;
        }
        return "N/A";
    }

    public void setPaymentMethod(String method) {
        this.PaymentMethod = method;
        this.paymentMethod = method; // Keep both for compatibility
    }

    public String getShippingMethod() {
        if (ShippingMethod != null && !ShippingMethod.trim().isEmpty()) {
            return ShippingMethod;
        }
        if (shippingMethod != null && !shippingMethod.trim().isEmpty()) {
            return shippingMethod;
        }
        return "N/A";
    }

    public void setShippingMethod(String method) {
        this.ShippingMethod = method;
        this.shippingMethod = method; // Keep both for compatibility
    }

    public double getDeliveryFee() {
        // Try Firestore field first
        if (DeliveryFee != null) {
            if (DeliveryFee instanceof Number) {
                return ((Number) DeliveryFee).doubleValue();
            } else if (DeliveryFee instanceof Map) {
                Map<?, ?> feeMap = (Map<?, ?>) DeliveryFee;
                if (feeMap.containsKey("amount")) {
                    Object amount = feeMap.get("amount");
                    if (amount instanceof Number) {
                        return ((Number) amount).doubleValue();
                    }
                }
            }
        }
        // Fallback to legacy field
        return deliveryFee;
    }

    public void setDeliveryFee(double fee) {
        this.DeliveryFee = fee;
        this.deliveryFee = fee; // Keep both for compatibility
    }

    public double getPrePrice() {
        // Try Firestore field first
        if (PrePrice != null) {
            if (PrePrice instanceof Number) {
                return ((Number) PrePrice).doubleValue();
            } else if (PrePrice instanceof Map) {
                Map<?, ?> priceMap = (Map<?, ?>) PrePrice;
                if (priceMap.containsKey("amount")) {
                    Object amount = priceMap.get("amount");
                    if (amount instanceof Number) {
                        return ((Number) amount).doubleValue();
                    }
                }
            }
        }
        // Fallback to legacy field
        return prePrice;
    }

    public void setPrePrice(double price) {
        this.PrePrice = price;
        this.prePrice = price; // Keep both for compatibility
    }

    public String getProductId() {
        if (ProductId != null && !ProductId.trim().isEmpty()) {
            return ProductId;
        }
        return (productId != null) ? productId : "N/A";
    }

    public void setProductId(String id) {
        this.ProductId = id;
        this.productId = id; // Keep both for compatibility
    }

    public int getQuantity() {
        if (Quantity > 0) {
            return Quantity;
        }
        return quantity;
    }

    public void setQuantity(int qty) {
        this.Quantity = qty;
        this.quantity = qty; // Keep both for compatibility
    }

    public String getNote() {
        if (Note != null && !Note.trim().isEmpty()) {
            return Note;
        }
        return (note != null) ? note : "";
    }

    public void setNote(String note) {
        this.Note = note;
        this.note = note; // Keep both for compatibility
    }

    // Legacy setters for backward compatibility
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Date getShipDate() { return shipDate; }
    public void setShipDate(Date shipDate) { this.shipDate = shipDate; }

    // Utility method for Customer ID
    public String getCustomerIdString() {
        if (Customer_id != null && Customer_id.containsKey("$oid")) {
            String customerId = Customer_id.get("$oid");
            if (customerId != null && !customerId.isEmpty()) {
                return customerId;
            }
        }
        return "N/A";
    }

    // Utility methods for Voucher information
    public String getVoucherName() {
        if (Voucher != null && Voucher.get("VoucherName") != null) {
            return Voucher.get("VoucherName").toString();
        }
        return "N/A";
    }

    public double getDiscountAmount() {
        if (Voucher != null && Voucher.get("DiscountAmount") != null) {
            try {
                return Double.parseDouble(Voucher.get("DiscountAmount").toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public double getDiscountPercent() {
        if (Voucher != null && Voucher.get("DiscountPercent") != null) {
            try {
                return Double.parseDouble(Voucher.get("DiscountPercent").toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // Utility methods for Shipping Address information
    public String getShippingAddress() {
        if (ShippingAddresses != null && ShippingAddresses.get("Address") != null) {
            return ShippingAddresses.get("Address").toString();
        }
        return "N/A";
    }

    public String getShippingPhone() {
        if (ShippingAddresses != null && ShippingAddresses.get("Phone") != null) {
            return ShippingAddresses.get("Phone").toString();
        }
        return "N/A";
    }

    public String getShippingName() {
        if (ShippingAddresses != null && ShippingAddresses.get("Name") != null) {
            return ShippingAddresses.get("Name").toString();
        }
        return "N/A";
    }

    // Formatted date methods
    public String getFormattedOrderDate() {
        if (OrderDate != null) {
            // Handle Firestore Timestamp format
            if (OrderDate.containsKey("_seconds")) {
                try {
                    long seconds = Long.parseLong(OrderDate.get("_seconds").toString());
                    Date date = new Date(seconds * 1000);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return sdf.format(date);
                } catch (Exception e) {
                    // Ignore and try other formats
                }
            }
            // Handle other timestamp formats
            if (OrderDate.containsKey("$date")) {
                Object dateValue = OrderDate.get("$date");
                if (dateValue instanceof Number) {
                    Date date = new Date(((Number) dateValue).longValue());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return sdf.format(date);
                }
            }
        }
        return "N/A";
    }

    public String getFormattedShipDate() {
        // Try Firestore field first
        if (ShipDate != null) {
            if (ShipDate instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format((Date) ShipDate);
            } else if (ShipDate instanceof Map) {
                Map<?, ?> dateMap = (Map<?, ?>) ShipDate;
                // Handle Firestore Timestamp format
                if (dateMap.containsKey("_seconds")) {
                    try {
                        long seconds = Long.parseLong(dateMap.get("_seconds").toString());
                        Date date = new Date(seconds * 1000);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        return sdf.format(date);
                    } catch (Exception e) {
                        // Ignore and try other formats
                    }
                }
                // Handle other timestamp formats
                if (dateMap.containsKey("$date")) {
                    Object dateValue = dateMap.get("$date");
                    if (dateValue instanceof Number) {
                        Date date = new Date(((Number) dateValue).longValue());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        return sdf.format(date);
                    }
                }
            } else if (ShipDate instanceof Number) {
                Date date = new Date(((Number) ShipDate).longValue());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(date);
            }
        }

        // Fallback to legacy field
        if (shipDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(shipDate);
        }

        return "N/A";
    }

    // Formatted price methods
    public String getFormattedPrePrice() {
        double price = getPrePrice();
        if (price > 0) {
            return String.format(Locale.getDefault(), "$%,.0f", price);
        }
        return "$0";
    }

    public String getFormattedDeliveryFee() {
        double fee = getDeliveryFee();
        if (fee > 0) {
            return String.format(Locale.getDefault(), "$%,.0f", fee);
        }
        return "$0";
    }

    public String getFormattedDiscountAmount() {
        double discount = getDiscountAmount();
        if (discount > 0) {
            return String.format(Locale.getDefault(), "-$%,.0f", discount);
        }
        return "$0";
    }

    public String getFormattedTotalPrice() {
        return String.format(Locale.getDefault(), "$%,d", TotalPrice);
    }
}