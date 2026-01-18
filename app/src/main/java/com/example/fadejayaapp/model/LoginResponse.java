package com.example.fadejayaapp.model;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UserData data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public UserData getData() { return data; }

    // Class di dalam Class (Nested Class untuk data user)
    public class UserData {
        @SerializedName("id")
        private String id;
        @SerializedName("username")
        private String username;
        @SerializedName("full_name")
        private String fullName;
        @SerializedName("role")
        private String role;
        @SerializedName("profile_photo")
        private String profilePhoto;

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public String getProfilePhoto() { return profilePhoto; }
    }
}