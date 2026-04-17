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
    private List<Product> masterProductList = new ArrayList<>();

    // Filter Variables
    private String currentSearchText = "";
    private String selectedCategoryId = "0"; // 0=Semua, 1=Piala, 2=Medali, 3=Plakat, 4=Bahan Piala

    // UI Categories
    private LinearLayout btnCatPiala, btnCatMedali, btnCatPlakat, btnCatBahanPiala;
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
        userRole = user.get(SessionManager.KEY_ROLE);

        // 2. Init View
        rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearch);
        btnCatPiala = findViewById(R.id.btnCatPiala);
        btnCatBahanPiala = findViewById(R.id.btnCatBahanPiala); // TAMBAHAN
        btnCatMedali = findViewById(R.id.btnCatMedali);
        btnCatPlakat = findViewById(R.id.btnCatPlakat);

        ImageView btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddProduct);

        // 3. Setup Recycler
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // 4. Actions
        btnBack.setOnClickListener(v -> finish());

        // LOGIKA FAB TAMBAH: Cek Role
        fabAdd.setOnClickListener(v -> {
            if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                Toast.makeText(this, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, InputProductActivity.class));
            }
        });

        // 5. Setup Category Click Listeners (Mapping ke ID database)
        btnCatPiala.setOnClickListener(v -> selectCategory("1", btnCatPiala));
        btnCatMedali.setOnClickListener(v -> selectCategory("2", btnCatMedali));
        btnCatPlakat.setOnClickListener(v -> selectCategory("3", btnCatPlakat));
        btnCatBahanPiala.setOnClickListener(v -> selectCategory("4", btnCatBahanPiala)); // TAMBAHAN

        // 6. Setup Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase();
                applyFilter();
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
        loadProducts();
    }

    // === LOGIKA PILIH KATEGORI ===
    private void selectCategory(String id, LinearLayout selectedLayout) {
        if (selectedCategoryId.equals(id)) {
            selectedCategoryId = "0"; // Reset ke semua
            resetCategoryUI();
        } else {
            selectedCategoryId = id;
            updateCategoryUI(selectedLayout);
        }
        applyFilter();
    }

    private void updateCategoryUI(LinearLayout activeLayout) {
        resetCategoryUI();
        activeLayout.setBackgroundResource(R.drawable.bg_category_active);
    }

    private void resetCategoryUI() {
        btnCatPiala.setBackgroundResource(R.drawable.bg_category_inactive);
        btnCatBahanPiala.setBackgroundResource(R.drawable.bg_category_inactive); // TAMBAHAN
        btnCatMedali.setBackgroundResource(R.drawable.bg_category_inactive);
        btnCatPlakat.setBackgroundResource(R.drawable.bg_category_inactive);
    }

    // === LOGIKA FILTER ===
    private void applyFilter() {
        List<Product> filteredList = new ArrayList<>();

        for (Product p : masterProductList) {
            boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(currentSearchText);

            boolean matchCategory = selectedCategoryId.equals("0") ||
                    (p.getCategoryId() != null && p.getCategoryId().equals(selectedCategoryId));

            if (matchName && matchCategory) {
                filteredList.add(p);
            }
        }

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

                    masterProductList = response.body().getData();
                    if (masterProductList == null) masterProductList = new ArrayList<>();

                    adapter = new ProductAdapter(ProductActivity.this, masterProductList, userRole, product -> {
                        if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                            Toast.makeText(ProductActivity.this, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(ProductActivity.this, EditProductActivity.class);
                            intent.putExtra("PRODUCT_DATA", product);
                            startActivity(intent);
                        }
                    });

                    rvProducts.setAdapter(adapter);
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