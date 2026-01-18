package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.adapter.ProductAdapter;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.Product;
import com.example.fadejayaapp.model.ProductResponse;
import com.example.fadejayaapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<Product> masterProductList = new ArrayList<>(); // Menyimpan semua data asli

    // Filter Variables
    private String currentSearchText = "";
    private String selectedCategoryId = "0"; // 0 = Semua, 1=Piala, 2=Medali, 3=Plakat

    // UI Categories
    private LinearLayout btnCatPiala, btnCatMedali, btnCatPlakat;
    private EditText etSearch;

    // Session & Role
    private SessionManager sessionManager;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // 1. Init Session & Ambil Role User
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userRole = user.get(SessionManager.KEY_ROLE); // Contoh: "Owner", "Admin", atau "Kasir"

        // 2. Init View
        rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearch);
        btnCatPiala = findViewById(R.id.btnCatPiala);
        btnCatMedali = findViewById(R.id.btnCatMedali);
        btnCatPlakat = findViewById(R.id.btnCatPlakat);

        ImageView btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddProduct);

        // 3. Setup Recycler
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // 4. Actions
        btnBack.setOnClickListener(v -> finish());

        // LOGIKA FAB TAMBAH: Cek Role dulu
        fabAdd.setOnClickListener(v -> {
            if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                Toast.makeText(this, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, InputProductActivity.class));
            }
        });

        // 5. Setup Category Click Listeners
        btnCatPiala.setOnClickListener(v -> selectCategory("1", btnCatPiala));
        btnCatMedali.setOnClickListener(v -> selectCategory("2", btnCatMedali));
        btnCatPlakat.setOnClickListener(v -> selectCategory("3", btnCatPlakat));

        // 6. Setup Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase();
                applyFilter(); // Jalankan filter setiap ketik
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 7. Load Data Awal
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Refresh data saat kembali dari halaman tambah/edit
    }

    // === LOGIKA PILIH KATEGORI ===
    private void selectCategory(String id, LinearLayout selectedLayout) {
        // Jika klik kategori yang sama, batalkan filter (toggle)
        if (selectedCategoryId.equals(id)) {
            selectedCategoryId = "0"; // Reset ke semua
            resetCategoryUI();
        } else {
            selectedCategoryId = id;
            updateCategoryUI(selectedLayout);
        }
        applyFilter(); // Jalankan filter
    }

    // Ubah Warna UI Kategori
    private void updateCategoryUI(LinearLayout activeLayout) {
        resetCategoryUI();
        activeLayout.setBackgroundResource(R.drawable.bg_category_active);
    }

    private void resetCategoryUI() {
        btnCatPiala.setBackgroundResource(R.drawable.bg_category_inactive);
        btnCatMedali.setBackgroundResource(R.drawable.bg_category_inactive);
        btnCatPlakat.setBackgroundResource(R.drawable.bg_category_inactive);
    }

    // === LOGIKA FILTER ===
    private void applyFilter() {
        List<Product> filteredList = new ArrayList<>();

        for (Product p : masterProductList) {
            // Cek 1: Nama
            boolean matchName = p.getName().toLowerCase().contains(currentSearchText);

            // Cek 2: Kategori (Pastikan null safety agar tidak crash)
            boolean matchCategory = selectedCategoryId.equals("0") ||
                    (p.getCategoryId() != null && p.getCategoryId().equals(selectedCategoryId));

            if (matchName && matchCategory) {
                filteredList.add(p);
            }
        }

        // Update Adapter
        if (adapter != null) {
            adapter.filterList(filteredList);
        }
    }

    private void loadProducts() {
        ApiService api = ApiClient.getService();
        api.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if(response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    // Simpan ke Master List
                    masterProductList = response.body().getData();
                    if (masterProductList == null) masterProductList = new ArrayList<>();

                    // INIT ADAPTER DENGAN ROLE
                    // Kita kirim userRole ke Adapter agar dia bisa sensor harga beli
                    adapter = new ProductAdapter(ProductActivity.this, masterProductList, userRole, product -> {

                        // Callback saat Item diklik (untuk Edit)
                        // Cek Role lagi untuk keamanan ganda
                        if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                            Toast.makeText(ProductActivity.this, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
                        } else {
                            // Pindah ke Halaman Edit
                            Intent intent = new Intent(ProductActivity.this, EditProductActivity.class);
                            intent.putExtra("PRODUCT_DATA", product);
                            startActivity(intent);
                        }
                    });

                    rvProducts.setAdapter(adapter);

                    // Terapkan filter ulang (jika user mengetik search lalu refresh/resume)
                    applyFilter();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(ProductActivity.this, "Koneksi Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}