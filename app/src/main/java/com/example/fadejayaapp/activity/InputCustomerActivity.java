package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.Customer;
import com.example.fadejayaapp.model.UploadResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputCustomerActivity extends AppCompatActivity {

    // UI Components
    private EditText etName, etPhone, etAddress, etDiscount;
    private Spinner spMemberType;
    private Button btnSave;
    private TextView tvPageTitle;

    // Variables
    private String action = "add"; // Default add
    private String currentId = "";
    private Customer existingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_customer);

        initViews();
        setupSpinner();

        // Cek Intent (Apakah Mode Edit?)
        if (getIntent().hasExtra("ACTION")) {
            action = getIntent().getStringExtra("ACTION");
        }

        if (action.equals("update") && getIntent().hasExtra("DATA")) {
            existingData = (Customer) getIntent().getSerializableExtra("DATA");
            fillData(); // Isi form dengan data lama

            // Ubah Tampilan jadi Mode Edit
            tvPageTitle.setText("Edit Pelanggan");
            btnSave.setText("UPDATE DATA");
        }

        // Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveData());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDiscount = findViewById(R.id.etDiscount);
        spMemberType = findViewById(R.id.spMemberType);
        btnSave = findViewById(R.id.btnSave);
        tvPageTitle = findViewById(R.id.tvPageTitle);
    }

    private void setupSpinner() {
        // Pilihan sesuai ENUM database: Umum, Reseller, Instansi
        String[] types = {"Umum", "Reseller", "Instansi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spMemberType.setAdapter(adapter);
    }

    private void fillData() {
        if (existingData != null) {
            currentId = existingData.getId();
            etName.setText(existingData.getName());
            etPhone.setText(existingData.getPhone());
            etAddress.setText(existingData.getAddress());

            // Handle Diskon (Hapus .00 jika ada)
            String disc = existingData.getSpecialDiscount();
            if(disc != null) etDiscount.setText(disc.replace(".00", ""));
            else etDiscount.setText("0");

            // Handle Spinner Selection
            String type = existingData.getMemberType();
            if (type != null) {
                if (type.equalsIgnoreCase("Umum")) spMemberType.setSelection(0);
                else if (type.equalsIgnoreCase("Reseller")) spMemberType.setSelection(1);
                else if (type.equalsIgnoreCase("Instansi")) spMemberType.setSelection(2);
            }
        }
    }

    private void saveData() {
        // 1. Ambil Input
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String discount = etDiscount.getText().toString().trim();
        String type = spMemberType.getSelectedItem().toString();

        // 2. Validasi Sederhana
        if (name.isEmpty()) {
            etName.setError("Nama wajib diisi!");
            return;
        }
        if (discount.isEmpty()) discount = "0";

        // 3. Matikan Tombol biar ga double klik
        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        // 4. Panggil API
        ApiService api = ApiClient.getService();
        Call<UploadResponse> call = api.saveCustomer(
                action,     // "add" atau "update"
                currentId,  // ID (kosong jika add)
                name,
                phone,
                address,
                type,
                discount
        );

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText(action.equals("add") ? "SIMPAN DATA" : "UPDATE DATA");

                if (response.isSuccessful() && response.body() != null) {

                    // PERBAIKAN DI SINI:
                    // Ganti .isSuccess() menjadi pengecekan .getStatus()
                    String status = response.body().getStatus();

                    if (status != null && status.equals("success")) {
                        Toast.makeText(InputCustomerActivity.this, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(InputCustomerActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(InputCustomerActivity.this, "Respon Server Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(action.equals("add") ? "SIMPAN DATA" : "UPDATE DATA");
                Toast.makeText(InputCustomerActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}