package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CustomerResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message; // Tambahan opsional jika PHP mengirim pesan error

    @SerializedName("data")
    private List<Customer> data;

    // === GETTER ===
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Customer> getData() {
        return data;
    }
}