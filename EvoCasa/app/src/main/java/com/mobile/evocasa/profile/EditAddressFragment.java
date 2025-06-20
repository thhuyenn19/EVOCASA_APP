// EditAddressFragment.java (Fixed version)
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.utils.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditAddressFragment extends Fragment {

    private Spinner spinnerCountry, spinnerProvince, spinnerDistrict, spinnerWard;
    private Button btnSave;
    private RequestQueue requestQueue;
    private EditText edtStreet;
    private ImageView imgProfileDetailsBack;
    private UserSessionManager sessionManager;

    private String selectedCountry = "";
    private String selectedProvince = "";
    private String selectedDistrict = "";
    private String selectedWard = "";
    private String selectedStreet = "";

    // Store province and district IDs for API calls
    private int selectedProvinceId = -1;
    private int selectedDistrictId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("EditShippingAddress", "onViewCreated called");

        // Initialize views
        sessionManager = new UserSessionManager(requireContext());
        spinnerCountry = view.findViewById(R.id.spinnerCountry);
        spinnerProvince = view.findViewById(R.id.spinnerProvince);
        spinnerDistrict = view.findViewById(R.id.spinnerDistrict);
        spinnerWard = view.findViewById(R.id.spinnerWard);
        edtStreet = view.findViewById(R.id.edtStreet);
        btnSave = view.findViewById(R.id.btnSave);
        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
        requestQueue = Volley.newRequestQueue(requireContext());

        // Parse current address
        String currentAddress = getArguments() != null ? getArguments().getString("currentShippingAddress", "") : "";
        Log.d("EditAddress", "Received address: " + currentAddress);

        String[] parts = currentAddress.split(", ");
        Log.d("EditAddress", "Split parts count: " + parts.length);
        for (int i = 0; i < parts.length; i++) {
            Log.d("EditAddress", "parts[" + i + "]: " + parts[i]);
        }

        if (parts.length >= 5) {
            selectedStreet = parts[0];
            selectedWard = parts[1];
            selectedDistrict = parts[2];
            selectedProvince = parts[3];
            selectedCountry = "Vietnam"; // Default to Vietnam

            Log.d("EditAddress", "Parsed selectedStreet: " + selectedStreet);
            Log.d("EditAddress", "Parsed selectedWard: " + selectedWard);
            Log.d("EditAddress", "Parsed selectedDistrict: " + selectedDistrict);
            Log.d("EditAddress", "Parsed selectedProvince: " + selectedProvince);
            Log.d("EditAddress", "Parsed selectedCountry: " + selectedCountry);
        } else {
            Log.w("EditAddress", "Invalid address format, not enough parts.");
        }

        edtStreet.setText(selectedStreet);

        // Initialize spinners
        setupCountrySpinner();

        btnSave.setOnClickListener(v -> {
            saveUpdatedLocationAddress();
        });

        if (imgProfileDetailsBack != null) {
            imgProfileDetailsBack.setOnClickListener(v -> {
                showExitDialog();
            });
        }
    }

    private void showExitDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.custom_exit_dialog);
        dialog.setCancelable(true);

        Button btnExit = dialog.findViewById(R.id.btn_exit);
        Button btnSaveInDialog = dialog.findViewById(R.id.btn_save);
        ImageView btnExitIcon = dialog.findViewById(R.id.btn_close_icon);

        btnExit.setOnClickListener(confirmView -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            dialog.dismiss();
        });

        btnSaveInDialog.setOnClickListener(saveView -> {
            saveUpdatedLocationAddress();
            dialog.dismiss();
        });

        btnExitIcon.setOnClickListener(xView -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void saveUpdatedLocationAddress() {
        String street = edtStreet.getText().toString().trim();
        if (street.isEmpty() || selectedWard.isEmpty() || selectedDistrict.isEmpty() || selectedProvince.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all address fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullAddress = street + ", " + selectedWard + ", " + selectedDistrict + ", " + selectedProvince + ", " + selectedCountry;
        Bundle result = new Bundle();
        result.putString("selectedAddress", fullAddress);
        getParentFragmentManager().setFragmentResult("shippingAddressUpdated", result);
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
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(), android.R.layout.simple_spinner_item, countryNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCountry.setAdapter(adapter);

                        // Set default selection
                        String defaultCountry = selectedCountry.isEmpty() ? "Vietnam" : selectedCountry;
                        int defaultIndex = countryNames.indexOf(defaultCountry);
                        if (defaultIndex != -1) {
                            spinnerCountry.setSelection(defaultIndex);
                            selectedCountry = defaultCountry;
                            Log.d("EditLocation", "Default country selected: " + selectedCountry);

                            // Load provinces for selected country
                            if (selectedCountry.equalsIgnoreCase("Vietnam")) {
                                loadVietnamProvinces();
                            }
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
                                    // Clear other spinners for non-Vietnam countries
                                    clearSpinners();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (JSONException e) {
                        Log.e("EditLocation", "JSON parsing error", e);
                        Toast.makeText(getContext(), "Failed to load countries", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EditLocation", "Failed to fetch country list", error);
                    Toast.makeText(getContext(), "Failed to load countries", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void clearSpinners() {
        List<String> placeholder = Arrays.asList("Select...");

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, placeholder);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, placeholder);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, placeholder);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);
    }

    private void loadVietnamProvinces() {
        Log.d("EditLocation", "Loading provinces for Vietnam...");
        String url = "https://provinces.open-api.vn/api/?depth=1";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<String> provinceNames = new ArrayList<>();
                    Map<String, Integer> provinceNameToId = new HashMap<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject province = response.getJSONObject(i);
                            String name = province.getString("name");
                            int id = province.getInt("code");
                            provinceNames.add(name);
                            provinceNameToId.put(name, id);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, provinceNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProvince.setAdapter(adapter);

                        // Set default selection if available
                        int defaultIndex = provinceNames.indexOf(selectedProvince);
                        if (defaultIndex != -1) {
                            spinnerProvince.setSelection(defaultIndex);
                            selectedProvinceId = provinceNameToId.get(selectedProvince);
                            Log.d("EditLocation", "Default province selected: " + selectedProvince + " (ID: " + selectedProvinceId + ")");
                            loadDistricts(selectedProvinceId);
                        } else {
                            Log.w("EditLocation", "Province not found in list: " + selectedProvince);
                        }

                        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedProvince = parent.getItemAtPosition(position).toString();
                                selectedProvinceId = provinceNameToId.get(selectedProvince);
                                Log.d("EditLocation", "Province selected: " + selectedProvince + " (ID: " + selectedProvinceId + ")");
                                loadDistricts(selectedProvinceId);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (JSONException e) {
                        Log.e("EditLocation", "Failed to parse provinces JSON", e);
                        Toast.makeText(getContext(), "Failed to load provinces", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EditLocation", "Failed to fetch provinces", error);
                    Toast.makeText(getContext(), "Failed to load provinces", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void loadDistricts(int provinceId) {
        if (provinceId == -1) {
            return;
        }

        Log.d("EditLocation", "Loading districts for province ID: " + provinceId);
        String url = "https://provinces.open-api.vn/api/p/" + provinceId + "?depth=2";

        // Sử dụng JsonObjectRequest thay vì JsonArrayRequest
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Bây giờ response đã là JSONObject rồi
                        JSONArray districts = response.getJSONArray("districts");
                        List<String> districtNames = new ArrayList<>();
                        Map<String, Integer> districtNameToId = new HashMap<>();

                        for (int i = 0; i < districts.length(); i++) {
                            JSONObject district = districts.getJSONObject(i);
                            String name = district.getString("name");
                            int id = district.getInt("code");
                            districtNames.add(name);
                            districtNameToId.put(name, id);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, districtNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDistrict.setAdapter(adapter);

                        // Set default selection if available
                        int defaultIndex = districtNames.indexOf(selectedDistrict);
                        if (defaultIndex != -1) {
                            spinnerDistrict.setSelection(defaultIndex);
                            selectedDistrictId = districtNameToId.get(selectedDistrict);
                            Log.d("EditLocation", "Default district selected: " + selectedDistrict + " (ID: " + selectedDistrictId + ")");
                            loadWards(selectedDistrictId);
                        }

                        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedDistrict = parent.getItemAtPosition(position).toString();
                                selectedDistrictId = districtNameToId.get(selectedDistrict);
                                Log.d("EditLocation", "District selected: " + selectedDistrict + " (ID: " + selectedDistrictId + ")");
                                loadWards(selectedDistrictId);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (JSONException e) {
                        Log.e("EditAddress", "Error parsing district data", e);
                        Toast.makeText(getContext(), "Failed to load districts", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EditAddress", "Failed to load districts", error);
                    Toast.makeText(getContext(), "Failed to load districts", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void loadWards(int districtId) {
        if (districtId == -1) {
            return;
        }

        Log.d("EditLocation", "Loading wards for district ID: " + districtId);
        String url = "https://provinces.open-api.vn/api/d/" + districtId + "?depth=2";

        // Sử dụng JsonObjectRequest thay vì JsonArrayRequest
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Bây giờ response đã là JSONObject rồi
                        JSONArray wards = response.getJSONArray("wards");
                        List<String> wardNames = new ArrayList<>();

                        for (int i = 0; i < wards.length(); i++) {
                            JSONObject ward = wards.getJSONObject(i);
                            wardNames.add(ward.getString("name"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, wardNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerWard.setAdapter(adapter);

                        // Set default selection if available
                        int defaultIndex = wardNames.indexOf(selectedWard);
                        if (defaultIndex != -1) {
                            spinnerWard.setSelection(defaultIndex);
                            Log.d("EditLocation", "Default ward selected: " + selectedWard);
                        }

                        spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedWard = parent.getItemAtPosition(position).toString();
                                Log.d("EditLocation", "Ward selected: " + selectedWard);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                    } catch (JSONException e) {
                        Log.e("EditAddress", "Error parsing ward data", e);
                        Toast.makeText(getContext(), "Failed to load wards", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EditAddress", "Failed to load wards", error);
                    Toast.makeText(getContext(), "Failed to load wards", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    // Method for loading provinces for other countries (placeholder)
    private void loadProvincesForOtherCountry(String country) {
        Log.d("EditLocation", "Loading regions for country: " + country);
        // This is a placeholder - you would need to implement actual API calls for other countries
        List<String> placeholder = Arrays.asList("Not supported yet");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, placeholder);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapter);
    }
}