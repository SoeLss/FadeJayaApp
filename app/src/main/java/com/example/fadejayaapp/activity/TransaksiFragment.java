package com.example.fadejayaapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.fadejayaapp.R;
// Pastikan Activity ini nanti dibuat
import com.example.fadejayaapp.activity.PenjualanActivity;
import com.example.fadejayaapp.activity.PembelianActivity;
import com.example.fadejayaapp.activity.ReturActivity;

public class TransaksiFragment extends Fragment {

    private CardView cardPenjualan, cardPembelian, cardRetur;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaksi, container, false);

        // Inisialisasi View
        cardPenjualan = view.findViewById(R.id.cardPenjualan);
        cardPembelian = view.findViewById(R.id.cardPembelian);
        cardRetur = view.findViewById(R.id.cardRetur);

        // Event Listener
        cardPenjualan.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PenjualanActivity.class);
            startActivity(intent);
        });

        cardPembelian.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PembelianActivity.class);
            startActivity(intent);
        });

        cardRetur.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ReturActivity.class);
            startActivity(intent);
        });

        return view;
    }
}