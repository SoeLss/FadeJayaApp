package com.example.fadejayaapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.api.ApiService;
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

public class InputProductActivity extends AppCompatActivity {

    private EditText etName, etSeries, etCode, etHeight, etRack, etBuy, etSell, etWholesale, etStock;
    private Spinner spCategory;
    private TextView tvType, tvPageTitle;
    private ImageView imgProduct;
    private Button btnSave;
    private File photoFile;

    private String[] listKategori = {"Piala", "Medali", "Plakat", "Bahan Piala"};
    private int[] listCatIds = {1, 2, 3, 4};

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

        imgProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherGaleri.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveProduct());
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

        if (tvPageTitle != null) tvPageTitle.setText("Tambah Barang Baru");
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
                    tvType.setText("");
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
                    Toast.makeText(this, "Data Bahan Piala Kosong / Sedang Dimuat...", Toast.LENGTH_SHORT).show();
                    return;
                }
                showBahanPialaDialog(); // Panggil Dialog Baru
            }
        });
    }

    // === DIALOG 1 LANGKAH (CHECKBOX & JUMLAH BERSEBELAHAN) ===
    private void showBahanPialaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Komponen Piala");

        // Container utama (Bisa di-scroll kalau komponennya banyak)
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 30, 50, 30);

        // Array penampung inputan
        CheckBox[] checkBoxes = new CheckBox[listBahanPiala.size()];
        EditText[] editTexts = new EditText[listBahanPiala.size()];

        for (int i = 0; i < listBahanPiala.size(); i++) {
            Product komponen = listBahanPiala.get(i);

            // Row (Baris) untuk masing-masing komponen
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);
            rowLayout.setPadding(0, 10, 0, 10);

            // 1. CheckBox (Nama Komponen)
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(komponen.getName() + " (Stok: " + komponen.getStock() + ")");
            checkBox.setChecked(checkedBahan[i]);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

            // 2. EditText (Kotak Jumlah / Qty)
            EditText etQty = new EditText(this);
            etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
            etQty.setHint("Qty");
            etQty.setText("1");
            etQty.setGravity(Gravity.CENTER);
            etQty.setEnabled(checkedBahan[i]); // Hanya bisa diketik kalau dicentang
            etQty.setLayoutParams(new LinearLayout.LayoutParams(150, ViewGroup.LayoutParams.WRAP_CONTENT));

            // Logic: Otomatis kunci/buka EditText saat CheckBox di-klik
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                etQty.setEnabled(isChecked);
                if (isChecked && etQty.getText().toString().isEmpty()) {
                    etQty.setText("1");
                }
            });

            // Simpan ke array
            checkBoxes[i] = checkBox;
            editTexts[i] = etQty;

            // Masukkan ke Row, lalu Row ke MainLayout
            rowLayout.addView(checkBox);
            rowLayout.addView(etQty);
            mainLayout.addView(rowLayout);
        }

        scrollView.addView(mainLayout);
        builder.setView(scrollView);

        // Jika OK diklik, kita looping semua inputan tadi
        builder.setPositiveButton("OK", (dialog, which) -> {
            List<String> hasilAkhir = new ArrayList<>();
            for (int i = 0; i < listBahanPiala.size(); i++) {
                checkedBahan[i] = checkBoxes[i].isChecked(); // Simpan riwayat centang

                if (checkBoxes[i].isChecked()) {
                    String qtyStr = editTexts[i].getText().toString();
                    int qty = qtyStr.isEmpty() ? 1 : Integer.parseInt(qtyStr);
                    Product kom = listBahanPiala.get(i);

                    // Format: 90#Truno (3x)
                    hasilAkhir.add(kom.getId() + "#" + kom.getName() + " (" + qty + "x)");
                }
            }

            if (hasilAkhir.isEmpty()) {
                tvType.setText("");
            } else {
                tvType.setText(TextUtils.join(", ", hasilAkhir));
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

    private void saveProduct() {
        if (etName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Nama Barang wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Menyimpan data...", Toast.LENGTH_SHORT).show();
        btnSave.setEnabled(false);

        RequestBody reqName = createPart(etName);
        RequestBody reqSeries = createPart(etSeries);
        RequestBody reqCode = createPart(etCode);
        RequestBody reqHeight = createPart(etHeight);
        RequestBody reqRack = createPart(etRack);
        RequestBody reqBuy = createPart(etBuy);
        RequestBody reqSell = createPart(etSell);
        RequestBody reqWholesale = createPart(etWholesale);
        RequestBody reqStock = createPart(etStock);

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

        ApiClient.getService().addProduct(reqName, reqCatId, reqSeries, reqCode, reqHeight, reqType, reqRack,
                        reqBuy, reqSell, reqWholesale, reqStock, bodyImage)
                .enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        btnSave.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            if ("success".equals(response.body().getStatus())) {
                                Toast.makeText(InputProductActivity.this, "Berhasil Disimpan!", Toast.LENGTH_SHORT).show();
                                finish();
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

    private RequestBody createPart(EditText editText) {
        String text = editText.getText().toString().trim();
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }
}