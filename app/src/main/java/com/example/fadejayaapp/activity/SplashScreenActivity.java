package com.example.fadejayaapp.activity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.example.fadejayaapp.R;
import com.example.fadejayaapp.utils.SessionManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Menunggu 2 detik (2000 ms) lalu pindah
        new Handler().postDelayed(() -> {SessionManager session = new SessionManager(getApplicationContext());
            if (session.isLoggedIn()) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}