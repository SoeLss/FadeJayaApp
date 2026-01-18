package com.example.fadejayaapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.fadejayaapp.activity.LoginActivity; // Pastikan package ini sesuai lokasi LoginActivity Anda

import java.util.HashMap;

public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    // Nama File SharedPreference
    private static final String PREF_NAME = "FadeJayaSession";
    private static final String IS_LOGIN = "IsLoggedIn";

    // Key Data User
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NAME = "name";
    public static final String KEY_ROLE = "role";

    // Key Foto Profil
    public static final String KEY_PHOTO = "profile_photo";

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, 0); // 0 - Private Mode
        editor = pref.edit();
    }

    /**
     * FUNGSI LOGIN SESSION (FINAL)
     * Menerima 5 parameter termasuk FOTO agar tersimpan saat login awal.
     */
    public void createLoginSession(String id, String username, String name, String role, String photo) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ROLE, role);

        // Simpan Foto Profil (Jika null/kosong dari database, simpan string kosong)
        if (photo == null) {
            editor.putString(KEY_PHOTO, "");
        } else {
            editor.putString(KEY_PHOTO, photo);
        }

        editor.commit(); // Simpan perubahan
    }

    /**
     * FUNGSI KHUSUS UPDATE FOTO
     * Dipanggil saat user sukses upload foto baru di BerandaFragment
     */
    public void updatePhotoSession(String newPhotoFileName) {
        editor.putString(KEY_PHOTO, newPhotoFileName);
        editor.commit();
    }

    /**
     * AMBIL DATA USER
     * Mengembalikan semua data user (termasuk foto) dalam bentuk HashMap
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_ID, pref.getString(KEY_ID, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));
        user.put(KEY_PHOTO, pref.getString(KEY_PHOTO, null));
        return user;
    }

    // Cek Status Login
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * LOGOUT USER (FINAL)
     * Menghapus session dan melempar user ke halaman Login
     */
    public void logoutUser() {
        // 1. Hapus Semua Data di Shared Preferences
        editor.clear();
        editor.commit();

        // 2. Arahkan User ke LoginActivity
        Intent i = new Intent(context, LoginActivity.class);

        // Tutup semua Activity sebelumnya agar user tidak bisa tekan tombol Back ke menu
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Jalankan Intent
        context.startActivity(i);
    }
}