package com.example.fadejayaapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.model.DashboardMenu;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private Context context;
    private List<DashboardMenu> menuList;
    private OnItemClickListener listener;

    // Interface agar bisa diklik di Fragment
    public interface OnItemClickListener {
        void onItemClick(String menuTitle);
    }

    public DashboardAdapter(Context context, List<DashboardMenu> menuList, OnItemClickListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardMenu menu = menuList.get(position);
        holder.tvTitle.setText(menu.getTitle());
        holder.ivIcon.setImageResource(menu.getIcon());

        holder.cardView.setOnClickListener(v -> {
            listener.onItemClick(menu.getTitle());
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivIcon;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMenuTitle);
            ivIcon = itemView.findViewById(R.id.ivMenuIcon);
            cardView = itemView.findViewById(R.id.cardMenu);
        }
    }
}