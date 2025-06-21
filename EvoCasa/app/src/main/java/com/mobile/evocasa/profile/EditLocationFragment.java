// EditLocationFragment.java
package com.mobile.evocasa.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.SpinnerCustomAdapter;
import com.mobile.evocasa.R;
import com.mobile.utils.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditLocationFragment extends Fragment {

    private Spinner spinnerCountry, spinnerProvince;
    private Button btnSave;
    private RequestQueue requestQueue;
    private ImageView imgProfileDetailsBack;
    private UserSessionManager sessionManager;


    private String selectedCountry = "";
    private String selectedProvince = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("EditLocation", "onViewCreated called");
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        spinnerCountry = view.findViewById(R.id.spinnerCountry);
        spinnerProvince = view.findViewById(R.id.spinnerProvince);
        btnSave = view.findViewById(R.id.btnSave);
        requestQueue = Volley.newRequestQueue(requireContext());

        String currentAddress = getArguments() != null ? getArguments().getString("currentAddress", "") : "";
        Log.d("EditLocation", "Received currentAddress: " + currentAddress);

        String[] parts = currentAddress.split(", ");
        selectedProvince = parts.length >= 1 ? parts[0].trim() : "";
        selectedCountry = parts.length >= 2 ? parts[parts.length - 1].trim() : "";

        // Normalize Vietnam spelling
        if (selectedCountry.equalsIgnoreCase("Viet Nam")) {
            selectedCountry = "Vietnam";
            loadVietnamProvinces();
        }
        else {
            loadProvincesForOtherCountry(selectedCountry); // Thêm dòng này
        }

        Log.d("EditLocation", "Parsed selectedProvince: " + selectedProvince);
        Log.d("EditLocation", "Parsed selectedCountry: " + selectedCountry);

        setupCountrySpinner();

        btnSave.setOnClickListener(v -> saveUpdatedLocation());
        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
        imgProfileDetailsBack.setOnClickListener(v -> {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.custom_exit_dialog);
            dialog.setCancelable(true);

            Button btnExit = dialog.findViewById(R.id.btn_exit);
            Button btnSaveInDialog = dialog.findViewById(R.id.btn_save); // đổi tên tránh trùng với btnSave ở trên
            ImageView btnExitIcon = dialog.findViewById(R.id.btn_close_icon);

            // Nhấn EXIT: quay lại mà không cập nhật
            btnExit.setOnClickListener(confirmView -> {
                requireActivity().getSupportFragmentManager().popBackStack();
                dialog.dismiss();
            });

            // Nhấn SAVE: cập nhật rồi quay lại
            btnSaveInDialog.setOnClickListener(saveView -> {
                saveUpdatedLocation(); // tự popBackStack
                dialog.dismiss();
            });

            // Nhấn icon X: chỉ đóng dialog
            btnExitIcon.setOnClickListener(xView -> {
                dialog.dismiss();
            });

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        });
    }

    private void saveUpdatedLocation() {
        String fullAddress = selectedProvince + ", " + selectedCountry;
        Log.d("EditLocation", "Sending back address: " + fullAddress);

        Bundle result = new Bundle();
        result.putString("selectedAddress", fullAddress);
        getParentFragmentManager().setFragmentResult("addressUpdated", result);

        requireActivity().getSupportFragmentManager().popBackStack();
    }


    private void setupCountrySpinner() {
        Log.d("EditLocation", "Fetching country list from API...");
        String url = "https://restcountries.com/v2/all?fields=name";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<String> countryNames = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject country = response.getJSONObject(i);
                            String name = country.getString("name");
                            countryNames.add(name);
                        }

                        countryNames.sort(String::compareTo);
                        SpinnerCustomAdapter adapter = new SpinnerCustomAdapter(requireContext(), countryNames);
                        spinnerCountry.setAdapter(adapter);

                        int defaultIndex = countryNames.indexOf(selectedCountry);
                        if (defaultIndex != -1) {
                            spinnerCountry.setSelection(defaultIndex);
                            Log.d("EditLocation", "Default country selected: " + selectedCountry);
                        } else {
                            Log.d("EditLocation", "Country not found: " + selectedCountry);
                        }

                        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedCountry = parent.getItemAtPosition(position).toString();
                                if (selectedCountry.equalsIgnoreCase("Vietnam")) {
                                    loadVietnamProvinces();
                                } else {
                                    List<String> placeholder = Arrays.asList("-");
                                    ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(requireContext(),
                                            android.R.layout.simple_spinner_item, placeholder);
                                    spinnerProvince.setAdapter(provinceAdapter);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (JSONException e) {
                        Log.e("EditLocation", "JSON parsing error", e);
                    }
                },
                error -> Log.e("EditLocation", "Failed to fetch country list", error));

        requestQueue.add(request);
    }

    private void loadVietnamProvinces() {
        Log.d("EditLocation", "Loading provinces for Vietnam...");
        String url = "https://provinces.open-api.vn/api/?depth=1";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<String> provinceNames = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject province = response.getJSONObject(i);
                            provinceNames.add(province.getString("name"));
                        }

                        SpinnerCustomAdapter adapter = new SpinnerCustomAdapter(requireContext(), provinceNames);
                        spinnerProvince.setAdapter(adapter);

                        int defaultIndex = provinceNames.indexOf(selectedProvince);
                        if (defaultIndex != -1) {
                            spinnerProvince.setSelection(defaultIndex);
                            Log.d("EditLocation", "Default province selected: " + selectedProvince);
                        } else {
                            Log.d("EditLocation", "Province not found: " + selectedProvince);
                        }

                        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedProvince = parent.getItemAtPosition(position).toString();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (JSONException e) {
                        Log.e("EditLocation", "Failed to parse provinces JSON", e);
                    }
                },
                error -> Log.e("EditLocation", "Failed to fetch provinces", error));

        requestQueue.add(request);
    }

    private void loadProvincesForOtherCountry(String country) {
        Log.d("EditLocation", "Loading regions for country: " + country);
        String url = "https://your-api.com/regions?country=" + country; // Replace with your real API

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<String> regions = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            regions.add(obj.getString("regionName")); // Tuỳ API
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, regions);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProvince.setAdapter(adapter);

                        int defaultIndex = regions.indexOf(selectedProvince);
                        if (defaultIndex != -1) {
                            spinnerProvince.setSelection(defaultIndex);
                            Log.d("EditLocation", "Default region selected: " + selectedProvince);
                        }
                    } catch (JSONException e) {
                        Log.e("EditLocation", "Failed to parse region JSON", e);
                    }
                },
                error -> Log.e("EditLocation", "Failed to fetch region list", error)
        );

        requestQueue.add(request);
    }

}
