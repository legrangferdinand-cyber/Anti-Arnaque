package com.antiarnaque.app.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.antiarnaque.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Coque de l'application : 4 onglets, navigation sans Navigation Component. */
public final class MainActivity extends AppCompatActivity {

    private AnalyzeFragment analyzeFragment;
    private HistoryFragment historyFragment;
    private ReportsFragment reportsFragment;
    private TipsFragment tipsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        analyzeFragment = new AnalyzeFragment();
        historyFragment = new HistoryFragment();
        reportsFragment = new ReportsFragment();
        tipsFragment = new TipsFragment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_analyze) {
                show(analyzeFragment);
                return true;
            } else if (id == R.id.nav_history) {
                show(historyFragment);
                return true;
            } else if (id == R.id.nav_reports) {
                show(reportsFragment);
                return true;
            } else if (id == R.id.nav_tips) {
                show(tipsFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_analyze);
            show(analyzeFragment);
        }
    }

    private void show(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.app_name);
    }
}
