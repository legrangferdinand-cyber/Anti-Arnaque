package com.antiarnaque.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antiarnaque.app.R;
import com.antiarnaque.app.data.HistoryDao;
import com.antiarnaque.app.data.PreferencesManager;
import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.HistoryEntry;
import com.antiarnaque.app.model.RiskLevel;
import com.antiarnaque.app.report.ReportManager;
import com.antiarnaque.app.util.AppExecutors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

/** Formulaire de signalement volontaire du mockup + configuration du backend. */
public final class ReportsFragment extends Fragment {

    private TextView statsText;
    private TextView endpointText;
    private TextView previewText;
    private Spinner reasonSpinner;
    private EditText commentInput;
    private Button sendButton;
    private PreferencesManager preferences;
    private ReportManager reportManager;
    private HistoryDao historyDao;
    private HistoryEntry lastEntry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        statsText = view.findViewById(R.id.text_report_stats);
        endpointText = view.findViewById(R.id.text_endpoint);
        previewText = view.findViewById(R.id.text_report_preview);
        reasonSpinner = view.findViewById(R.id.spinner_reason);
        commentInput = view.findViewById(R.id.edit_comment);
        sendButton = view.findViewById(R.id.button_send_report);
        Button syncButton = view.findViewById(R.id.button_sync);
        Button configButton = view.findViewById(R.id.button_configure);

        preferences = new PreferencesManager(requireContext());
        reportManager = new ReportManager(requireContext());
        historyDao = new HistoryDao(requireContext());

        sendButton.setOnClickListener(v -> submitReport());
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
        loadPreview();
    }

    private void loadPreview() {
        AppExecutors.run(() -> {
            List<HistoryEntry> entries = historyDao.getAll();
            HistoryEntry latest = entries.isEmpty() ? null : entries.get(0);
            AppExecutors.onMain(() -> {
                lastEntry = latest;
                if (latest == null) {
                    previewText.setText(R.string.report_preview_none);
                } else {
                    String message = latest.getMessage();
                    String preview = message.length() > 160
                            ? message.substring(0, 160) + "…" : message;
                    previewText.setText(preview);
                }
            });
        });
    }

    private void submitReport() {
        if (lastEntry == null) {
            Toast.makeText(requireContext(), R.string.report_error_no_message,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int position = reasonSpinner.getSelectedItemPosition();
        if (position <= 0) {
            Toast.makeText(requireContext(), R.string.report_error_no_reason,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String reason = reasonSpinner.getSelectedItem().toString();
        String comment = commentInput.getText().toString().trim();
        String fullReason = comment.isEmpty() ? reason : reason + " — " + comment;

        AnalysisResult result = new AnalysisResult(lastEntry.getMessage(), lastEntry.getScore(),
                lastEntry.getLevel() == RiskLevel.ELEVE ? "spam" : "legit",
                lastEntry.getLevel(), Collections.singletonList(fullReason), null,
                lastEntry.getTimestamp(), lastEntry.getEngine());

        AppExecutors.run(() -> {
            ReportManager.ReportResult outcome = reportManager.submit(lastEntry.getMessage(), result);
            AppExecutors.onMain(() -> {
                Toast.makeText(requireContext(),
                        outcome.sent ? R.string.report_sent : R.string.report_saved,
                        Toast.LENGTH_LONG).show();
                commentInput.setText("");
                refresh();
            });
        });
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
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_config, null);
        EditText apiInput = dialogView.findViewById(R.id.edit_api_url);
        EditText reportInput = dialogView.findViewById(R.id.edit_report_url);
        apiInput.setText(preferences.getApiBaseUrl());
        reportInput.setText(preferences.getReportBaseUrl());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.config_title)
                .setMessage(R.string.config_message)
                .setView(dialogView)
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
