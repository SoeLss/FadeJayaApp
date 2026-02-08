package com.example.fadejayaapp.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// Import Library Printer
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;

// Import App Classes
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.adapter.CartAdapter;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.CartItem;
import com.example.fadejayaapp.model.Customer;
import com.example.fadejayaapp.model.CustomerResponse;
import com.example.fadejayaapp.model.Product;
import com.example.fadejayaapp.model.ProductResponse;
import com.example.fadejayaapp.model.TransactionRequest;
import com.example.fadejayaapp.model.TransactionResponse;

// Import Java Utilities
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Import Retrofit (WAJIB ADA 3 INI)
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PenjualanActivity extends AppCompatActivity {

    // View Components
    private TextView tvTopTotal, tvTopQty, tvNamaPelanggan, tvNoFaktur, tvTanggal;
    private TextView tvSubTotal, tvDiskonVal, tvGrandTotal, tvKembali;
    private EditText etDibayar;
    private Spinner spinnerPayment;
    private RecyclerView rvCart;
    private CardView cardPelanggan;
    private Button btnTambahBarang, btnPrint;

    // Data Variables
    private List<CartItem> cartList = new ArrayList<>();
    private CartAdapter adapter;
    private Customer selectedCustomer = null;
    private double currentDiscountPercent = 0;
    private double finalTotal = 0;
    private AlertDialog productDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan);

        initViews();
        setupRecyclerView();
        setupListeners();

        // Set Tanggal Hari Ini
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvTanggal.setText(today);

        // Generate Nomor Faktur Dummy (Nanti diganti response server)
        tvNoFaktur.setText("No Faktur: TRX-" + System.currentTimeMillis());

        // Setup Spinner Pembayaran
        ArrayAdapter<String> payAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Cash", "Transfer", "QRIS"});
        spinnerPayment.setAdapter(payAdapter);
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvTopTotal = findViewById(R.id.tvTopTotal);
        tvTopQty = findViewById(R.id.tvTopQty);
        tvNamaPelanggan = findViewById(R.id.tvNamaPelanggan);
        tvNoFaktur = findViewById(R.id.tvNoFaktur);
        tvTanggal = findViewById(R.id.tvTanggal);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        tvDiskonVal = findViewById(R.id.tvDiskonVal);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        tvKembali = findViewById(R.id.tvKembali);

        etDibayar = findViewById(R.id.etDibayar);
        spinnerPayment = findViewById(R.id.spinnerPayment);

        cardPelanggan = findViewById(R.id.cardPelanggan);
        btnTambahBarang = findViewById(R.id.btnTambahBarang);
        btnPrint = findViewById(R.id.btnPrint);
        rvCart = findViewById(R.id.rvCart);
    }

    private void setupListeners() {
        cardPelanggan.setOnClickListener(v -> showCustomerDialog());
        btnTambahBarang.setOnClickListener(v -> showProductDialog());

        etDibayar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { calculateKembali(); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnPrint.setOnClickListener(v -> processTransaction());
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter = new CartAdapter(cartList);
        rvCart.setAdapter(adapter);
    }

    // --- FITUR 1: PELANGGAN ---
    private void showCustomerDialog() {
        Toast.makeText(this, "Mengambil data...", Toast.LENGTH_SHORT).show();

        // Menggunakan retrofit2.Callback eksplisit untuk menghindari error
        ApiClient.getService().getCustomers().enqueue(new retrofit2.Callback<CustomerResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CustomerResponse> call, retrofit2.Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cek getData() atau getRecords() sesuai model CustomerResponse Anda
                    if (response.body().getData() != null) {
                        showSelectionDialog(response.body().getData());
                    } else {
                        Toast.makeText(PenjualanActivity.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<CustomerResponse> call, Throwable t) {
                Toast.makeText(PenjualanActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSelectionDialog(List<Customer> customers) {
        String[] customerNames = new String[customers.size()];
        for (int i = 0; i < customers.size(); i++) {
            customerNames[i] = customers.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Pelanggan");
        builder.setItems(customerNames, (dialog, which) -> {
            selectedCustomer = customers.get(which);
            tvNamaPelanggan.setText(selectedCustomer.getName());

            String discStr = selectedCustomer.getSpecial_discount();
            currentDiscountPercent = (discStr != null && !discStr.isEmpty()) ? Double.parseDouble(discStr) : 0;

            tvDiskonVal.setText(currentDiscountPercent + "%");
            calculateTotals();
        });
        builder.show();
    }

    // --- FITUR 2: PRODUK ---
    private void showProductDialog() {
        Toast.makeText(this, "Memuat produk...", Toast.LENGTH_SHORT).show();
        ApiClient.getService().getProducts().enqueue(new retrofit2.Callback<ProductResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ProductResponse> call, retrofit2.Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getData() != null) {
                        showProductSearchPopup(response.body().getData());
                    } else {
                        Toast.makeText(PenjualanActivity.this, "Produk Kosong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ProductResponse> call, Throwable t) {
                Toast.makeText(PenjualanActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductSearchPopup(List<Product> products) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_search_product, null);
        builder.setView(view);

        EditText etSearch = view.findViewById(R.id.etSearchProduct);
        ListView listView = view.findViewById(R.id.lvProducts);
        Button btnClose = view.findViewById(R.id.btnCloseDialog);

        ArrayList<String> productNames = new ArrayList<>();
        for (Product p : products) {
            productNames.add(p.getName() + "\nRp " + p.getSell_price() + " (Stok: " + p.getStock() + ")");
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productNames);
        listView.setAdapter(listAdapter);

        productDialog = builder.create();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Product selected = products.get(position);
            int stock = (selected.getStock() != null) ? Integer.parseInt(selected.getStock()) : 0;

            if (stock <= 0) {
                Toast.makeText(this, "Stok Habis!", Toast.LENGTH_SHORT).show();
                return;
            }
            showQtyDialog(selected);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listAdapter.getFilter().filter(s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        btnClose.setOnClickListener(v -> productDialog.dismiss());
        productDialog.show();
    }

    private void showQtyDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Beli: " + product.getName());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText("1");
        input.setGravity(Gravity.CENTER);
        builder.setView(input);

        builder.setPositiveButton("TAMBAH", (dialog, which) -> {
            String qtyStr = input.getText().toString();
            if (!qtyStr.isEmpty()) {
                int qty = Integer.parseInt(qtyStr);
                int currentStock = (product.getStock() != null) ? Integer.parseInt(product.getStock()) : 0;

                if (qty > currentStock) {
                    Toast.makeText(this, "Stok Kurang! Sisa: " + currentStock, Toast.LENGTH_LONG).show();
                } else {
                    addToCart(product, qty);
                    if (productDialog != null) productDialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addToCart(Product product, int qty) {
        boolean exist = false;
        // Pastikan konversi ID aman
        int prodId = Integer.parseInt(product.getId());

        for (CartItem item : cartList) {
            if (item.getProductId() == prodId) {
                item.setQty(item.getQty() + qty);
                exist = true;
                break;
            }
        }

        if (!exist) {
            CartItem newItem = new CartItem();
            newItem.setProductId(prodId);
            newItem.setName(product.getName());
            newItem.setPrice(Double.parseDouble(product.getSell_price()));
            newItem.setQty(qty);
            newItem.setCode(product.getProduct_code());
            cartList.add(newItem);
        }

        adapter.notifyDataSetChanged();
        calculateTotals();
    }

    // --- LOGIC PERHITUNGAN ---
    private double calculateSubTotal() {
        double sub = 0;
        for (CartItem item : cartList) {
            sub += (item.getPrice() * item.getQty());
        }
        return sub;
    }

    private double calculateDiscountRp() {
        return calculateSubTotal() * (currentDiscountPercent / 100);
    }

    private void calculateTotals() {
        double subTotal = calculateSubTotal();
        int totalQty = 0;
        for (CartItem item : cartList) totalQty += item.getQty();

        finalTotal = subTotal - calculateDiscountRp();

        tvSubTotal.setText(formatRupiah(subTotal));
        tvGrandTotal.setText(formatRupiah(finalTotal));
        tvTopTotal.setText(formatRupiah(finalTotal));
        tvTopQty.setText(String.valueOf(totalQty));

        calculateKembali();
    }

    private void calculateKembali() {
        String paidStr = etDibayar.getText().toString();
        if (!paidStr.isEmpty()) {
            try {
                double paid = Double.parseDouble(paidStr);
                double change = paid - finalTotal;
                tvKembali.setText(formatRupiah(change));
            } catch (NumberFormatException e) {
                tvKembali.setText("Rp 0");
            }
        }
    }

    // --- FITUR 3: PROSES TRANSAKSI ---
    private void processTransaction() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Keranjang Kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        TransactionRequest req = new TransactionRequest();
        req.setUser_id("1"); // Hardcode dulu, nanti ambil dari session
        req.setCustomer_id(selectedCustomer != null ? selectedCustomer.getId() : null);
        req.setSub_total(calculateSubTotal());
        req.setDiscount_amount(calculateDiscountRp());
        req.setGrand_total(finalTotal);
        req.setPayment_method(spinnerPayment.getSelectedItem().toString());
        req.setItems(cartList);

        // Panggil API dengan Callback Eksplisit
        ApiClient.getService().savePenjualan(req).enqueue(new retrofit2.Callback<TransactionResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TransactionResponse> call, retrofit2.Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(PenjualanActivity.this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show();
                        printStruk(response.body().getInvoice(), response.body().getSaleId());
                    } else {
                        Toast.makeText(PenjualanActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PenjualanActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TransactionResponse> call, Throwable t) {
                Toast.makeText(PenjualanActivity.this, "Koneksi Gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void printStruk(String invoiceNo, String saleId) {
        try {
            com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection selectedDevice =
                    BluetoothPrintersConnections.selectFirstPaired();

            if (selectedDevice == null) {
                Toast.makeText(this, "Printer belum terhubung! Transaksi tersimpan.", Toast.LENGTH_LONG).show();
                finish(); // Tetap keluar karena transaksi sudah masuk DB
                return;
            }

            EscPosPrinter printer = new EscPosPrinter(selectedDevice, 203, 48f, 32);

            StringBuilder textToPrint = new StringBuilder();
            textToPrint.append("[C]<b>FADE JAYA TROPHY</b>\n");
            textToPrint.append("[C]Jl. Tuban, Jawa Timur\n");
            textToPrint.append("[C]================================\n");
            textToPrint.append("[L]Faktur: ").append(invoiceNo).append("\n");
            textToPrint.append("[L]Tgl   : ").append(tvTanggal.getText().toString()).append("\n");
            textToPrint.append("[C]--------------------------------\n");

            for (CartItem item : cartList) {
                textToPrint.append("[L]").append(item.getName()).append("\n");
                textToPrint.append("[L]").append(item.getQty()).append(" x ").append((int) item.getPrice())
                        .append("[R]").append((int) (item.getQty() * item.getPrice())).append("\n");
            }

            textToPrint.append("[C]--------------------------------\n");
            textToPrint.append("[L]Total[R]").append(tvTopTotal.getText().toString()).append("\n");
            textToPrint.append("[L]Bayar[R]").append(etDibayar.getText().toString()).append("\n");
            textToPrint.append("[L]Kembali[R]").append(tvKembali.getText().toString()).append("\n");
            textToPrint.append("[C]================================\n");
            textToPrint.append("[C]Terima Kasih\n");

            printer.printFormattedText(textToPrint.toString());
            Toast.makeText(this, "Struk Berhasil Dicetak", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal Print: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // Tetap finish karena data sudah masuk DB
        }
    }

    private String formatRupiah(double number) {
        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        return formatKurensi.format(number);
    }
}