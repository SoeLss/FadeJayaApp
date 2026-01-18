package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;  // [TAMBAHAN] Untuk melihat log di bawah
import android.widget.Toast; // [TAMBAHAN] Untuk notifikasi pop-up

// Import library SmoothBottomBar
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient; // [TAMBAHAN]
import com.example.fadejayaapp.model.Product; // [TAMBAHAN]
import com.example.fadejayaapp.model.ProductResponse; // [TAMBAHAN]

import java.util.List; // [TAMBAHAN]

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import retrofit2.Call; // [TAMBAHAN]
import retrofit2.Callback; // [TAMBAHAN]
import retrofit2.Response; // [TAMBAHAN]

public class MainActivity extends AppCompatActivity {

    private SmoothBottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inisialisasi Bottom Bar
        bottomBar = findViewById(R.id.bottomBar);

        // 2. Load Halaman Awal (Beranda)
        loadFragment(new BerandaFragment());

        // 3. Listener Klik Menu Navigasi
        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                Fragment selectedFragment = null;

                switch (i) {
                    case 0:
                        selectedFragment = new BerandaFragment();
                        break;
                    case 1:
                        selectedFragment = new TransaksiFragment();
                        break;
                    case 2:
                        selectedFragment = new LaporanFragment();
                        break;
                    case 3:
                        selectedFragment = new SettingFragment();
                        break;
                }

                loadFragment(selectedFragment);
                return true;
            }
        });

        // ==========================================
        // [TAMBAHAN BARU] PANGGIL FUNGSI TES KONEKSI
        // ==========================================
        tesKoneksiServer();
    }

    // Fungsi ganti halaman (Fragment)
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commit();
        }
    }

    // ==========================================
    // [TAMBAHAN BARU] FUNGSI RETROFIT
    // ==========================================
    private void tesKoneksiServer() {
        // Menggunakan ApiClient yang sudah dibuat sebelumnya
        ApiClient.getService().getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // BERHASIL KONEK DAN ADA DATA
                    List<Product> dataBarang = response.body().getData();

                    // Munculkan pesan sukses di layar HP
                    Toast.makeText(MainActivity.this,
                            "Koneksi Sukses! Ditemukan " + dataBarang.size() + " Barang.",
                            Toast.LENGTH_LONG).show();

                    // Cek detailnya di Logcat (Filter: TES_API)
                    for (Product p : dataBarang) {
                        Log.d("TES_API", "Barang: " + p.getName() + " | Harga: " + p.getSellPrice());
                    }

                } else {
                    // KONEK TAPI DATA KOSONG / ERROR API
                    Toast.makeText(MainActivity.this, "Koneksi Berhasil tapi Data Kosong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // GAGAL TOTAL (Internet Mati / Server Down / URL Salah)
                Log.e("TES_API", "Error Koneksi: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Gagal Konek Server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}