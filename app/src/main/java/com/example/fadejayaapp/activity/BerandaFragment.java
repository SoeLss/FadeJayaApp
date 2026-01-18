package com.example.fadejayaapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.adapter.DashboardAdapter;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.DashboardMenu;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.utils.FileUtils;
import com.example.fadejayaapp.utils.SessionManager;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BerandaFragment extends Fragment {

    // --- KOMPONEN UI ---
    private TextView tvHeaderName, tvBodyName;
    private RecyclerView rvDataMaster;
    private ImageView btnLogout;
    private CardView cardProfileSmall;
    private ImageView imgProfile;

    // --- TOOLS ---
    private SessionManager sessionManager;

    // --- LAUNCHER GALERI ---
    // Menangani hasil ketika user memilih foto dari galeri
    ActivityResultLauncher<Intent> launcherGaleri = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    // Langsung jalankan proses upload saat foto dipilih
                    uploadFotoProfil(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beranda, container, false);

        // 1. Inisialisasi Komponen UI
        tvHeaderName = view.findViewById(R.id.tvHeaderName);
        tvBodyName = view.findViewById(R.id.tvBodyName);
        rvDataMaster = view.findViewById(R.id.rvDataMaster);
        btnLogout = view.findViewById(R.id.btnLogout);
        cardProfileSmall = view.findViewById(R.id.cardProfileSmall);

        // Ambil ImageView di dalam CardView (karena tidak ada ID langsung di XML untuk ImageView-nya)
        if (cardProfileSmall.getChildCount() > 0) {
            imgProfile = (ImageView) cardProfileSmall.getChildAt(0);
        }

        sessionManager = new SessionManager(getContext());

        // 2. Setup Logic
        setupHeader();      // Tampilkan Nama & Foto
        setupMenuGrid();    // Tampilkan Menu
        setupLogoutAction();// Aktifkan Tombol Logout

        // 3. Aksi Klik Foto Profil -> Buka Galeri
        cardProfileSmall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherGaleri.launch(intent);
        });
        
        View btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            showLogoutDialog(); // Panggil Dialog Keren
        });

        return view;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Inflate Layout Custom
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        builder.setView(view);

        // Buat Dialog
        AlertDialog dialog = builder.create();

        // PENTING: Set background transparan agar sudut rounded terlihat
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Init Tombol dalam Dialog
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnLogoutConfirm);

        // Aksi Batal
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Aksi Logout
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();

            // Proses Logout Session
            sessionManager.logoutUser();
            // logoutUser() biasanya sudah ada intent ke LoginActivity & finish()
        });

        dialog.show();
    }

    // === BAGIAN 1: HEADER & FOTO PROFIL ===
    private void setupHeader() {
        HashMap<String, String> user = sessionManager.getUserDetails();
        String name = user.get(SessionManager.KEY_NAME);
        String photoName = user.get(SessionManager.KEY_PHOTO); // Ambil nama file dari session

        // Set Nama
        tvHeaderName.setText("Hi, " + name);
        tvBodyName.setText("Hello " + name);

        // Load Foto Profil dengan GLIDE
        if (photoName != null && !photoName.isEmpty()) {
            // URL Manual ke folder uploads di server
            String fullUrl = "https://api.robotrakitan.my.id/uploads/" + photoName;

            if (getContext() != null) {
                Glide.with(this)
                        .load(fullUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Matikan cache agar foto baru langsung muncul
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_launcher_background) // Gambar loading
                        .error(R.drawable.ic_launcher_background)       // Gambar jika error
                        .centerCrop()
                        .into(imgProfile);
            }
        }
    }

    // === BAGIAN 2: UPLOAD FOTO ===
    private void uploadFotoProfil(Uri uri) {
        Toast.makeText(getContext(), "Sedang mengupload...", Toast.LENGTH_SHORT).show();

        // A. Konversi URI Galeri ke File Fisik
        File file = FileUtils.getFile(getContext(), uri);

        if (file != null) {
            // B. Siapkan Data untuk API
            HashMap<String, String> user = sessionManager.getUserDetails();
            String idUser = user.get(SessionManager.KEY_ID);

            // RequestBody untuk teks (ID User)
            RequestBody reqId = RequestBody.create(MediaType.parse("text/plain"), idUser);
            // RequestBody untuk file gambar
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            // MultipartBody.Part untuk file wrapper
            MultipartBody.Part body = MultipartBody.Part.createFormData("photo", file.getName(), reqFile);

            // C. Eksekusi API Update Photo
            ApiService api = ApiClient.getService(); // Panggil ApiService
            api.updateProfilePhoto(reqId, body).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UploadResponse resp = response.body();

                        if ("success".equals(resp.getStatus())) {
                            Toast.makeText(getContext(), "Foto Berhasil Diganti!", Toast.LENGTH_SHORT).show();

                            // 1. Simpan nama foto baru ke Session HP
                            sessionManager.updatePhotoSession(resp.getNewPhoto());

                            // 2. Refresh Tampilan Header agar foto berubah
                            setupHeader();
                        } else {
                            Toast.makeText(getContext(), "Gagal: " + resp.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Respon Server Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error Koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Gagal membaca file gambar", Toast.LENGTH_SHORT).show();
        }
    }

    // === BAGIAN 3: MENU GRID ===
    private void setupMenuGrid() {
        List<DashboardMenu> menus = new ArrayList<>();

        // 1. Tambahkan SEMUA Menu (Tanpa If-Else Role di sini)
        menus.add(new DashboardMenu("Barang", R.drawable.ic_menu_produk));
        menus.add(new DashboardMenu("Pelanggan", R.drawable.ic_menu_pelanggan));
        menus.add(new DashboardMenu("Supplier", R.drawable.ic_menu_supplier));
        menus.add(new DashboardMenu("Karyawan", R.drawable.ic_menu_karyawan)); // Menu ini sekarang selalu muncul

        // Ambil Role User Saat Ini
        HashMap<String, String> user = sessionManager.getUserDetails();
        String role = user.get(SessionManager.KEY_ROLE); // Contoh: "Owner", "Admin", "Kasir"

        // 2. Setup Adapter & Logic Klik
        DashboardAdapter adapter = new DashboardAdapter(getContext(), menus, new DashboardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String menuTitle) {
                Intent intent = null;

                switch (menuTitle) {
                    case "Barang":
                        intent = new Intent(getContext(), ProductActivity.class);
                        break;
                    case "Pelanggan":
                        intent = new Intent(getContext(), CustomerActivity.class);
                        break;
                    case "Supplier":
                        intent = new Intent(getContext(), SupplierActivity.class);
                        break;
                    case "Karyawan":
                        // === LOGIC BARU DI SINI ===
                        // Cek apakah user adalah Owner?
                        if (role != null && role.equalsIgnoreCase("Owner")) {
                            intent = new Intent(getContext(), EmployeeActivity.class);
                        } else {
                            // Jika BUKAN Owner, Tampilkan Pesan
                            Toast.makeText(getContext(), "Akses Ditolak! Menu ini khusus Owner.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        Toast.makeText(getContext(), "Menu belum tersedia", Toast.LENGTH_SHORT).show();
                        break;
                }

                // Jalankan Intent jika tidak null
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });

        rvDataMaster.setLayoutManager(new GridLayoutManager(getContext(), 3)); // Grid 3 kolom
        rvDataMaster.setAdapter(adapter);
    }

    // === BAGIAN 4: LOGOUT ===
    private void setupLogoutAction() {
        btnLogout.setOnClickListener(v -> {
            // Hapus session
            sessionManager.logoutUser();

            // Pindah ke LoginActivity & Hapus History
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Tutup Fragment/Activity ini
            if (getActivity() != null) getActivity().finish();
        });
    }
}