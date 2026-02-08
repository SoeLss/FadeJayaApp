package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {

    // === INI SOLUSINYA ===
    // Kita paksa agar saat dikirim namanya jadi "product_id" (sesuai database)
    @SerializedName("product_id")
    private int productId;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("price") // Sesuai kolom di PHP ($item['price'])
    private double price;

    @SerializedName("qty")   // Sesuai kolom di PHP ($item['qty'])
    private int qty;

    // Constructor Kosong
    public CartItem() {}

    // === GETTER & SETTER ===
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}