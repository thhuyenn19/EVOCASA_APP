package com.thanhhuyen.evocasaadmin;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.thanhhuyen.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EditProductFragment extends DialogFragment {
    private static final String TAG = "EditProductFragment";
    private static final String ARG_PRODUCT_ID = "product_id";

    private FirebaseManager firebaseManager;
    private Product currentProduct;
    private OnProductUpdateListener updateListener;

    private ImageView productImage;
    private TextView productName;
    private TextInputLayout priceInputLayout;
    private TextInputEditText priceInput;
    private TextInputLayout quantityInputLayout;
    private TextInputEditText quantityInput;
    private Button saveButton;
    private Button cancelButton;

    public interface OnProductUpdateListener {
        void onProductUpdated(double newPrice, int newQuantity);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            updateListener = (OnProductUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnProductUpdateListener");
        }
    }

    public static EditProductFragment newInstance(String productId) {
        EditProductFragment fragment = new EditProductFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        firebaseManager = FirebaseManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_product, container, false);
        initializeViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String productId = getArguments().getString(ARG_PRODUCT_ID);
        if (productId != null) {
            loadProductDetails(productId);
        }
        setupListeners();
    }

    private void initializeViews(View view) {
        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        priceInputLayout = view.findViewById(R.id.priceInputLayout);
        priceInput = view.findViewById(R.id.priceInput);
        quantityInputLayout = view.findViewById(R.id.quantityInputLayout);
        quantityInput = view.findViewById(R.id.quantityInput);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Set up price input formatter
        priceInput.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    priceInput.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,]", "");
                    double parsed = Double.parseDouble(cleanString.isEmpty() ? "0" : cleanString);
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                    currencyFormat.setMaximumFractionDigits(0);
                    String formatted = currencyFormat.format(parsed);

                    current = formatted;
                    priceInput.setText(formatted);
                    priceInput.setSelection(formatted.length());

                    priceInput.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveChanges());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void loadProductDetails(String productId) {
        firebaseManager.getProductById(productId, new FirebaseManager.OnProductLoadedListener() {
            @Override
            public void onProductLoaded(Product product) {
                if (product != null) {
                    currentProduct = product;
                    updateUI(product);
                } else {
                    Toast.makeText(getContext(), "Error: Product not found", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading product: " + error, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    private void updateUI(Product product) {
        productName.setText(product.getName());
        
        // Format and set price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormat.setMaximumFractionDigits(0);
        priceInput.setText(currencyFormat.format(product.getPrice()));
        
        // Set quantity
        quantityInput.setText(String.valueOf(product.getQuantity()));

        // Load product image
        List<String> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            Glide.with(this)
                    .load(images.get(0))
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(productImage);
        }
    }

    private void saveChanges() {
        if (currentProduct == null) return;

        // Validate inputs
        String priceStr = priceInput.getText().toString().replaceAll("[$,]", "");
        String quantityStr = quantityInput.getText().toString();

        if (priceStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double newPrice = Double.parseDouble(priceStr);
            int newQuantity = Integer.parseInt(quantityStr);

            // Update product
            currentProduct.setPrice(newPrice);
            currentProduct.setQuantity(newQuantity);

            // Save to Firebase
            firebaseManager.updateProduct(currentProduct, new FirebaseManager.OnProductUpdateListener() {
                @Override
                public void onSuccess() {
                    // Notify activity about the update
                    if (updateListener != null) {
                        updateListener.onProductUpdated(newPrice, newQuantity);
                    }
                    Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error updating product: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price or quantity format", Toast.LENGTH_SHORT).show();
        }
    }
} 