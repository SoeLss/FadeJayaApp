package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView; // Tambahan untuk tombol Back
import android.widget.TextView;  // Tambahan untuk Forgot Pass
import android.widget.Toast;

// Import Material Design Components
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.LoginResponse;
import com.example.fadejayaapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Komponen UI
    private TextInputLayout layoutUsername, layoutPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;

    // Tambahan Komponen Baru sesuai Desain XML
    private ImageView btnBack;
    private TextView tvForgotPassword;

    // Tools
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inisialisasi Session
        sessionManager = new SessionManager(this);

        // 2. Binding View (Menghubungkan variabel dengan ID di XML)
        layoutUsername = findViewById(R.id.layoutUsername); // Pastikan ID ini sudah ditambah di XML
        layoutPassword = findViewById(R.id.layoutPassword); // Pastikan ID ini sudah ditambah di XML
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Binding elemen baru
        btnBack = findViewById(R.id.btnBack);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 3. Aksi Klik Tombol Back (Panah Kiri)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Menutup halaman login (kembali ke halaman sebelumnya atau keluar app)
                onBackPressed();
            }
        });

        // 4. Aksi Klik Lupa Password
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Silakan hubungi Admin untuk reset password.", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Aksi Klik Tombol Login (Logika Lama Tetap Dipakai)
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                // Validasi Input Kosong
                if (validateInput(user, pass)) {
                    prosesLoginKeServer(user, pass);
                }
            }
        });
    }

    // Fungsi Validasi (Menampilkan Error Merah di Outline Input)
    private boolean validateInput(String user, String pass) {
        boolean isValid = true;

        if (user.isEmpty()) {
            layoutUsername.setError("Username tidak boleh kosong");
            isValid = false;
        } else {
            layoutUsername.setError(null); // Hapus error jika sudah diisi
            layoutUsername.setErrorEnabled(false); // Hilangkan space error agar rapi
        }

        if (pass.isEmpty()) {
            layoutPassword.setError("Password wajib diisi");
            isValid = false;
        } else {
            layoutPassword.setError(null);
            layoutPassword.setErrorEnabled(false);
        }

        return isValid;
    }

    // Fungsi Utama Koneksi ke API (TIDAK ADA YANG DIUBAH)
    private void prosesLoginKeServer(String username, String password) {
        // Ubah tombol jadi disable biar gak diklik berkali-kali saat loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Memuat...");

        ApiClient.getService().loginUser(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Kembalikan tombol seperti semula
                btnLogin.setEnabled(true);
                btnLogin.setText("Masuk");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResult = response.body();

                    if (loginResult.isSuccess()) {
                        // === LOGIN SUKSES ===
                        LoginResponse.UserData data = loginResult.getData();

                        // 1. Simpan data user ke HP
                        sessionManager.createLoginSession(
                                data.getId(),
                                data.getUsername(),
                                data.getFullName(),
                                data.getRole(),
                                data.getProfilePhoto()
                        );

                        // 2. Notifikasi
                        Toast.makeText(LoginActivity.this, "Selamat Datang, " + data.getFullName(), Toast.LENGTH_SHORT).show();

                        // 3. Pindah ke MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        // === PASSWORD SALAH / USER GAK ADA ===
                        Toast.makeText(LoginActivity.this, loginResult.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        int errorCode = response.code();
                        android.util.Log.e("DEBUG_LOGIN", "Kode: " + errorCode + " | Pesan: " + errorBody);
                        Toast.makeText(LoginActivity.this, "Error Server: " + errorCode, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Masuk");
                Toast.makeText(LoginActivity.this, "Gagal Konek: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}