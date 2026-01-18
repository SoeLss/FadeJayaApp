package com.example.fadejayaapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.model.Supplier;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {

    private Context context;
    private List<Supplier> list;
    private String userRole; // Variable Role
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Supplier supplier);
    }

    // Constructor terima Role
    public SupplierAdapter(Context context, List<Supplier> list, String userRole, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.userRole = userRole;
        this.listener = listener;
    }

    public void filterList(List<Supplier> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_supplier_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Supplier s = list.get(position);

        holder.tvStoreName.setText(s.getStoreName());
        holder.tvContact.setText("Sales : " + (s.getContactPerson() != null ? s.getContactPerson() : "-"));
        holder.tvPhone.setText(s.getPhone());
        holder.tvAddress.setText(s.getAddress());
        holder.tvDesc.setText("Ket : " + (s.getGoodsDescription() != null ? s.getGoodsDescription() : "-"));

        // Efek Glow Biru Muda (Sesuai Gambar)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            holder.card.setOutlineSpotShadowColor(Color.parseColor("#00E5FF")); // Cyan Glow
            holder.card.setOutlineAmbientShadowColor(Color.parseColor("#00E5FF"));
        }

        // LOGIKA KLIK EDIT
        holder.btnEdit.setOnClickListener(v -> {
            if (userRole != null && userRole.equalsIgnoreCase("Kasir")) {
                Toast.makeText(context, "Only Owner & Admin", Toast.LENGTH_SHORT).show();
            } else {
                listener.onEdit(s);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvContact, tvPhone, tvAddress, tvDesc;
        ImageView btnEdit;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            card = itemView.findViewById(R.id.cardSupplier);
        }
    }
}