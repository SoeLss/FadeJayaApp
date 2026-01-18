package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Product implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("series")
    private String series;

    @SerializedName("product_code")
    private String productCode;

    @SerializedName("height")
    private String height;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("type")
    private String type; // Jadi, Bahan, Jasa

    @SerializedName("rack_location")
    private String rackLocation;

    @SerializedName("sell_price")
    private String sellPrice;

    @SerializedName("buy_price")
    private String buyPrice;

    @SerializedName("wholesale_price")
    private String wholesalePrice;

    @SerializedName("stock")
    private String stock;

    @SerializedName("product_image")
    private String productImage;

    @SerializedName("category_id")
    private String categoryId;

    // === CONSTRUCTOR KOSONG (Penting untuk Retrofit) ===
    public Product() {
    }

    // === GETTER ===
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSeries() { return series; }
    public String getProductCode() { return productCode; }
    public String getHeight() { return height; }
    public String getCategoryName() { return categoryName; }
    public String getType() { return type; }
    public String getRackLocation() { return rackLocation; }
    public String getSellPrice() { return sellPrice; }
    public String getBuyPrice() { return buyPrice; }
    public String getWholesalePrice() { return wholesalePrice; }
    public String getStock() { return stock; }
    public String getProductImage() { return productImage; }
    public String getCategoryId() { return categoryId; }

    // === SETTER (Opsional, tapi bagus jika nanti butuh update manual di Java) ===
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSeries(String series) { this.series = series; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setHeight(String height) { this.height = height; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setType(String type) { this.type = type; }
    public void setRackLocation(String rackLocation) { this.rackLocation = rackLocation; }
    public void setSellPrice(String sellPrice) { this.sellPrice = sellPrice; }
    public void setBuyPrice(String buyPrice) { this.buyPrice = buyPrice; }
    public void setWholesalePrice(String wholesalePrice) { this.wholesalePrice = wholesalePrice; }
    public void setStock(String stock) { this.stock = stock; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
}