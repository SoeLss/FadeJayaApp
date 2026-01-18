package com.example.fadejayaapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.activity.ChangePasswordActivity;
import com.example.fadejayaapp.activity.EditProfileActivity;
import com.example.fadejayaapp.activity.PrinterActivity;
import com.example.fadejayaapp.utils.SessionManager;

import java.util.HashMap;

public class SettingFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvName, tvRole;
    private ImageView imgProfile;
    private Button btnLogout;
    private TextView menuEditProfile, menuChangePass, menuPrinter, menuAbout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // 1. Init Session
        sessionManager = new SessionManager(getContext());

        // 2. Init Views
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        menuEditProfile = view.findViewById(R.id.menuEditProfile);
        menuChangePass = view.findViewById(R.id.menuChangePass);
        menuPrinter = view.findViewById(R.id.menuPrinter);
        menuAbout = view.findViewById(R.id.menuAbout);

        // 3. Load Data Awal
        loadUserData();

        // 4. Setup Listeners (Aksi Klik Menu)

        // Menu Edit Profil
        menuEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Menu Ganti Password
        menuChangePass.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Menu Setting Printer (Penting untuk Cetak Struk)
        menuPrinter.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PrinterActivity.class);
            startActivity(intent);
        });

        // Menu Tentang Aplikasi (Opsional)
        menuAbout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Aplikasi Fade Jaya v1.0\nDeveloped by RRLabs", Toast.LENGTH_LONG).show();
        });

        // Tombol Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    // Dipanggil otomatis saat kembali ke halaman ini (agar nama/foto terupdate)
    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (getContext() == null) return;

        HashMap<String, String> user = sessionManager.getUserDetails();
        String name = user.get(SessionManager.KEY_NAME);
        String role = user.get(SessionManager.KEY_ROLE);
        String photo = user.get(SessionManager.KEY_PHOTO);

        // Set Text
        tvName.setText(name != null ? name : "User");
        tvRole.setText(role != null ? role.toUpperCase() : "STAFF");

        // Load Foto Profil dengan Glide
        // Ganti URL_BASE dengan alamat server Anda yang sebenarnya
        String BASE_URL_IMG = "https://api.robotrakitan.my.id/uploads/";

        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(BASE_URL_IMG + photo)
                    .placeholder(R.drawable.ic_menu_karyawan) // Gambar default saat loading
                    .error(R.drawable.ic_menu_karyawan)       // Gambar jika error/tidak ketemu
                    .circleCrop()
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.ic_menu_karyawan);
        }
    }

    // Menampilkan Dialog Logout Modern
    private void showLogoutDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnLogoutConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            sessionManager.logoutUser(); // Logout dan Clear Session
            if (getActivity() != null) getActivity().finish(); // Tutup Main Activity
        });

        dialog.show();
    }
}