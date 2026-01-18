package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputEmployeeActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etPassword;
    private Spinner spRole;
    private SwitchCompat switchStatus;
    private Button btnSave;
    private TextView tvPageTitle, tvPassHint;

    private String action = "add";
    private String currentId = "";
    private User existingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_employee);

        initViews();
        setupSpinner();

        // Cek Intent Mode
        if (getIntent().hasExtra("ACTION")) {
            action = getIntent().getStringExtra("ACTION");
        }

        if (action.equals("update") && getIntent().hasExtra("DATA")) {
            existingData = (User) getIntent().getSerializableExtra("DATA");
            fillData();

            // Mode Edit
            tvPageTitle.setText("Edit Karyawan");
            btnSave.setText("UPDATE DATA");
            tvPassHint.setVisibility(View.VISIBLE); // Tampilkan hint password opsional
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveData());
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        switchStatus = findViewById(R.id.switchStatus);
        btnSave = findViewById(R.id.btnSave);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvPassHint = findViewById(R.id.tvPassHint);
    }

    private void setupSpinner() {
        // Role harus sesuai Enum Database
        String[] roles = {"Owner", "Admin", "Kasir"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spRole.setAdapter(adapter);
    }

    private void fillData() {
        if (existingData != null) {
            currentId = existingData.getId();
            etFullName.setText(existingData.getFullName());
            etUsername.setText(existingData.getUsername());
            etPassword.setText(""); // Password dikosongkan (biar aman)

            // Set Status Switch
            switchStatus.setChecked(existingData.getIsActive().equals("1"));

            // Set Spinner Selection
            String role = existingData.getRole();
            if (role != null) {
                if(role.equalsIgnoreCase("Owner")) spRole.setSelection(0);
                else if(role.equalsIgnoreCase("Admin")) spRole.setSelection(1);
                else if(role.equalsIgnoreCase("Kasir")) spRole.setSelection(2);
            }
        }
    }

    private void saveData() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();
        String isActive = switchStatus.isChecked() ? "1" : "0";

        // Validasi
        if (fullName.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Nama dan Username wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Jika Tambah Baru, Password Wajib
        if (action.equals("add") && password.isEmpty()) {
            etPassword.setError("Password wajib diisi untuk user baru!");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Proses...");

        ApiService api = ApiClient.getService();
        Call<UploadResponse> call = api.saveUser(
                action,
                currentId,
                username,
                fullName,
                role,
                isActive,
                password // Password dikirim (kosong jika edit & tidak diganti)
        );

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText(action.equals("add") ? "SIMPAN DATA USER" : "UPDATE DATA");

                if(response.isSuccessful() && response.body() != null) {
                    if("success".equals(response.body().getStatus())) {
                        Toast.makeText(InputEmployeeActivity.this, "Berhasil!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(InputEmployeeActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputEmployeeActivity.this, "Error Server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(InputEmployeeActivity.this, "Koneksi Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}