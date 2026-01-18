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
import com.bumptech.glide.Glide;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.model.User;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private Context context;
    private List<User> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(User user);
    }

    public EmployeeAdapter(Context context, List<User> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_employee_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User u = list.get(position);

        holder.tvName.setText(u.getFullName());
        holder.tvUsername.setText("@" + u.getUsername());
        holder.tvRole.setText(u.getRole().toUpperCase());

        // LOAD FOTO (Default jika kosong)
        if (u.getProfilePhoto() != null && !u.getProfilePhoto().isEmpty()) {
            String url = "https://api.robotrakitan.my.id/uploads/" + u.getProfilePhoto();
            Glide.with(context).load(url).circleCrop().into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_menu_karyawan); // Icon default
        }

        // --- LOGIC DESIGN KREATIF ---

        // 1. Warna Strip Berdasarkan Role
        String role = u.getRole();
        int color = Color.GRAY;
        if(role.equalsIgnoreCase("Owner")) color = Color.parseColor("#FFD700"); // Emas
        else if(role.equalsIgnoreCase("Admin")) color = Color.parseColor("#00BCD4"); // Biru
        else if(role.equalsIgnoreCase("Kasir")) color = Color.parseColor("#4CAF50"); // Hijau

        holder.viewRoleStrip.setBackgroundColor(color);

        // 2. Status Indicator
        if (u.getIsActive().equals("1")) {
            holder.tvStatus.setText("ACTIVE");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Hijau
        } else {
            holder.tvStatus.setText("INACTIVE");
            holder.tvStatus.setTextColor(Color.RED);
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(u));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvUsername, tvStatus;
        ImageView imgAvatar;
        View viewRoleStrip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            viewRoleStrip = itemView.findViewById(R.id.viewRoleStrip);
        }
    }
}