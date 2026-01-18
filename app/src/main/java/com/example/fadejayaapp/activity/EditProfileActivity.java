package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Tambahkan import ini
import android.widget.Toast;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.utils.SessionManager;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private SessionManager sessionManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Init Views
        etName = findViewById(R.id.etName);
        Button btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack); // Init Tombol Back

        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userId = user.get(SessionManager.KEY_ID);

        // Set nama saat ini ke EditText
        etName.setText(user.get(SessionManager.KEY_NAME));

        // --- LOGIC TOMBOL BACK (YANG DITAMBAHKAN) ---
        btnBack.setOnClickListener(v -> finish());

        // Logic Simpan
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();

        if(newName.isEmpty()) {
            etName.setError("Nama tidak boleh kosong");
            return;
        }

        // Matikan tombol sementara
        findViewById(R.id.btnSave).setEnabled(false);

        ApiClient.getService().updateProfile(userId, newName).enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                findViewById(R.id.btnSave).setEnabled(true);

                if(response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())){
                    Toast.makeText(EditProfileActivity.this, "Sukses Update Profil!", Toast.LENGTH_SHORT).show();

                    // Update data di Session HP agar langsung berubah tanpa login ulang
                    HashMap<String, String> user = sessionManager.getUserDetails();
                    sessionManager.createLoginSession(
                            userId,
                            user.get(SessionManager.KEY_USERNAME),
                            newName, // Simpan Nama Baru
                            user.get(SessionManager.KEY_ROLE),
                            user.get(SessionManager.KEY_PHOTO)
                    );

                    finish(); // Kembali ke menu sebelumnya
                } else {
                    Toast.makeText(EditProfileActivity.this, "Gagal Update", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                findViewById(R.id.btnSave).setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Error Koneksi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}