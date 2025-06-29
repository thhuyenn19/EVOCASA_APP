package com.thanhhuyen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.thanhhuyen.evocasaadmin.CustomerDetailActivity;
import com.thanhhuyen.evocasaadmin.R;
import com.thanhhuyen.models.Customer;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private List<Customer> customerList;
    private Context context;

    public CustomerAdapter(List<Customer> customerList) {
        this.customerList = customerList;
    }

    @Override
    public CustomerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.txtCustomerId.setText("ID: " + customer.getId());
        holder.txtCustomerName.setText(customer.getName());
        holder.txtCustomerGender.setText("Gender: " + customer.getGender());
        holder.txtCustomerMail.setText("Mail: " + customer.getMail());
        holder.txtCustomerPhone.setText("Phone: " + customer.getPhone());
        holder.txtCustomerDob.setText("DOB: " + customer.getDob());

        View.OnClickListener viewDetailClick = v -> {
            Intent intent = new Intent(context, CustomerDetailActivity.class);
            intent.putExtra("customerId", customer.getId());
            context.startActivity(intent);
        };

        holder.btnView.setOnClickListener(viewDetailClick);
        holder.txtViewLabel.setOnClickListener(viewDetailClick);
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView txtCustomerId, txtCustomerName, txtCustomerGender, txtViewLabel,
                txtCustomerMail, txtCustomerPhone, txtCustomerDob;
        ImageView btnView;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            txtCustomerId = itemView.findViewById(R.id.txtCustomerId);
            txtCustomerName = itemView.findViewById(R.id.txtCustomerName);
            txtCustomerGender = itemView.findViewById(R.id.txtCustomerGender);
            txtCustomerMail = itemView.findViewById(R.id.txtCustomerMail);
            txtCustomerPhone = itemView.findViewById(R.id.txtCustomerPhone);
            txtCustomerDob = itemView.findViewById(R.id.txtCustomerDob);
            txtViewLabel = itemView.findViewById(R.id.txtViewLabel);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
