package com.example.fadejayaapp.api;

import com.example.fadejayaapp.model.LoginResponse;
import com.example.fadejayaapp.model.ProductResponse;
import com.example.fadejayaapp.model.SupplierResponse;
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.model.CustomerResponse; // <--- TAMBAHKAN INI
import com.example.fadejayaapp.model.UploadResponse;
import com.example.fadejayaapp.model.UserResponse;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // 1. AMBIL DATA PRODUK (Lewat Script Custom get_products.php)
    // URL Asli: https://api.robotrakitan.my.id/get_products.php
    // Kita gunakan "../" karena Base URL di ApiClient berakhiran "/api.php/"
    @GET("../get_products.php")
    Call<ProductResponse> getProducts();

    // 2. UPLOAD GAMBAR PRODUK
    @Multipart
    @POST("../upload.php")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);

    // 3. LOGIN USER
    @FormUrlEncoded
    @POST("../login.php")
    Call<LoginResponse> loginUser(
            @Field("username") String username,
            @Field("password") String password
    );

    // 4. UPDATE FOTO PROFIL
    @Multipart
    @POST("../update_photo.php")
    Call<UploadResponse> updateProfilePhoto(
            @Part("id_user") okhttp3.RequestBody idUser,
            @Part MultipartBody.Part photo
    );
    @Multipart
    @POST("../add_product.php")
    Call<UploadResponse> addProduct(
            @Part("name") okhttp3.RequestBody name,
            @Part("category_id") okhttp3.RequestBody categoryId,
            @Part("series") okhttp3.RequestBody series,
            @Part("product_code") okhttp3.RequestBody productCode,
            @Part("height") okhttp3.RequestBody height,
            @Part("type") okhttp3.RequestBody type,
            @Part("rack_location") okhttp3.RequestBody rackLocation,
            @Part("buy_price") okhttp3.RequestBody buyPrice,
            @Part("sell_price") okhttp3.RequestBody sellPrice,
            @Part("wholesale_price") okhttp3.RequestBody wholesalePrice,
            @Part("stock") okhttp3.RequestBody stock,
            @Part MultipartBody.Part image // Gambar (Bisa Null)
    );
    @Multipart
    @POST("../edit_product.php")
    Call<UploadResponse> editProduct(
            @Part("id") okhttp3.RequestBody id, // ID Produk yg diedit
            @Part("name") okhttp3.RequestBody name,
            @Part("category_id") okhttp3.RequestBody categoryId,
            @Part("series") okhttp3.RequestBody series,
            @Part("product_code") okhttp3.RequestBody productCode,
            @Part("height") okhttp3.RequestBody height,
            @Part("type") okhttp3.RequestBody type,
            @Part("rack_location") okhttp3.RequestBody rackLocation,
            @Part("buy_price") okhttp3.RequestBody buyPrice,
            @Part("sell_price") okhttp3.RequestBody sellPrice,
            @Part("wholesale_price") okhttp3.RequestBody wholesalePrice,
            @Part("stock") okhttp3.RequestBody stock,
            @Part MultipartBody.Part image // Gambar (Boleh Null)
    );
    @GET("../get_customers.php")
    Call<CustomerResponse> getCustomers();

    @FormUrlEncoded
    @POST("../save_customer.php")
    Call<UploadResponse> saveCustomer(
            @Field("action") String action,
            @Field("id") String id,
            @Field("name") String name,
            @Field("phone") String phone,
            @Field("address") String address,
            @Field("member_type") String memberType,
            @Field("special_discount") String discount // Kirim ke PHP sebagai 'special_discount'
    );
    @GET("../get_suppliers.php")
    Call<SupplierResponse> getSuppliers();

    @FormUrlEncoded
    @POST("../save_supplier.php")
    Call<UploadResponse> saveSupplier(
            @Field("action") String action,
            @Field("id") String id,
            @Field("store_name") String storeName,
            @Field("contact_person") String contactPerson,
            @Field("phone") String phone,
            @Field("address") String address,
            @Field("goods_description") String goodsDescription
    );
    @GET("../get_users.php")
    Call<UserResponse> getUsers();

    @FormUrlEncoded
    @POST("../save_user.php")
    Call<UploadResponse> saveUser(
            @Field("action") String action,
            @Field("id") String id,
            @Field("username") String username,
            @Field("full_name") String fullName,
            @Field("role") String role,
            @Field("is_active") String isActive, // Kirim "1" atau "0"
            @Field("password") String password
    );
}