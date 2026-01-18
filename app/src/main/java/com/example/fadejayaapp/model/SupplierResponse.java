package com.example.fadejayaapp.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SupplierResponse {
    @SerializedName("success") private boolean success;
    @SerializedName("data") private List<Supplier> data;

    public boolean isSuccess() { return success; }
    public List<Supplier> getData() { return data; }
}