package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Customer implements Serializable {
    @SerializedName("id") private String id;
    @SerializedName("name") private String name;
    @SerializedName("phone") private String phone;
    @SerializedName("address") private String address;
    @SerializedName("member_type") private String memberType;
    @SerializedName("special_discount") private String specialDiscount; // Sesuai DB

    // Getter
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getMemberType() { return memberType; }
    public String getSpecialDiscount() { return specialDiscount; }
}