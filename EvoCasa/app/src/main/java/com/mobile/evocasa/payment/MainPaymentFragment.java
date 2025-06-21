package com.mobile.evocasa.payment;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.ShippingMethodAdapter;
import com.mobile.adapters.VoucherAdapter;
import com.mobile.evocasa.NarBarActivity;
import com.mobile.evocasa.R;
import com.mobile.models.CartProduct;
import com.mobile.models.ShippingAddress;
import com.mobile.models.ShippingMethod;
import com.mobile.models.Voucher;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

public class MainPaymentFragment extends Fragment {
    private View currentSelectedOption = null;
    private LinearLayout layoutUseVoucher, voucherOptionsLayout;
    private View overlayBackground;
    private RecyclerView recyclerVoucher;
    private TextView txtVoucher;
    private ImageView btnCloseVoucherLayout;
    private List<CartProduct> selectedProducts = new ArrayList<>();
    private List<Voucher> voucherList = new ArrayList<>();
    private Voucher selectedVoucher;
    private ShippingAddress selectedShipping = null;
    private LinearLayout productContainer;
    private LinearLayout layoutShipping;
    private View overlayShipping;
    private LinearLayout shippingOptionsLayout;
    private RecyclerView rvShipping;
    private ImageView btnCloseShipping;
    private TextView txtName, txtFee, txtDesc;
    private String selectedPaymentMethod = null;
    // nếu Credit, lưu tiếp detail
    private String savedCardNumber, savedCardName, savedExpiry, savedCvv;
    private int selectedColor;
    private int defaultColor;
    private TextView txtTotalPrice, txtShippingFee, txtDiscount, txtTotalValue, txtSavingValue;
    private double currentShippingFee = 0;
    TextView txtTotalValueBottom, txtSavingValueBottom;
    private double calculatedTotal = 0;
    private double calculatedSaving = 0;
    private boolean isDefault;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_payment, container, false);
        getParentFragmentManager()
                .setFragmentResultListener("select_payment_method", getViewLifecycleOwner(),
                        (key, bundle) -> {
                            // lấy phương thức
                            selectedPaymentMethod = bundle.getString("paymentMethod");
                            // nếu credit thì lấy chi tiết
                            if ("CREDIT".equals(selectedPaymentMethod)) {
                                savedCardNumber = bundle.getString("cardNumber");
                                savedCardName   = bundle.getString("cardName");
                                savedExpiry     = bundle.getString("expiry");
                                savedCvv        = bundle.getString("cvv");
                            }
                            // cập nhật UI highlight
                            highlightPaymentOption(view);
                        });

        productContainer = view.findViewById(R.id.productContainer);
        // Lấy dữ liệu cartPayment từ Intent (của Activity chứa fragment này)
        Intent intent = requireActivity().getIntent();
        String jsonCart = intent.getStringExtra("cartPayment");
        Type type = new TypeToken<List<CartProduct>>() {}.getType();
        List<CartProduct> cartPayment = new Gson().fromJson(jsonCart, type);
        selectedProducts = cartPayment;            // lưu tạm
        bindProductsToUI(cartPayment);             // 2) show lên UI

        // In ra console để kiểm tra
        Log.d("PaymentActivity", "Received cartPayment: " + new Gson().toJson(cartPayment));

        // Set font và underline cho các TextView cần thiết
        FontUtils.applyFont(view.findViewById(R.id.txtShippingAddress), requireContext(), R.font.inter);


        getParentFragmentManager()
                .setFragmentResultListener("select_shipping", getViewLifecycleOwner(),
                        (requestKey, bundle) -> {
                            // 3) Lấy object ShippingAddress từ bundle và cập nhật UI
                            ShippingAddress addr = (ShippingAddress) bundle.getSerializable("selectedShipping");
                            if (addr != null) {
                                selectedShipping = addr;
                                updateShippingUI(view, selectedShipping);
                            }
                        });


        txtVoucher = view.findViewById(R.id.txtVoucher);
        txtVoucher.setPaintFlags(txtVoucher.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView txtEditInfor = view.findViewById(R.id.txtEditInfor);
        txtEditInfor.setPaintFlags(txtEditInfor.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        LinearLayout optionCOD     = view.findViewById(R.id.optionCOD);
        LinearLayout optionMomo    = view.findViewById(R.id.optionMomo);
        LinearLayout optionBanking = view.findViewById(R.id.optionBanking);
        LinearLayout optionCredit  = view.findViewById(R.id.optionCredit);

        selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        defaultColor  = getResources().getColor(android.R.color.transparent, null);

        // 4) Dùng 1 listener chung
        View.OnClickListener paymentClickListener = v -> {
            /// reset all four
            resetOption(optionCOD,     defaultColor);
            resetOption(optionMomo,    defaultColor);
            resetOption(optionBanking, defaultColor);
            resetOption(optionCredit,  defaultColor);

            // select new
            currentSelectedOption = v;
            v.setBackgroundColor(selectedColor);
            setTextStyleInViewGroup((ViewGroup)v, Typeface.BOLD);

            // update state
            if      (v == optionCOD)     selectedPaymentMethod = "COD";
            else if (v == optionMomo)    selectedPaymentMethod = "MOMO";
            else if (v == optionBanking) selectedPaymentMethod = "BANKING";
            else if (v == optionCredit)  selectedPaymentMethod = "CREDIT";
        };


        optionCOD    .setOnClickListener(paymentClickListener);
        optionMomo   .setOnClickListener(paymentClickListener);
        optionBanking.setOnClickListener(paymentClickListener);
        optionCredit.setOnClickListener(v -> {
            // Set selected method
            selectedPaymentMethod = "CREDIT";

            // Chuyển qua fragment PaymentMethodFragment và truyền CREDIT luôn
            PaymentMethodFragment frag = new PaymentMethodFragment();
            Bundle args = new Bundle();
            args.putString("paymentMethod", "CREDIT");

            // nếu đã có dữ liệu card từ trước thì truyền tiếp
            if (savedCardNumber != null) {
                args.putString("cardNumber", savedCardNumber);
                args.putString("cardName", savedCardName);
                args.putString("expiry", savedExpiry);
                args.putString("cvv", savedCvv);
            }

            frag.setArguments(args);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit();
        });


        // reset ban đầu
        resetOption(optionCOD,     defaultColor);
        resetOption(optionMomo,    defaultColor);
        resetOption(optionBanking, defaultColor);
        resetOption(optionCredit,  defaultColor);

        // 5) “See All Payment” mở PaymentMethodFragment cùng state
        TextView txtSeeAllPayment = view.findViewById(R.id.txtSeeAllPayment);
        txtSeeAllPayment.setOnClickListener(v -> {
            PaymentMethodFragment frag = new PaymentMethodFragment();
            Bundle args = new Bundle();
            args.putDouble("total", calculatedTotal);      // truyền total
            args.putDouble("saving", calculatedSaving);    // truyền saving
            if (selectedPaymentMethod != null) {
                args.putString("paymentMethod", selectedPaymentMethod);
                if ("CREDIT".equals(selectedPaymentMethod)) {
                    args.putString("cardNumber", savedCardNumber);
                    args.putString("cardName",   savedCardName);
                    args.putString("expiry",     savedExpiry);
                    args.putString("cvv",        savedCvv);
                }
            }
            frag.setArguments(args);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit();
        });

        // Chuyển sang fragment chọn edit Shipping Address
        txtEditInfor.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ShippingAddressFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Nút Checkout
        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            if (selectedPaymentMethod == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            // Tạo ID cho order
            String orderId = String.valueOf(System.currentTimeMillis());

            // Lấy uid từ session
            String uid = new UserSessionManager(requireContext()).getUid();

            // Chuẩn bị dữ liệu đơn hàng
            Map<String, Object> order = new HashMap<>();

            // Gán ID người dùng
            Map<String, Object> customerIdObj = new HashMap<>();
            customerIdObj.put("$oid", uid);
            order.put("Customer_id", customerIdObj);

            Map<String, Object> orderIdObj = new HashMap<>();
            orderIdObj.put("$oid", orderId);
            order.put("_id", orderIdObj);

            Map<String, Object> orderDateObj = new HashMap<>();
            orderDateObj.put("$date", new Date());

            order.put("OrderDate", orderDateObj);
            order.put("ShipDate", orderDateObj);  // hoặc tạo 1 bản riêng nếu khác thời gian

// Shipping info
            order.put("ShippingMethod", txtName.getText().toString());
            order.put("DeliveryFee", currentShippingFee);

            // Ghi chú từ người dùng (cho phép rỗng)
            String note = ((TextView) requireView().findViewById(R.id.edtMessageForShop)).getText().toString();
            order.put("Note", note);

            // Trạng thái đơn
            order.put("Status", "Pending");

            // Tạo mã tracking random
            String tracking = "TRK" + String.format("%06d", new Random().nextInt(999999));
            order.put("TrackingNumber", tracking);

            // Phương thức thanh toán
            order.put("PaymentMethod", getPaymentText(selectedPaymentMethod));

            // Giá tiền
            double subtotal = calculateSubtotal();
            order.put("TotalPrice", subtotal + currentShippingFee - calculatedSaving);
            order.put("PrePrice", subtotal);

            // Danh sách sản phẩm
            List<Map<String, Object>> orderProducts = new ArrayList<>();
            for (CartProduct p : selectedProducts) {
                Map<String, Object> product = new HashMap<>();
                product.put("Quantity", p.getQuantity());
                product.put("Customize", null); // nếu có tuỳ chỉnh thì thay thế ở đây

                // Product ID kiểu $oid
                Map<String, Object> idObj = new HashMap<>();
                idObj.put("$oid", p.getId());
                product.put("id", idObj);

                orderProducts.add(product);
            }
            order.put("OrderProduct", orderProducts);

            // Thông tin Voucher (nếu có)
            if (selectedVoucher != null) {
                Map<String, Object> voucher = new HashMap<>();
                voucher.put("VoucherName", selectedVoucher.getName());
                voucher.put("DiscountAmount", calculatedSaving);
                voucher.put("DiscountPercent", selectedVoucher.getDiscountPercent());

                // Nếu có ID của voucher → thêm vào
                if (selectedVoucher.getId() != null) {
                    Map<String, Object> voucherIdObj = new HashMap<>();
                    voucherIdObj.put("$oid", selectedVoucher.getId());
                }

                order.put("Voucher", voucher);
            }
            if (selectedShipping != null) {
                Map<String, Object> shippingAddress = new HashMap<>();
                shippingAddress.put("Name", selectedShipping.getName());
                shippingAddress.put("Phone", selectedShipping.getPhone());
                shippingAddress.put("Address", selectedShipping.getAddress());

                order.put("ShippingAddresses", shippingAddress);
            }


            // Gửi lên Firestore
            FirebaseFirestore.getInstance()
                    .collection("Order")
                    .document(orderId)
                    .set(order)
                    .addOnSuccessListener(aVoid -> {
                        // Push thông báo sau khi tạo đơn thành công
                        Map<String, Object> notification = new HashMap<>();
                        String notiId = "NOTI" + System.currentTimeMillis();
                        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

                        notification.put("NotificationId", notiId);
                        notification.put("CreatedAt", createdAt);
                        notification.put("Title", "Payment Confirmed");
                        notification.put("Content", "Your order " + orderId + " has been placed successfully.");
                        notification.put("Image", "/images/Notification/OrderPlaced.jpg");  // bạn có thể đổi lại đường dẫn phù hợp
                        notification.put("Status", "Unread");
                        notification.put("Type", "PaymentConfirmed");

// Đẩy vào danh sách Notifications của người dùng
                        FirebaseFirestore.getInstance()
                                .collection("Customers")
                                .document(uid)
                                .update("Notifications", FieldValue.arrayUnion(notification));
                        // Chuyển sang trang finish khi thành công
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new FinishPaymentFragment())
                                .addToBackStack(null)
                                .commit();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to place order", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });


        });



        layoutUseVoucher = view.findViewById(R.id.layoutUseVoucher);
        voucherOptionsLayout = view.findViewById(R.id.voucherOptionsLayout);
        overlayBackground = view.findViewById(R.id.overlayBackground);
        recyclerVoucher = view.findViewById(R.id.recyclerVoucher);
        btnCloseVoucherLayout = view.findViewById(R.id.btnCloseVoucherLayout);

        layoutUseVoucher.setOnClickListener(v -> {
            loadAndDisplayVouchers();
            overlayBackground.setVisibility(View.VISIBLE);
            voucherOptionsLayout.setVisibility(View.VISIBLE);
        });

        btnCloseVoucherLayout.setOnClickListener(v -> {
            overlayBackground.setVisibility(View.GONE);
            voucherOptionsLayout.setVisibility(View.GONE);
        });

        overlayBackground.setOnClickListener(v -> {
            overlayBackground.setVisibility(View.GONE);
            voucherOptionsLayout.setVisibility(View.GONE);
        });
        layoutShipping         = view.findViewById(R.id.layoutShippingMethod);
        overlayShipping        = view.findViewById(R.id.overlayShipping);
        shippingOptionsLayout  = view.findViewById(R.id.shippingOptionsLayout);
        rvShipping             = view.findViewById(R.id.recyclerShipping);
        btnCloseShipping       = view.findViewById(R.id.btnCloseShippingLayout);

        txtName  = view.findViewById(R.id.txtShippingMethodName);
        txtFee   = view.findViewById(R.id.txtShippingPrice);
        txtDesc  = view.findViewById(R.id.txtShippingMethodDesc);

        txtTotalPrice    = view.findViewById(R.id.txtTotalPrice);
        txtShippingFee   = view.findViewById(R.id.txtShippingFee);
        txtDiscount      = view.findViewById(R.id.txtDiscount);
        txtTotalValue    = view.findViewById(R.id.txtTotalOrderValue);
        txtSavingValue   = view.findViewById(R.id.txtTotalSaveValue);

        txtTotalValueBottom = view.findViewById(R.id.txtTotalValue);
        txtSavingValueBottom = view.findViewById(R.id.txtSavingValue);



        return view;
    }

    private Object getPaymentText(String method) {
        if (method == null) return "COD";
        switch (method) {
            case "MOMO": return "Momo";
            case "BANKING": return "Internet Banking";
            case "CREDIT": return "Credit / Debit Card";
            default: return "COD";
        }
    }

    private void highlightPaymentOption(View root) {
        if (selectedPaymentMethod == null) return;
        LinearLayout c = root.findViewById(R.id.optionCOD);
        LinearLayout m = root.findViewById(R.id.optionMomo);
        LinearLayout b = root.findViewById(R.id.optionBanking);
        LinearLayout r = root.findViewById(R.id.optionCredit);

        resetOption(c, defaultColor);
        resetOption(m, defaultColor);
        resetOption(b, defaultColor);
        resetOption(r, defaultColor);

        View toSelect = null;
        switch (selectedPaymentMethod) {
            case "COD":     toSelect = c; break;
            case "MOMO":    toSelect = m; break;
            case "BANKING": toSelect = b; break;
            case "CREDIT":  toSelect = r; break;
        }
        if (toSelect != null) {
            toSelect.setBackgroundColor(selectedColor);
            setTextStyleInViewGroup((ViewGroup)toSelect, Typeface.BOLD);
            currentSelectedOption = toSelect;
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String cartJson = args.getString("cartPayment");
            Log.d("MainPaymentFragment", "cartJson = " + cartJson);

            String voucherJson = args.getString("selectedVoucher");
            Log.d("MainPaymentFragment", "voucherJson = " + voucherJson);

            Type listType = new TypeToken<List<CartProduct>>(){}.getType();
            List<CartProduct> productList = new Gson().fromJson(cartJson, listType);

            Voucher selectedVoucher = null;
            if (voucherJson != null) {
                selectedVoucher = new Gson().fromJson(voucherJson, Voucher.class);
            }

            // DEBUG LOG
            Log.d("MainPaymentFragment", "Received products: " + new Gson().toJson(productList));
            if (selectedVoucher != null) {
                Log.d("MainPaymentFragment", "Received voucher: " + new Gson().toJson(selectedVoucher));
            }

            bindProductsToUI(productList);
            bindVoucherToUI(selectedVoucher);
            double subtotal = calculateSubtotal();           // tổng price x qty
            double shippingFee = 50;                         // mặc định là Express Delivery
            double discount = 0;

            if (selectedVoucher != null) {
                double percent = selectedVoucher.getDiscountPercent();
                double maxValue = selectedVoucher.getMaxDiscount();
                discount = Math.min(subtotal * percent / 100, maxValue);
            }

            calculatedTotal = subtotal + shippingFee - discount;
            calculatedSaving = discount;
            updatePaymentDetails();
        }
        // 2) chuẩn bị dữ liệu
        List<ShippingMethod> shippingList = Arrays.asList(
                new ShippingMethod(
                        "Express Delivery", 50,
                        "Received on Thursday, May 15, 2025",
                        R.drawable.ic_express_delivery),
                new ShippingMethod(
                        "Standard Delivery", 20,
                        "Received on Monday, May 19, 2025",
                        R.drawable.ic_standard_delivery),
                new ShippingMethod(
                        "Weekend Delivery", 30,
                        "Received on Saturday, May 18, 2025",
                        R.drawable.ic_weekend_delivery)
        );

        // 3) thiết lập RecyclerView + Adapter
        ShippingMethodAdapter adapter =
                new ShippingMethodAdapter(shippingList, requireContext());
        rvShipping.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvShipping.setAdapter(adapter);

        adapter.setOnItemClickListener(method -> {
            // cập nhật summary khi chọn
            txtName.setText(method.getName());
            txtFee .setText("$" + (int)method.getPrice());
            txtDesc.setText(method.getReceiveOn());

            currentShippingFee = method.getPrice();  // ← thêm dòng này
            updatePaymentDetails();
            // ẩn overlay
            shippingOptionsLayout.setVisibility(View.GONE);
            overlayShipping.setVisibility(View.GONE);
        });

        // 4) default hiển thị mục đầu tiên
        ShippingMethod def = shippingList.get(0);
        txtName.setText(def.getName());
        txtFee .setText("$" + (int)def.getPrice());
        txtDesc.setText(def.getReceiveOn());
        currentShippingFee = def.getPrice();  // ← tính luôn phí giao hàng mặc định
        updatePaymentDetails();               // ← cập nhật lại tổng cộng

        // 5) show/hide overlay
        layoutShipping.setOnClickListener(v -> {
            overlayShipping.setVisibility(View.VISIBLE);
            shippingOptionsLayout.setVisibility(View.VISIBLE);
        });
        btnCloseShipping.setOnClickListener(v -> {
            shippingOptionsLayout.setVisibility(View.GONE);
            overlayShipping.setVisibility(View.GONE);
        });

        overlayShipping.setOnClickListener(v -> {
            shippingOptionsLayout.setVisibility(View.GONE);
            overlayShipping.setVisibility(View.GONE);
        });
        loadShippingInfoFromFirestore(view);

    }


    private void loadAndDisplayVouchers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = new UserSessionManager(requireContext()).getUid();

        db.collection("Customers").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> userVouchers = (List<Map<String, Object>>) doc.get("Voucher");
                    if (userVouchers == null || userVouchers.isEmpty()) {
                        showVoucherAdapter(new ArrayList<>());
                        return;
                    }

                    List<String> voucherIds = new ArrayList<>();
                    for (Map<String, Object> item : userVouchers) {
                        String id = (String) item.get("VoucherId");
                        if (id != null) voucherIds.add(id);
                    }
                    loadVoucherDetails(voucherIds);
                });
    }

    private void loadVoucherDetails(List<String> voucherIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Voucher> loadedVouchers = new ArrayList<>();
        AtomicInteger loaded = new AtomicInteger(0);

        for (String id : voucherIds) {
            db.collection("Voucher").document(id)
                    .get()
                    .addOnSuccessListener(voucherDoc -> {
                        if (voucherDoc.exists()) {
                            Voucher voucher = voucherDoc.toObject(Voucher.class);
                            if (voucher != null) {
                                voucher.setId(voucherDoc.getId());
                                loadedVouchers.add(voucher);
                            }
                        }
                        if (loaded.incrementAndGet() == voucherIds.size()) {
                            showVoucherAdapter(loadedVouchers);
                        }
                    });
        }
    }

    private void showVoucherAdapter(List<Voucher> vouchers) {
        double subtotal = calculateSubtotal();
        VoucherAdapter adapter = new VoucherAdapter(vouchers, subtotal, voucher -> {
            selectedVoucher = voucher;
            txtVoucher.setText("Voucher: " + voucher.getName());
            txtVoucher.setTextColor(getResources().getColor(R.color.color_FF6600));
            updatePaymentDetails();
        }, selectedVoucher);

        recyclerVoucher.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerVoucher.setAdapter(adapter);
    }
    private double calculateSubtotal() {
        double subtotal = 0;
        for (CartProduct p : selectedProducts) {
            subtotal += p.getPrice() * p.getQuantity();
        }
        return subtotal;
    }

    private void updateShippingUI(View view, ShippingAddress addr) {
        TextView n = view.findViewById(R.id.txtCustomerName);
        TextView p = view.findViewById(R.id.txtCustomerPhone);
        TextView a = view.findViewById(R.id.txtCustomerAddress);
        n.setText(addr.getName());
        p.setText(addr.getPhone());
        a.setText(addr.getAddress());
    }
    private void loadShippingInfoFromFirestore(View view) {
        if (selectedShipping != null) {
            updateShippingUI(view, selectedShipping);
            return;  // ← bỏ qua load default
        }
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null || uid.isEmpty()) {
            Log.e("MainPaymentFragment", "User not logged in");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> addressList =
                                (List<Map<String, Object>>) documentSnapshot.get("ShippingAddresses");

                        if (addressList != null && !addressList.isEmpty()) {
                            for (Map<String, Object> addressMap : addressList) {
                                Boolean isDefault = (Boolean) addressMap.get("IsDefault");
                                if (isDefault != null && isDefault) {
                                    String name = (String) addressMap.get("Name");
                                    String phone = (String) addressMap.get("Phone");
                                    String address = (String) addressMap.get("Address");
                                    isDefault = (Boolean) addressMap.get("IsDefault");

                                    selectedShipping = new ShippingAddress(name, phone, address, isDefault != null && isDefault);

                                    TextView txtName = view.findViewById(R.id.txtCustomerName);
                                    TextView txtPhone = view.findViewById(R.id.txtCustomerPhone);
                                    TextView txtAddress = view.findViewById(R.id.txtCustomerAddress);

                                    if (txtName != null) txtName.setText(name != null ? name : "No Name");
                                    if (txtPhone != null) txtPhone.setText(phone != null ? phone : "No Phone");
                                    if (txtAddress != null) txtAddress.setText(address != null ? address : "No Address");

                                    Log.d("MainPaymentFragment", "Loaded default shipping: " + name + " | " + phone + " | " + address);
                                    return; // đã lấy được default → thoát
                                }
                            }
                        } else {
                            Log.w("MainPaymentFragment", "No ShippingAddresses found");
                        }
                    } else {
                        Log.w("MainPaymentFragment", "Customer document not found");
                    }
                })
                .addOnFailureListener(e -> Log.e("MainPaymentFragment", "Error loading shipping address", e));
    }


    private void bindProductsToUI(List<CartProduct> productList) {
        productContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (CartProduct p : productList) {
            // inflate layout cho 1 sản phẩm
            View item = inflater.inflate(R.layout.item_payment_product, productContainer, false);

            // map data vào các view con
            ImageView img = item.findViewById(R.id.imgProduct);
            TextView txtName = item.findViewById(R.id.txtProductTitle);
            TextView txtPrice = item.findViewById(R.id.txtProductPrice);
            TextView txtQty  = item.findViewById(R.id.txtProductQuantity);

            // nếu có URL ảnh, load bằng Glide hoặc Picasso
            // Glide.with(item).load(p.getImageUrl()).into(img);
            String url = p.getFirstImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(this)                   // hoặc Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.mipmap.ic_cart_product)
                        .error(R.mipmap.ic_cart_product)
                        .into(img);
            } else {
                img.setImageResource(R.mipmap.ic_cart_product);
            }
            txtName.setText(p.getName());
            txtPrice.setText("$" + String.format("%,.2f", p.getPrice()));
            txtQty.setText("Quantity: " + p.getQuantity());

            productContainer.addView(item);
        }
    }

    private void bindVoucherToUI(Voucher voucher) {
        TextView txtVoucher = getView().findViewById(R.id.txtVoucher);
        if (voucher != null) {
            selectedVoucher = voucher;
            txtVoucher.setText(voucher.getName() + " -$" + voucher.getDiscountPercent());
        } else {
            txtVoucher.setText("No voucher selected");
        }
        updatePaymentDetails();
    }


    private void resetOption(ViewGroup group, int bgColor) {
        group.setBackgroundColor(bgColor);
        setTextStyleInViewGroup(group, Typeface.NORMAL);
    }

    private void setTextStyleInViewGroup(ViewGroup group, int style) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                Typeface original = tv.getTypeface();
                if (original != null) {
                    tv.setTypeface(Typeface.create(original, style));
                } else {
                    tv.setTypeface(null, style);
                }
            } else if (child instanceof ViewGroup) {
                setTextStyleInViewGroup((ViewGroup) child, style);
            }
        }
    }
    private void updatePaymentDetails() {
        double subtotal = calculateSubtotal();
        double discount = 0;

        if (selectedVoucher != null) {
            double percent = selectedVoucher.getDiscountPercent();
            double max = selectedVoucher.getMaxDiscount();  // thêm dòng này
            discount = subtotal * percent / 100.0;
            if (discount > max) discount = max;  // giới hạn theo maxDiscount
        }

        double total = subtotal + currentShippingFee - discount;

        txtTotalPrice.setText("$" + String.format("%,.2f", subtotal));
        txtShippingFee.setText("$" + String.format("%,.2f", currentShippingFee));
        txtDiscount.setText("-$" + String.format("%,.2f", discount));
        txtTotalValue.setText("$" + String.format("%,.2f", total));
        txtSavingValue.setText(" $" + String.format("%,.2f", discount));

        txtTotalValueBottom.setText(String.format("$%,.2f", total));
        txtSavingValueBottom.setText(String.format("$%,.2f", discount));

        calculatedSaving = discount;
        calculatedTotal = total;

    }


}
