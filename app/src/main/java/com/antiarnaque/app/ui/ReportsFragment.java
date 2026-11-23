package com.antiarnaque.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antiarnaque.app.R;
import com.antiarnaque.app.data.PreferencesManager;
import com.antiarnaque.app.report.ReportManager;
import com.antiarnaque.app.util.AppExecutors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Vue d'ensemble des signalements collectifs et configuration du backend. */
public final class ReportsFragment extends Fragment {

    private TextView statsText;
    private TextView endpointText;
    private PreferencesManager preferences;
    private ReportManager reportManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        statsText = view.findViewById(R.id.text_report_stats);
        endpointText = view.findViewById(R.id.text_endpoint);
        Button syncButton = view.findViewById(R.id.button_sync);
        Button configButton = view.findViewById(R.id.button_configure);

        preferences = new PreferencesManager(requireContext());
        reportManager = new ReportManager(requireContext());

        syncButton.setOnClickListener(v -> AppExecutors.run(() -> {
            ReportManager.SyncResult result = reportManager.syncPending();
            AppExecutors.onMain(() -> {
                if (!result.configured) {
                    Toast.makeText(requireContext(), R.string.report_no_endpoint,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.report_sync_result, result.success, result.total),
                            Toast.LENGTH_LONG).show();
                }
                refresh();
            });
        }));

        configButton.setOnClickListener(v -> showConfigDialog());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        AppExecutors.run(() -> {
            int total = reportManager.countAll();
            int pending = reportManager.countPending();
            int synced = reportManager.countSynced();
            AppExecutors.onMain(() -> {
                statsText.setText(getString(R.string.report_stats, total, synced, pending));
                String endpoint = preferences.getReportBaseUrl();
                endpointText.setText(getString(R.string.report_endpoint,
                        endpoint.isEmpty() ? getString(R.string.report_endpoint_none) : endpoint));
            });
        });
    }

    private void showConfigDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, 0, padding, 0);

        EditText apiInput = new EditText(requireContext());
        apiInput.setHint(R.string.config_api_hint);
        apiInput.setText(preferences.getApiBaseUrl());
        apiInput.setSingleLine(true);
        layout.addView(apiInput);

        EditText reportInput = new EditText(requireContext());
        reportInput.setHint(R.string.config_report_hint);
        reportInput.setText(preferences.getReportBaseUrl());
        reportInput.setSingleLine(true);
        layout.addView(reportInput);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.config_title)
                .setMessage(R.string.config_message)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String api = apiInput.getText().toString().trim();
                    String report = reportInput.getText().toString().trim();
                    preferences.setApiBaseUrl(api);
                    preferences.setReportBaseUrl(report.isEmpty() ? api : report);
                    preferences.setRemoteAnalysisEnabled(!api.isEmpty());
                    refresh();
                    Toast.makeText(requireContext(), R.string.config_saved, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
