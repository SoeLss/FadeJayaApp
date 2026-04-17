package com.example.fadejayaapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;
    private String userRole; // Variable untuk Role

    public interface OnItemClickListener {
        void onEditClick(Product product);
    }

    // Constructor UPDATE: Terima Role
    public ProductAdapter(Context context, List<Product> productList, String userRole, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.userRole = userRole; // Simpan role
        this.listener = listener;
    }

    // Method Filter
    public void filterList(List<Product> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pastikan nama layout XML sesuai dengan yang Anda miliki
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.tvName.setText(p.getName());

        // Setup variabel untuk spesifikasi
        String kategori = p.getCategoryName() != null ? p.getCategoryName() : "Umum";
        String jenisText = p.getType() != null ? p.getType() : "-";
        String labelJenis = "Jenis"; // Default

        // === LOGIKA KHUSUS KATEGORI PIALA ===
        // Bisa cek berdasarkan ID kategori (misal "1") atau Nama Kategorinya ("Piala")
        if ("1".equals(p.getCategoryId()) || "Piala".equalsIgnoreCase(kategori)) {
            labelJenis = "Komponen";

            // Pecah teks komponen berdasarkan koma
            String[] listKomponen = jenisText.split(",");

            // Batasi tampilan maksimal 3 komponen agar Card tidak kepanjangan
            if (listKomponen.length > 3) {
                jenisText = listKomponen[0].trim() + ", " +
                        listKomponen[1].trim() + ", " +
                        listKomponen[2].trim() + " ...";
            }

            // Tampilkan Tombol Info
            holder.btnInfoKomponen.setVisibility(View.VISIBLE);

            // Simpan teks komponen asli untuk ditampilkan penuh di pop-up
            String fullKomponen = p.getType() != null ? p.getType().replace(",", "\n") : "";

            // Aksi klik tombol Info
            holder.btnInfoKomponen.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Detail Komponen Piala")
                        .setMessage(fullKomponen)
                        .setPositiveButton("Tutup", null)
                        .show();
            });

        } else {
            // Jika BUKAN Piala, sembunyikan tombol info
            holder.btnInfoKomponen.setVisibility(View.GONE);
        }

        // Gabungkan semua teks spesifikasi
        String specs = "Seri : " + (p.getSeries() != null ? p.getSeries() : "-") + "\n" +
                "Code : " + (p.getProductCode() != null ? p.getProductCode() : "-") + "\n" +
                "Ukuran : " + (p.getHeight() != null ? p.getHeight() : "-") + "\n" +
                "Kategori : " + kategori + "\n" +
                labelJenis + " : " + jenisText + "\n" +
                "Lokasi Rak : " + (p.getRackLocation() != null ? p.getRackLocation() : "-");

        holder.tvSpecs.setText(specs);

        // === LOGIKA ROLE UNTUK HARGA BELI ===
        String displayBuyPrice;
        if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
            displayBuyPrice = "Harga Beli : Only Owner & Admin";
        } else {
            displayBuyPrice = "Harga Beli : " + formatRupiah(p.getBuyPrice());
        }

        String prices = "Harga Jual : " + formatRupiah(p.getSellPrice()) + "\n" +
                displayBuyPrice + "\n" +
                "Harga Grosir : " + formatRupiah(p.getWholesalePrice()) + "\n" +
                "Stok : " + p.getStock();
        holder.tvPrices.setText(prices);

        // Load Gambar
        if (p.getProductImage() != null && !p.getProductImage().isEmpty()) {
            String imageUrl = "https://api.robotrakitan.my.id/uploads/" + p.getProductImage();
            Glide.with(context).load(imageUrl).centerCrop().placeholder(R.drawable.ic_launcher_background).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        // === LOGIKA KLIK EDIT ===
        holder.btnEdit.setOnClickListener(v -> {
            if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                // Jika Kasir, Tampilkan Toast
                Toast.makeText(context, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
            } else {
                // Jika Admin/Owner, Lanjut Edit
                listener.onEditClick(p);
            }
        });
    }

    private String formatRupiah(String number) {
        if(number == null) return "Rp 0";
        try {
            double amount = Double.parseDouble(number);
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return format.format(amount);
        } catch (NumberFormatException e) {
            return "Rp " + number;
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecs, tvPrices;
        ImageView imgProduct, btnEdit, btnInfoKomponen; // TAMBAHKAN btnInfoKomponen DISINI

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecs = itemView.findViewById(R.id.tvSpecs);
            tvPrices = itemView.findViewById(R.id.tvPrices);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnInfoKomponen = itemView.findViewById(R.id.btnInfoKomponen); // INISIALISASI ID DISINI
        }
    }
}