package com.example.fadejayaapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    // Fungsi Utama: Mengubah Uri (dari Galeri) menjadi File (untuk Upload)
    public static File getFile(Context context, Uri uri) {
        if (uri == null) return null;

        try {
            ContentResolver contentResolver = context.getContentResolver();

            // 1. Ambil ekstensi file (jpg/png)
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = contentResolver.getType(uri);
            String extension = mime.getExtensionFromMimeType(type);
            if (extension == null) extension = "jpg"; // Default jpg jika tidak terdeteksi

            // 2. Buat file sementara di folder cache aplikasi
            File file = new File(context.getCacheDir(), "temp_upload." + extension);

            // 3. Salin data dari Uri ke File sementara
            InputStream inputStream = contentResolver.openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(file);

            copyStream(inputStream, outputStream);

            outputStream.close();
            inputStream.close();

            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fungsi Pembantu: Menyalin aliran data (Stream)
    private static void copyStream(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}