package com.example.fadejayaapp.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.utils.SessionManager;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPass, etNewPass, etConfirmPass;
    private Button btnSave;
    private String userId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 1. Inisialisasi View
        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        btnSave = findViewById(R.id.btnSave);

        // 2. Ambil User ID dari Session (PENTING: Agar sistem tahu siapa yang ganti password)
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = user.get(SessionManager.KEY_ID);

        // 3. Listener Tombol Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 4. Listener Tombol Simpan
        btnSave.setOnClickListener(v -> processChangePassword());
    }

    private void processChangePassword() {
        String oldPass = etOldPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        // Validasi Kosong
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi Konfirmasi Password
        if (!newPass.equals(confirmPass)) {
            etConfirmPass.setError("Konfirmasi password tidak sama!");
            Toast.makeText(this, "Konfirmasi password beda!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Matikan tombol agar tidak double klik
        btnSave.setEnabled(false);
        btnSave.setText("Memproses...");

        // Panggil API
        ApiClient.getService().changePassword(userId, oldPass, newPass).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText("UPDATE PASSWORD");

                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getStatus();
                    String message = response.body().getMessage();

                    if ("success".equals(status)) {
                        Toast.makeText(ChangePasswordActivity.this, "Berhasil! Password diganti.", Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke menu setting
                    } else {
                        // Biasanya error karena password lama salah
                        Toast.makeText(ChangePasswordActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Terjadi kesalahan server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("UPDATE PASSWORD");
                Toast.makeText(ChangePasswordActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}