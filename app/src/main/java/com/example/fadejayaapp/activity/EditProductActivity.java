package com.example.fadejayaapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.Product;
import com.example.fadejayaapp.model.ProductResponse;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etSeries, etCode, etHeight, etRack, etBuy, etSell, etWholesale, etStock;
    private Spinner spCategory;
    private TextView tvType, tvPageTitle;
    private ImageView imgProduct;
    private Button btnSave;
    private File photoFile;
    private Product productData;

    // Array Kategori
    private String[] listKategori = {"Piala", "Medali", "Plakat", "Bahan Piala"};
    private int[] listCatIds = {1, 2, 3, 4};

    // Variabel Penampung List Bahan
    private List<Product> listBahanPiala = new ArrayList<>();
    private String[] arrayNamaBahan;
    private boolean[] checkedBahan;

    ActivityResultLauncher<Intent> launcherGaleri = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    imgProduct.setImageURI(uri);
                    imgProduct.setPadding(0, 0, 0, 0);
                    photoFile = FileUtils.getFile(this, uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_product);

        initViews();
        setupSpinnerAndListeners();
        loadBahanPialaDariAPI();

        productData = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");
        if (productData != null) {
            fillData();
        }

        imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherGaleri.launch(intent);
        });

        btnSave.setText("UPDATE BARANG");
        btnSave.setOnClickListener(v -> updateProduct());
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
        tvType = findViewById(R.id.tvType);
        imgProduct = findViewById(R.id.imgProductInput);
        btnSave = findViewById(R.id.btnSave);
        tvPageTitle = findViewById(R.id.tvPageTitle);

        if(tvPageTitle != null) tvPageTitle.setText("Edit Barang");
    }

    private void setupSpinnerAndListeners() {
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listKategori);
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapterCat);

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = listKategori[position];

                if (selected.equals("Piala")) {
                    // Hanya reset jika isinya 'Bahan' atau 'Jadi'. Kalau isinya list komponen lama, biarkan
                    if (tvType.getText().toString().equals("Bahan") || tvType.getText().toString().equals("Jadi")) {
                        tvType.setText("");
                    }
                    tvType.setHint("Pilih Komponen Piala (Klik Disini)");
                    tvType.setEnabled(true);
                } else if (selected.equals("Bahan Piala")) {
                    tvType.setText("Bahan");
                    tvType.setEnabled(false);
                } else {
                    tvType.setText("Jadi");
                    tvType.setEnabled(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        tvType.setOnClickListener(v -> {
            if (spCategory.getSelectedItem().toString().equals("Piala")) {
                if (arrayNamaBahan == null || arrayNamaBahan.length == 0) {
                    Toast.makeText(this, "Data Bahan Piala Kosong / Loading...", Toast.LENGTH_SHORT).show();
                    return;
                }
                showBahanPialaDialog();
            }
        });
    }

    private void showBahanPialaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Komponen Piala");

        // Sync centang dengan data yang sudah pernah di-save dari database
        String existingType = tvType.getText().toString();
        for (int i = 0; i < arrayNamaBahan.length; i++) {
            checkedBahan[i] = existingType.contains(arrayNamaBahan[i]);
        }

        builder.setMultiChoiceItems(arrayNamaBahan, checkedBahan, (dialog, which, isChecked) -> {
            checkedBahan[which] = isChecked;
        });

        builder.setPositiveButton("Selesai", (dialog, which) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arrayNamaBahan.length; i++) {
                if (checkedBahan[i]) {
                    sb.append(arrayNamaBahan[i]).append(", ");
                }
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2);
                tvType.setText(sb.toString());
            } else {
                tvType.setText("");
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void loadBahanPialaDariAPI() {
        ApiClient.getService().getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listBahanPiala.clear();
                    for (Product p : response.body().getData()) {
                        if ("Bahan".equalsIgnoreCase(p.getType()) || "4".equals(p.getCategoryId())) {
                            listBahanPiala.add(p);
                        }
                    }

                    arrayNamaBahan = new String[listBahanPiala.size()];
                    checkedBahan = new boolean[listBahanPiala.size()];
                    for (int i = 0; i < listBahanPiala.size(); i++) {
                        arrayNamaBahan[i] = listBahanPiala.get(i).getName();
                        checkedBahan[i] = false;
                    }
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {}
        });
    }

    private void fillData() {
        etName.setText(productData.getName());
        etSeries.setText(productData.getSeries());
        etCode.setText(productData.getProductCode());
        etHeight.setText(productData.getHeight());
        etRack.setText(productData.getRackLocation());
        etBuy.setText(productData.getBuyPrice());
        etSell.setText(productData.getSellPrice());
        etWholesale.setText(productData.getWholesalePrice());
        etStock.setText(productData.getStock());

        // Set Kategori sesuai Database (Map ID dari server ke index Spinner)
        try {
            int catId = Integer.parseInt(productData.getCategoryId());
            for (int i = 0; i < listCatIds.length; i++) {
                if (listCatIds[i] == catId) {
                    spCategory.setSelection(i);
                    break;
                }
            }
        } catch (Exception e) {}

        // Set Teks Komponen/Jenis
        tvType.setText(productData.getType() != null ? productData.getType() : "");

        if (productData.getProductImage() != null && !productData.getProductImage().isEmpty()) {
            String url = "https://api.robotrakitan.my.id/uploads/" + productData.getProductImage();
            Glide.with(this).load(url).centerCrop().into(imgProduct);
            imgProduct.setPadding(0, 0, 0, 0);
        }
    }

    private void updateProduct() {
        if (etName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Nama Barang wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        btnSave.setEnabled(false);

        RequestBody reqId = createPart(productData.getId());
        RequestBody reqName = createPart(etName.getText().toString());
        RequestBody reqSeries = createPart(etSeries.getText().toString());
        RequestBody reqCode = createPart(etCode.getText().toString());
        RequestBody reqHeight = createPart(etHeight.getText().toString());
        RequestBody reqRack = createPart(etRack.getText().toString());
        RequestBody reqBuy = createPart(etBuy.getText().toString());
        RequestBody reqSell = createPart(etSell.getText().toString());
        RequestBody reqWholesale = createPart(etWholesale.getText().toString());
        RequestBody reqStock = createPart(etStock.getText().toString());

        int selectedCatIndex = spCategory.getSelectedItemPosition();
        int catIdDatabase = listCatIds[selectedCatIndex];
        RequestBody reqCatId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(catIdDatabase));

        String typeText = tvType.getText().toString().isEmpty() ? "Jadi" : tvType.getText().toString();
        RequestBody reqType = RequestBody.create(MediaType.parse("text/plain"), typeText);

        MultipartBody.Part bodyImage = null;
        if (photoFile != null) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
            bodyImage = MultipartBody.Part.createFormData("image", photoFile.getName(), reqFile);
        }

        ApiClient.getService().editProduct(reqId, reqName, reqCatId, reqSeries, reqCode, reqHeight, reqType, reqRack,
                        reqBuy, reqSell, reqWholesale, reqStock, bodyImage)
                .enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        btnSave.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            if ("success".equals(response.body().getStatus())) {
                                Toast.makeText(EditProductActivity.this, "Berhasil Diupdate!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProductActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        btnSave.setEnabled(true);
                        Toast.makeText(EditProductActivity.this, "Error koneksi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private RequestBody createPart(String value) {
        if(value == null) value = "";
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}