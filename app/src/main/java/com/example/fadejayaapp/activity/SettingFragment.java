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

        sessionManager = new SessionManager(getContext());

        // Init View
        tvName = view.findViewById(R.id.tvName);
        tvRole = view.findViewById(R.id.tvRole);
        imgProfile = view.findViewById(R.id.imgProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        menuEditProfile = view.findViewById(R.id.menuEditProfile);
        menuChangePass = view.findViewById(R.id.menuChangePass);
        menuPrinter = view.findViewById(R.id.menuPrinter);
        menuAbout = view.findViewById(R.id.menuAbout);

        loadUserData();

        // --- LISTENERS ---

        menuEditProfile.setOnClickListener(v -> {
            // Intent ke Activity Edit Profile (Buat nanti)
            Toast.makeText(getContext(), "Fitur Edit Profil", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(getContext(), ProfileActivity.class));
        });

        menuPrinter.setOnClickListener(v -> {
            // Intent ke Activity Setting Printer Bluetooth (Penting buat Kasir)
            Toast.makeText(getContext(), "Fitur Setting Printer", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadUserData() {
        HashMap<String, String> user = sessionManager.getUserDetails();
        String name = user.get(SessionManager.KEY_NAME);
        String role = user.get(SessionManager.KEY_ROLE);
        String photo = user.get(SessionManager.KEY_PHOTO);

        tvName.setText(name);
        tvRole.setText(role);

        // Load Foto jika ada
        if (photo != null && !photo.isEmpty()) {
            String url = "https://api.robotrakitan.my.id/uploads/" + photo;
            Glide.with(this).load(url).circleCrop().into(imgProfile);
        }
    }

    // Reuse Dialog Logout Modern yang Anda buat sebelumnya
    private void showLogoutDialog() {
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
            sessionManager.logoutUser(); // Logout dan pindah ke Login
            if (getActivity() != null) getActivity().finish();
        });

        dialog.show();
    }
}