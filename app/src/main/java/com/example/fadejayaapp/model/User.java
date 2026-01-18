package com.example.fadejayaapp.model;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("id") private String id;
    @SerializedName("username") private String username;
    @SerializedName("password") private String password;
    @SerializedName("full_name") private String fullName;
    @SerializedName("role") private String role;
    @SerializedName("is_active") private String isActive; // "1" atau "0"
    @SerializedName("profile_photo") private String profilePhoto;

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getIsActive() { return isActive; }
    public String getProfilePhoto() { return profilePhoto; }
}