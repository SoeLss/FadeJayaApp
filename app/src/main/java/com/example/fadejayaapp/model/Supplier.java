package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Supplier implements Serializable {
    @SerializedName("id") private String id;
    @SerializedName("store_name") private String storeName;
    @SerializedName("contact_person") private String contactPerson;
    @SerializedName("phone") private String phone;
    @SerializedName("address") private String address;
    @SerializedName("goods_description") private String goodsDescription;

    // Getters
    public String getId() { return id; }
    public String getStoreName() { return storeName; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getGoodsDescription() { return goodsDescription; }
}