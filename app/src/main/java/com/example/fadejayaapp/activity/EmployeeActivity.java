package com.example.fadejayaapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadejayaapp.R;
import com.example.fadejayaapp.adapter.EmployeeAdapter;
import com.example.fadejayaapp.api.ApiClient;
import com.example.fadejayaapp.model.User;
import com.example.fadejayaapp.model.UserResponse;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeActivity extends AppCompatActivity {

    private RecyclerView rvEmployee;
    private EmployeeAdapter adapter;
    private TextView tvTotalUser, tvActiveUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        rvEmployee = findViewById(R.id.rvEmployee);
        tvTotalUser = findViewById(R.id.tvTotalUser);
        tvActiveUser = findViewById(R.id.tvActiveUser);
        ExtendedFloatingActionButton fab = findViewById(R.id.fabAdd);

        rvEmployee.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, InputEmployeeActivity.class);
            intent.putExtra("ACTION", "add");
            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ApiClient.getService().getUsers().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful() && response.body() != null && response.body().isSuccess()){
                    List<User> list = response.body().getData();

                    // Hitung Statistik Dashboard
                    int total = list.size();
                    int active = 0;
                    for(User u : list) {
                        if(u.getIsActive().equals("1")) active++;
                    }
                    tvTotalUser.setText(String.valueOf(total));
                    tvActiveUser.setText(String.valueOf(active));

                    adapter = new EmployeeAdapter(EmployeeActivity.this, list, user -> {
                        Intent intent = new Intent(EmployeeActivity.this, InputEmployeeActivity.class);
                        intent.putExtra("ACTION", "update");
                        intent.putExtra("DATA", user);
                        startActivity(intent);
                    });
                    rvEmployee.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EmployeeActivity.this, "Gagal Load Data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}