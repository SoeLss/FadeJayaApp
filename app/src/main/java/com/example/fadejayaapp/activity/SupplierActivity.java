package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.adapter.SupplierAdapter;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.Supplier;
import com.example.fadejayaapp.model.SupplierResponse;
import com.example.fadejayaapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupplierActivity extends AppCompatActivity {

    private RecyclerView rvSuppliers;
    private SupplierAdapter adapter;
    private List<Supplier> masterList = new ArrayList<>();
    private EditText etSearch;

    private SessionManager sessionManager;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier);

        // 1. Ambil Role User
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        userRole = user.get(SessionManager.KEY_ROLE);

        rvSuppliers = findViewById(R.id.rvSuppliers);
        etSearch = findViewById(R.id.etSearch);

        rvSuppliers.setLayoutManager(new GridLayoutManager(this, 2));

        // 2. Logic FAB (Tombol Tambah)
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                Toast.makeText(this, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, InputSupplierActivity.class);
                intent.putExtra("ACTION", "add");
                startActivity(intent);
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 3. Search Logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void filter(String text) {
        List<Supplier> filtered = new ArrayList<>();
        for (Supplier s : masterList) {
            if (s.getStoreName().toLowerCase().contains(text.toLowerCase()) ||
                    s.getContactPerson().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(s);
            }
        }
        if (adapter != null) adapter.filterList(filtered);
    }

    private void loadData() {
        ApiClient.getService().getSuppliers().enqueue(new Callback<SupplierResponse>() {
            @Override
            public void onResponse(Call<SupplierResponse> call, Response<SupplierResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    masterList = response.body().getData();

                    // Pass userRole ke Adapter
                    adapter = new SupplierAdapter(SupplierActivity.this, masterList, userRole, supplier -> {
                        // Logic edit sudah di-handle di Adapter (utk cek role)
                        // Jika lolos (bukan Kasir), kode ini jalan:
                        Intent intent = new Intent(SupplierActivity.this, InputSupplierActivity.class);
                        intent.putExtra("ACTION", "update");
                        intent.putExtra("DATA", supplier);
                        startActivity(intent);
                    });

                    rvSuppliers.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<SupplierResponse> call, Throwable t) {
                Toast.makeText(SupplierActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}