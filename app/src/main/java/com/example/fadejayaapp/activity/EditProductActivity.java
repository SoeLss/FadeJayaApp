package com.example.fadejayaapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.fadejayaapp.api.ApiService;
import com.example.fadejayaapp.model.Product;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.utils.FileUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etSeries, etCode, etHeight, etRack, etBuy, etSell, etWholesale, etStock;
    private Spinner spCategory, spType;
    private ImageView imgProduct;
    private Button btnSave;
    private File photoFile;
    private Product productData; // Data lama

    ActivityResultLauncher<Intent> launcherGaleri = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    imgProduct.setImageURI(uri);
                    imgProduct.setPadding(0,0,0,0);
                    photoFile = FileUtils.getFile(this, uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_product); // Pakai layout yang sama saja biar hemat

        // Ganti judul header biar beda dikit
        TextView tvHeader = findViewById(R.id.header).findViewById(R.id.tvPageTitle); // Asumsi ada ID
        // Atau find manual TextView Header nya

        initViews();
        setupSpinners();

        // AMBIL DATA DARI INTENT
        productData = (Product) getIntent().getSerializableExtra("PRODUCT_DATA");
        if (productData != null) {
            fillData(); // Isi form dengan data lama
        }

        imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherGaleri.launch(intent);
        });

        btnSave.setText("UPDATE BARANG"); // Ganti teks tombol
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
        spType = findViewById(R.id.spType);
        imgProduct = findViewById(R.id.imgProductInput);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupSpinners() {
        String[] types = {"Jadi", "Bahan", "Jasa"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        String[] categories = {"Piala", "Medali", "Plakat"};
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
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

        // Set Spinner Type
        String type = productData.getType();
        if(type != null) {
            if(type.equals("Jadi")) spType.setSelection(0);
            else if(type.equals("Bahan")) spType.setSelection(1);
            else if(type.equals("Jasa")) spType.setSelection(2);
        }

        // Set Spinner Category (1=Piala, 2=Medali, 3=Plakat)
        // Karena array index mulai dari 0, maka ID dikurangi 1
        try {
            int catId = Integer.parseInt(productData.getCategoryId());
            if (catId > 0) spCategory.setSelection(catId - 1);
        } catch (Exception e) {}

        // Load Gambar Lama
        if (productData.getProductImage() != null && !productData.getProductImage().isEmpty()) {
            String url = "https://api.robotrakitan.my.id/uploads/" + productData.getProductImage();
            Glide.with(this).load(url).centerCrop().into(imgProduct);
            imgProduct.setPadding(0,0,0,0);
        }
    }

    private void updateProduct() {
        Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
        btnSave.setEnabled(false);

        RequestBody reqId = createPart(productData.getId()); // ID DARI DATA LAMA
        RequestBody reqName = createPart(etName.getText().toString());
        RequestBody reqSeries = createPart(etSeries.getText().toString());
        RequestBody reqCode = createPart(etCode.getText().toString());
        RequestBody reqHeight = createPart(etHeight.getText().toString());
        RequestBody reqRack = createPart(etRack.getText().toString());
        RequestBody reqBuy = createPart(etBuy.getText().toString());
        RequestBody reqSell = createPart(etSell.getText().toString());
        RequestBody reqWholesale = createPart(etWholesale.getText().toString());
        RequestBody reqStock = createPart(etStock.getText().toString());

        RequestBody reqType = RequestBody.create(MediaType.parse("text/plain"), spType.getSelectedItem().toString());
        int catIndex = spCategory.getSelectedItemPosition() + 1;
        RequestBody reqCatId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(catIndex));

        MultipartBody.Part bodyImage = null;
        if (photoFile != null) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
            bodyImage = MultipartBody.Part.createFormData("image", photoFile.getName(), reqFile);
        }

        ApiService api = ApiClient.getService();
        api.editProduct(reqId, reqName, reqCatId, reqSeries, reqCode, reqHeight, reqType, reqRack,
                        reqBuy, reqSell, reqWholesale, reqStock, bodyImage)
                .enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        btnSave.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            if ("success".equals(response.body().getStatus())) {
                                Toast.makeText(EditProductActivity.this, "Berhasil Diupdate!", Toast.LENGTH_SHORT).show();
                                finish(); // Kembali
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