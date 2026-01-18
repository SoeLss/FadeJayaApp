package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.adapter.CustomerAdapter;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.Customer;
import com.example.fadejayaapp.model.CustomerResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerActivity extends AppCompatActivity {

    private RecyclerView rvCustomers;
    private CustomerAdapter adapter;
    private List<Customer> masterList = new ArrayList<>();

    // UI Filter
    private LinearLayout btnUmum, btnReseller, btnInstansi;
    private ImageView iconUmum, iconReseller, iconInstansi;
    private TextView txtUmum, txtReseller, txtInstansi;
    private EditText etSearch;

    private String currentSearch = "";
    private String selectedType = "All"; // All, Umum, Reseller, Instansi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // Init Views
        rvCustomers = findViewById(R.id.rvCustomers);
        etSearch = findViewById(R.id.etSearch);

        btnUmum = findViewById(R.id.btnFilterUmum);
        btnReseller = findViewById(R.id.btnFilterReseller);
        btnInstansi = findViewById(R.id.btnFilterInstansi);

        iconUmum = findViewById(R.id.iconUmum);
        iconReseller = findViewById(R.id.iconReseller);
        iconInstansi = findViewById(R.id.iconInstansi);

        txtUmum = findViewById(R.id.txtUmum);
        txtReseller = findViewById(R.id.txtReseller);
        txtInstansi = findViewById(R.id.txtInstansi);

        // Setup Grid 2 Kolom
        rvCustomers.setLayoutManager(new GridLayoutManager(this, 2));

        // Listener Filter
        btnUmum.setOnClickListener(v -> toggleFilter("Umum", btnUmum));
        btnReseller.setOnClickListener(v -> toggleFilter("Reseller", btnReseller));
        btnInstansi.setOnClickListener(v -> toggleFilter("Instansi", btnInstansi));

        // Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().toLowerCase();
                applyFilter();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        // Add Button (CRUD Semua Role)
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(this, InputCustomerActivity.class);
            intent.putExtra("ACTION", "add");
            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load data awal
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // === LOGIC UTAMA GANTI WARNA ===
    private void toggleFilter(String type, LinearLayout btn) {
        // Jika tombol yang sama diklik lagi, matikan filter (kembali ke All/Grey)
        if (selectedType.equals(type)) {
            selectedType = "All";
            resetFilterUI(); // Semua jadi abu-abu
        } else {
            // Jika tombol baru diklik
            selectedType = type;
            resetFilterUI(); // Reset semua dulu
            updateActiveUI(type, btn); // Warnai yang dipilih
        }
        applyFilter();
    }

    private void resetFilterUI() {
        int gray = Color.parseColor("#757575");

        // 1. HAPUS TINT (Penting agar warna custom hilang)
        btnUmum.setBackgroundTintList(null);
        btnReseller.setBackgroundTintList(null);
        btnInstansi.setBackgroundTintList(null);

        // 2. Set Background kembali ke Drawable Abu-abu
        btnUmum.setBackgroundResource(R.drawable.bg_category_inactive);
        btnReseller.setBackgroundResource(R.drawable.bg_category_inactive);
        btnInstansi.setBackgroundResource(R.drawable.bg_category_inactive);

        // 3. Reset Warna Text & Icon jadi Abu
        txtUmum.setTextColor(gray); iconUmum.setColorFilter(gray);
        txtReseller.setTextColor(gray); iconReseller.setColorFilter(gray);
        txtInstansi.setTextColor(gray); iconInstansi.setColorFilter(gray);
    }

    private void updateActiveUI(String type, LinearLayout btn) {
        int white = Color.WHITE;
        int color = Color.GRAY;

        // Tentukan Warna Berdasarkan Tipe
        if(type.equals("Umum")) {
            color = Color.parseColor("#00C4FF"); // Cyan/Biru Muda
            txtUmum.setTextColor(white);
            iconUmum.setColorFilter(white);
        }
        else if(type.equals("Reseller")) {
            color = Color.parseColor("#FF5252"); // Merah
            txtReseller.setTextColor(white);
            iconReseller.setColorFilter(white);
        }
        else if(type.equals("Instansi")) {
            color = Color.parseColor("#FFD740"); // Kuning
            txtInstansi.setTextColor(white);
            iconInstansi.setColorFilter(white);
        }

        // Terapkan Warna ke Background Tombol yang dipilih
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
    }
    // ==========================================

    private void applyFilter() {
        List<Customer> filtered = new ArrayList<>();
        for (Customer c : masterList) {
            boolean matchSearch = c.getName().toLowerCase().contains(currentSearch);
            // Jika selectedType "All", semua tipe lolos. Jika tidak, harus match.
            boolean matchType = selectedType.equals("All") || c.getMemberType().equalsIgnoreCase(selectedType);

            if (matchSearch && matchType) filtered.add(c);
        }
        if (adapter != null) adapter.filterList(filtered);
    }

    private void loadData() {
        ApiClient.getService().getCustomers().enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    masterList = response.body().getData();

                    adapter = new CustomerAdapter(CustomerActivity.this, masterList, customer -> {
                        // Intent Edit
                        Intent intent = new Intent(CustomerActivity.this, InputCustomerActivity.class);
                        intent.putExtra("ACTION", "update");
                        intent.putExtra("DATA", customer);
                        startActivity(intent);
                    });

                    rvCustomers.setAdapter(adapter);
                    applyFilter(); // Terapkan filter (berguna jika search text masih ada saat resume)
                }
            }
            @Override
            public void onFailure(Call<CustomerResponse> call, Throwable t) {
                Toast.makeText(CustomerActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}