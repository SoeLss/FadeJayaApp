package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("invoice")
    private String invoice;

    @SerializedName("sale_id")
    private String saleId;

    // Getter
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getInvoice() { return invoice; }
    public String getSaleId() { return saleId; }
}