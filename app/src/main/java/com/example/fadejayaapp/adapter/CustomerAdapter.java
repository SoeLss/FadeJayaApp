package com.example.fadejayaapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.model.Customer;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private Context context;
    private List<Customer> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Customer customer);
    }

    public CustomerAdapter(Context context, List<Customer> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void filterList(List<Customer> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer c = list.get(position);

        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone() != null ? c.getPhone() : "-");
        holder.tvAddress.setText(c.getAddress() != null ? c.getAddress() : "-");
        holder.tvDiscount.setText("Diskon " + c.getSpecialDiscount().replace(".00","") + "%");
        holder.tvMemberType.setText("Member " + c.getMemberType());

        // --- LOGIKA WARNA (Stroke & Shadow Effect) ---
        int mainColor = Color.GRAY;

        switch (c.getMemberType()) {
            case "Umum":
                mainColor = Color.parseColor("#00C4FF"); // Cyan/Biru
                break;
            case "Reseller":
                mainColor = Color.parseColor("#FF5252"); // Merah
                break;
            case "Instansi":
                mainColor = Color.parseColor("#FFD740"); // Kuning
                break;
        }

        // Set Warna Stroke & Teks Header
        holder.card.setStrokeColor(mainColor);
        holder.tvMemberType.setTextColor(mainColor);

        // Efek Glow (Android P+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            holder.card.setOutlineSpotShadowColor(mainColor);
            holder.card.setOutlineAmbientShadowColor(mainColor);
        }

        // Klik Edit
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(c));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvDiscount, tvMemberType;
        ImageView btnEdit;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvMemberType = itemView.findViewById(R.id.tvMemberType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            card = itemView.findViewById(R.id.cardCustomer);
        }
    }
}