package com.example.fadejayaapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.utils.FileUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputProductActivity extends AppCompatActivity {

    // UI Components
    private EditText etName, etSeries, etCode, etHeight, etRack, etBuy, etSell, etWholesale, etStock;
    private Spinner spCategory, spType;
    private ImageView imgProduct;
    private Button btnSave;

    // Data Gambar
    private File photoFile;

    // Launcher Galeri
    ActivityResultLauncher<Intent> launcherGaleri = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    // Tampilkan di ImageView
                    imgProduct.setImageURI(uri);
                    imgProduct.setPadding(0,0,0,0); // Hapus padding default

                    // Convert ke File
                    photoFile = FileUtils.getFile(this, uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_product);

        initViews();
        setupSpinners();

        // Klik Gambar -> Buka Galeri
        imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherGaleri.launch(intent);
        });

        // Klik Simpan
        btnSave.setOnClickListener(v -> saveProduct());

        // Klik Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etSeries = findViewById(R.id.etSeries);
        etCode = findViewById(R.id.etCode);
        etHeight = findViewById(R.id.etHeight);
        etRack = findViewById(R.id.etRack);
        etBuy = findViewById(R.id.etBuyPrice);
        etSell = findViewById(R.id.etSellPrice);
        etWholesale = findViewById(R.id.etWholesalePrice);
        etStock = findViewById(R.id.etStock);
        spCategory = findViewById(R.id.spCategory);
        spType = findViewById(R.id.spType);
        imgProduct = findViewById(R.id.imgProductInput);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupSpinners() {
        // Spinner Tipe
        String[] types = {"Jadi", "Bahan", "Jasa"};
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(adapterType);

        // Spinner Kategori (Manual dulu sesuai database Anda: 1=Piala Set, 2=Figur, 3=Sparepart)
        // Nanti bisa dibikin dinamis ambil dari API jika mau
        String[] categories = {"Piala", "Medali", "Plakat"};
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(adapterCat);
    }

    private void saveProduct() {
        // Validasi Sederhana
        if (etName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Nama Barang wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Menyimpan data...", Toast.LENGTH_SHORT).show();
        btnSave.setEnabled(false); // Cegah klik ganda

        // Siapkan RequestBody Text
        RequestBody reqName = createPart(etName);
        RequestBody reqSeries = createPart(etSeries);
        RequestBody reqCode = createPart(etCode);
        RequestBody reqHeight = createPart(etHeight);
        RequestBody reqRack = createPart(etRack);
        RequestBody reqBuy = createPart(etBuy);
        RequestBody reqSell = createPart(etSell);
        RequestBody reqWholesale = createPart(etWholesale);
        RequestBody reqStock = createPart(etStock);

        // Ambil Value Spinner
        RequestBody reqType = RequestBody.create(MediaType.parse("text/plain"), spType.getSelectedItem().toString());

        // Mapping Kategori (Nama -> ID)
        int catIndex = spCategory.getSelectedItemPosition() + 1; // 0->1, 1->2, dst
        RequestBody reqCatId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(catIndex));

        // Siapkan Gambar (Jika ada)
        MultipartBody.Part bodyImage = null;
        if (photoFile != null) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
            bodyImage = MultipartBody.Part.createFormData("image", photoFile.getName(), reqFile);
        }

        // Eksekusi API
        ApiService api = ApiClient.getService();
        api.addProduct(reqName, reqCatId, reqSeries, reqCode, reqHeight, reqType, reqRack,
                        reqBuy, reqSell, reqWholesale, reqStock, bodyImage)
                .enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        btnSave.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus().equals("success")) {
                                Toast.makeText(InputProductActivity.this, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show();
                                finish(); // Kembali ke halaman list
                            } else {
                                Toast.makeText(InputProductActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(InputProductActivity.this, "Respon Server Error", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        btnSave.setEnabled(true);
                        Toast.makeText(InputProductActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper untuk membuat RequestBody dari EditText
    private RequestBody createPart(EditText editText) {
        String text = editText.getText().toString().trim();
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }
}