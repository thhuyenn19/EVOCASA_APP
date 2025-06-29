package com.thanhhuyen.evocasaadmin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thanhhuyen.adapters.CustomerAdapter;
import com.thanhhuyen.models.Customer;
import com.thanhhuyen.untils.FontUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerManagementActivity extends AppCompatActivity {

    private RecyclerView customerRecyclerView;
    private CustomerAdapter adapter;
    private List<Customer> customerList;
    private TextView txtTitle, txtTotalCustomersValue,
            txtTotalCustomersTitle, txtTotalCustomersPercent, txtNewCustomersValue,
            txtNewCustomersTitle, txtNewCustomersPercent, txtTotalOrdersValue,
            txtTotalOrdersTitle, txtTotalOrdersPercent;
    private ImageView imgBack;
    private EditText edtSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_management);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        initViews();
    }

    private void initViews() {
        // Back button trong topbar
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) imgBack.setOnClickListener(v -> finish());

        // Font setup
        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }
        edtSearch = findViewById(R.id.edtSearch);
         if (edtSearch != null) {
             FontUtils.setRegularFont(this, edtSearch);
         }edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            FontUtils.setRegularFont(this, edtSearch);
        }

        TextView txtTotalCustomersValue = findViewById(R.id.txtTotalCustomersValue);
        if (txtTotalCustomersValue != null) {
            FontUtils.setBoldFont(this, txtTotalCustomersValue);
        }

        TextView txtTotalCustomersTitle = findViewById(R.id.txtTotalCustomersTitle);
        if (txtTotalCustomersTitle != null) {
            FontUtils.setRegularFont(this, txtTotalCustomersTitle);
        }

        TextView txtTotalCustomersPercent = findViewById(R.id.txtTotalCustomersPercent);
        if (txtTotalCustomersPercent != null) {
            FontUtils.setBoldFont(this, txtTotalCustomersPercent);
        }

        TextView txtNewCustomersValue = findViewById(R.id.txtNewCustomersValue);
        if (txtNewCustomersValue != null) {
            FontUtils.setBoldFont(this, txtNewCustomersValue);
        }

        TextView txtNewCustomersTitle = findViewById(R.id.txtNewCustomersTitle);
        if (txtNewCustomersTitle != null) {
            FontUtils.setRegularFont(this, txtNewCustomersTitle);
        }

        TextView txtNewCustomersPercent = findViewById(R.id.txtNewCustomersPercent);
        if (txtNewCustomersPercent != null) {
            FontUtils.setBoldFont(this, txtNewCustomersPercent);
        }

//        TextView txtTotalOrdersValue = findViewById(R.id.txtTotalOrdersValue);
//        if (txtTotalOrdersValue != null) {
//            FontUtils.setBoldFont(this, txtTotalOrdersValue);
//        }
//
//        TextView txtTotalOrdersTitle = findViewById(R.id.txtTotalOrdersTitle);
//        if (txtTotalOrdersTitle != null) {
//            FontUtils.setRegularFont(this, txtTotalOrdersTitle);
//        }
//
//        TextView txtTotalOrdersPercent = findViewById(R.id.txtTotalOrdersPercent);
//        if (txtTotalOrdersPercent != null) {
//            FontUtils.setBoldFont(this, txtTotalOrdersPercent);
//        }

        customerRecyclerView = findViewById(R.id.customerRecyclerView);
        customerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        customerList = new ArrayList<>();
        adapter = new CustomerAdapter(customerList);
        customerRecyclerView.setAdapter(adapter);

        loadCustomers();

    }

    private void loadCustomers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Customers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        customerList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("Name");
                            String gender = document.getString("Gender");
                            String mail = document.getString("Mail");
                            String phone = document.getString("Phone");

                            String dob = "";
                            if (document.get("DOB") instanceof Map) {
                                Map<String, Object> dobMap = (Map<String, Object>) document.get("DOB");
                                dob = (String) dobMap.get("$date");
                            }

                            customerList.add(new Customer(id, name, gender, mail, phone, dob));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error getting customers", task.getException());
                    }
                });
    }

}