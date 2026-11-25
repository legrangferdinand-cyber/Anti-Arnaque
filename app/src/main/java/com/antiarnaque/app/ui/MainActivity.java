package com.antiarnaque.app.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.antiarnaque.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Coque de l'application : 5 onglets, navigation sans Navigation Component. */
public final class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private AccueilFragment accueilFragment;
    private AnalyzeFragment analyzeFragment;
    private HistoryFragment historyFragment;
    private ReportsFragment reportsFragment;
    private TipsFragment tipsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accueilFragment = new AccueilFragment();
        analyzeFragment = new AnalyzeFragment();
        historyFragment = new HistoryFragment();
        reportsFragment = new ReportsFragment();
        tipsFragment = new TipsFragment();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target = fragmentFor(item.getItemId());
            if (target == null) {
                return false;
            }
            show(target);
            return true;
        });

        if (savedInstanceState == null) {
            selectTab(R.id.nav_home);
        } else {
            Fragment current = fragmentFor(bottomNav.getSelectedItemId());
            show(current != null ? current : accueilFragment);
        }
    }

    /** Utilisé par l'accueil pour ouvrir une autre rubrique. */
    public void selectTab(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    private Fragment fragmentFor(int itemId) {
        if (itemId == R.id.nav_home) {
            return accueilFragment;
        } else if (itemId == R.id.nav_analyze) {
            return analyzeFragment;
        } else if (itemId == R.id.nav_history) {
            return historyFragment;
        } else if (itemId == R.id.nav_reports) {
            return reportsFragment;
        } else if (itemId == R.id.nav_tips) {
            return tipsFragment;
        }
        return null;
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
