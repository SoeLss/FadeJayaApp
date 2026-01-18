package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // JSON dari get_products.php menggunakan key "data"
    @SerializedName("data")
    private List<Product> data;

    // === GETTER ===
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Product> getData() {
        return data;
    }
}