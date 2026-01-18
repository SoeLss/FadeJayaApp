package com.example.fadejayaapp.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("new_photo") // Harus sama persis dengan JSON PHP
    private String newPhoto;

    // Getter
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getNewPhoto() { return newPhoto; }
}