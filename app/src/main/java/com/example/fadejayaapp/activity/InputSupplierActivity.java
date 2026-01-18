package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.Supplier;
import com.example.fadejayaapp.model.UploadResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputSupplierActivity extends AppCompatActivity {

    // UI Components
    private EditText etStoreName, etContact, etPhone, etAddress, etDesc;
    private Button btnSave;
    private TextView tvPageTitle;

    // Variables
    private String action = "add";
    private String currentId = "";
    private Supplier existingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_supplier);

        initViews();

        // Cek Intent (Apakah Mode Edit atau Tambah?)
        if (getIntent().hasExtra("ACTION")) {
            action = getIntent().getStringExtra("ACTION");
        }

        if (action.equals("update") && getIntent().hasExtra("DATA")) {
            existingData = (Supplier) getIntent().getSerializableExtra("DATA");
            fillData(); // Isi form dengan data lama

            // Ubah Tampilan jadi Mode Edit
            tvPageTitle.setText("Edit Supplier");
            btnSave.setText("UPDATE DATA");
        }

        // Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveData());
    }

    private void initViews() {
        etStoreName = findViewById(R.id.etStoreName);
        etContact = findViewById(R.id.etContact);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDesc = findViewById(R.id.etDesc);
        btnSave = findViewById(R.id.btnSave);
        tvPageTitle = findViewById(R.id.tvPageTitle);
    }

    private void fillData() {
        if (existingData != null) {
            currentId = existingData.getId();
            etStoreName.setText(existingData.getStoreName());
            etContact.setText(existingData.getContactPerson());
            etPhone.setText(existingData.getPhone());
            etAddress.setText(existingData.getAddress());
            etDesc.setText(existingData.getGoodsDescription());
        }
    }

    private void saveData() {
        // 1. Ambil Input
        String storeName = etStoreName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        // 2. Validasi Sederhana
        if (storeName.isEmpty()) {
            etStoreName.setError("Nama Toko wajib diisi!");
            return;
        }

        // 3. Matikan Tombol
        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        // 4. Panggil API
        ApiService api = ApiClient.getService();
        Call<UploadResponse> call = api.saveSupplier(
                action,     // "add" atau "update"
                currentId,  // ID (kosong jika add)
                storeName,
                contact,
                phone,
                address,
                desc
        );

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText(action.equals("add") ? "SIMPAN DATA" : "UPDATE DATA");

                if (response.isSuccessful() && response.body() != null) {
                    // Cek status dari response PHP
                    String status = response.body().getStatus();

                    if (status != null && status.equals("success")) {
                        Toast.makeText(InputSupplierActivity.this, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke list
                    } else {
                        Toast.makeText(InputSupplierActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputSupplierActivity.this, "Respon Server Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText(action.equals("add") ? "SIMPAN DATA" : "UPDATE DATA");
                Toast.makeText(InputSupplierActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}