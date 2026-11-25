package com.antiarnaque.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.antiarnaque.app.R;

/** Écran de démarrage : affiche le logo et le nom de l'application. */
public final class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1600L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, SPLASH_DURATION_MS);
    }
}
