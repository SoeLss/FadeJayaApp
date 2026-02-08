package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Customer implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("member_type")
    private String member_type;

    @SerializedName("special_discount")
    private String special_discount;

    // === CONSTRUCTOR ===
    public Customer() {
    }

    // === GETTER STANDAR ===
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    // === JEMBATAN "ANTI-ERROR" (Support 2 Gaya Penulisan) ===

    // 1. Untuk menangani error "cannot find symbol getMemberType()"
    public String getMemberType() {
        return member_type;
    }

    // 2. Untuk menangani panggilan gaya database (jaga-jaga)
    public String getMember_type() {
        return member_type;
    }

    // 3. Untuk menangani error sebelumnya "getSpecial_discount"
    public String getSpecial_discount() {
        return special_discount;
    }

    // 4. Jaga-jaga jika ada activity lain panggil "getSpecialDiscount"
    public String getSpecialDiscount() {
        return special_discount;
    }

    @Override
    public String toString() {
        return name;
    }
}