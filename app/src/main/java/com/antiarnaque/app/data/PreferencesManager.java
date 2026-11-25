package com.antiarnaque.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.antiarnaque.app.BuildConfig;

/** Paramètres de connexion au backend (Flask ou Fonction Firebase). */
public final class PreferencesManager {

    private static final String PREF_NAME = "anti_arnaque_prefs";
    private static final String KEY_API_URL = "api_base_url";
    private static final String KEY_REPORT_URL = "report_base_url";
    private static final String KEY_REMOTE_ENABLED = "remote_analysis_enabled";

    private final SharedPreferences preferences;

    public PreferencesManager(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getApiBaseUrl() {
        String value = preferences.getString(KEY_API_URL, BuildConfig.DEFAULT_API_URL);
        return TextUtils.isEmpty(value) ? "" : value.trim();
    }

    public void setApiBaseUrl(String url) {
        preferences.edit().putString(KEY_API_URL, url == null ? "" : url.trim()).apply();
    }

    public String getReportBaseUrl() {
        String value = preferences.getString(KEY_REPORT_URL, getApiBaseUrl());
        return TextUtils.isEmpty(value) ? "" : value.trim();
    }

    public void setReportBaseUrl(String url) {
        preferences.edit().putString(KEY_REPORT_URL, url == null ? "" : url.trim()).apply();
    }

    public boolean isRemoteAnalysisEnabled() {
        return preferences.getBoolean(KEY_REMOTE_ENABLED, !TextUtils.isEmpty(getApiBaseUrl()));
    }

    public void setRemoteAnalysisEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_REMOTE_ENABLED, enabled).apply();
    }
}
