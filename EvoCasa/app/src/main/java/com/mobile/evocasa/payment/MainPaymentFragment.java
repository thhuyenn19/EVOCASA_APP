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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_payment, container, false);
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

        // Chuyển sang fragment chọn phương thức thanh toán
        TextView txtSeeAllPayment = view.findViewById(R.id.txtSeeAllPayment);
        txtSeeAllPayment.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new PaymentMethodFragment());
            transaction.addToBackStack(null);
            transaction.commit();
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
            Intent checkoutIntent = new Intent(requireContext(), NarBarActivity.class);
            intent.putExtra("tab_pos", 4);
            startActivity(intent);
            requireActivity().finish();
        });

        // Ánh xạ các lựa chọn thanh toán
        LinearLayout optionCOD = view.findViewById(R.id.optionCOD);
        LinearLayout optionBanking = view.findViewById(R.id.optionBanking);
        LinearLayout optionMomo = view.findViewById(R.id.optionMomo);
        LinearLayout optionCredit = view.findViewById(R.id.optionCredit);

        int selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        int defaultColor = getResources().getColor(android.R.color.transparent, null);

        // Xử lý click chọn phương thức thanh toán
        View.OnClickListener paymentClickListener = v -> {
            if (currentSelectedOption == v) {
                // Nhấn lại để bỏ chọn
                resetOption((ViewGroup) currentSelectedOption, defaultColor);
                currentSelectedOption = null;
            } else {
                // Reset lựa chọn cũ nếu có
                if (currentSelectedOption != null) {
                    resetOption((ViewGroup) currentSelectedOption, defaultColor);
                }

                // Chọn mới
                currentSelectedOption = v;
                v.setBackgroundColor(selectedColor);
                setTextStyleInViewGroup((ViewGroup) v, Typeface.BOLD);
            }
        };

        optionCOD.setOnClickListener(paymentClickListener);
        optionBanking.setOnClickListener(paymentClickListener);
        optionMomo.setOnClickListener(paymentClickListener);
        optionCredit.setOnClickListener(paymentClickListener);

        // Reset font và background ban đầu để tránh bị bold sẵn
        resetOption(optionCOD, defaultColor);
        resetOption(optionBanking, defaultColor);
        resetOption(optionMomo, defaultColor);
        resetOption(optionCredit, defaultColor);

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



        setupPaymentMethodSelector(view);
        return view;
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
        }
        // 2) chuẩn bị dữ liệu
        List<ShippingMethod> shippingList = Arrays.asList(
                new ShippingMethod(
                        "Express Delivery", 50,
                        "Received on Thursday, May 15, 2025",
                        R.drawable.ic_delivery),
                new ShippingMethod(
                        "Standard Delivery", 20,
                        "Received on Monday, May 19, 2025",
                        R.drawable.ic_delivery),
                new ShippingMethod(
                        "Weekend Delivery", 30,
                        "Received on Saturday, May 18, 2025",
                        R.drawable.ic_delivery)
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
            // ẩn overlay
            shippingOptionsLayout.setVisibility(View.GONE);
            overlayShipping.setVisibility(View.GONE);
        });

        // 4) default hiển thị mục đầu tiên
        ShippingMethod def = shippingList.get(0);
        txtName.setText(def.getName());
        txtFee .setText("$" + (int)def.getPrice());
        txtDesc.setText(def.getReceiveOn());

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

    private void setupPaymentMethodSelector(View view) {
        LinearLayout optionCOD = view.findViewById(R.id.optionCOD);
        LinearLayout optionBanking = view.findViewById(R.id.optionBanking);
        LinearLayout optionMomo = view.findViewById(R.id.optionMomo);
        LinearLayout optionCredit = view.findViewById(R.id.optionCredit);

        int selectedColor = getResources().getColor(R.color.color_80F2EAD3, null);
        int defaultColor = getResources().getColor(android.R.color.transparent, null);

        View.OnClickListener listener = v -> {
            if (currentSelectedOption == v) {
                resetOption((ViewGroup) currentSelectedOption, defaultColor);
                currentSelectedOption = null;
            } else {
                if (currentSelectedOption != null) {
                    resetOption((ViewGroup) currentSelectedOption, defaultColor);
                }
                currentSelectedOption = v;
                v.setBackgroundColor(selectedColor);
                setTextStyleInViewGroup((ViewGroup) v, Typeface.BOLD);
            }
        };

        optionCOD.setOnClickListener(listener);
        optionBanking.setOnClickListener(listener);
        optionMomo.setOnClickListener(listener);
        optionCredit.setOnClickListener(listener);

        resetOption(optionCOD, defaultColor);
        resetOption(optionBanking, defaultColor);
        resetOption(optionMomo, defaultColor);
        resetOption(optionCredit, defaultColor);
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
            txtVoucher.setText(voucher.getName() + " -$" + voucher.getDiscountPercent());
        } else {
            txtVoucher.setText("No voucher selected");
        }
    }


    private void resetOption(ViewGroup group, int backgroundColor) {
        group.setBackgroundColor(backgroundColor);
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

}
