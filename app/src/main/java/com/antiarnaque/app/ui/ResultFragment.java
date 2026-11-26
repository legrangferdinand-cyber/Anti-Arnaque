package com.antiarnaque.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.antiarnaque.app.R;
import com.antiarnaque.app.data.HistoryDao;
import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.HistoryEntry;
import com.antiarnaque.app.model.RiskLevel;
import com.antiarnaque.app.report.ReportManager;
import com.antiarnaque.app.util.AppExecutors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Écran Résultat du mockup : dernière analyse enregistrée,score et indices. */
public final class ResultFragment extends Fragment {

    private View card;
    private TextView emptyView;
    private TextView verdict;
    private TextView score;
    private TextView risk;
    private TextView badge;
    private TextView reasons;
    private ProgressBar progress;
    private Button reportButton;
    private HistoryDao historyDao;
    private HistoryEntry current;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        card = view.findViewById(R.id.result_card);
        emptyView = view.findViewById(R.id.result_empty);
        verdict = view.findViewById(R.id.result_verdict);
        score = view.findViewById(R.id.result_score);
        risk = view.findViewById(R.id.result_risk);
        badge = view.findViewById(R.id.result_badge);
        reasons = view.findViewById(R.id.result_reasons);
        progress = view.findViewById(R.id.result_progress);
        reportButton = view.findViewById(R.id.result_report_button);

        historyDao = new HistoryDao(requireContext());
        reportButton.setOnClickListener(v -> askReport());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppExecutors.run(() -> {
            List<HistoryEntry> entries = historyDao.getAll();
            HistoryEntry latest = entries.isEmpty() ? null : entries.get(0);
            AppExecutors.onMain(() -> display(latest));
        });
    }

    private void display(@Nullable HistoryEntry entry) {
        current = entry;
        if (entry == null) {
            card.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        emptyView.setVisibility(View.GONE);
        card.setVisibility(View.VISIBLE);

        boolean spam = entry.getLevel() == RiskLevel.ELEVE;
        verdict.setText(spam ? R.string.verdict_danger : R.string.verdict_safe);
        score.setText(getString(R.string.result_score, entry.getScorePercent()));
        risk.setText(getString(R.string.result_risk,
                entry.getLevel().getLabel().toLowerCase(Locale.FRANCE)));
        badge.setText(spam ? R.string.result_badge_spam : R.string.result_badge_ham);
        reasons.setText(entry.getReasons());
        progress.setProgress(entry.getScorePercent());

        int color = ContextCompat.getColor(requireContext(), spam
                ? R.color.risk_high : R.color.risk_low);
        score.setTextColor(color);
        risk.setTextColor(color);
        badge.setTextColor(color);
    }

    private void askReport() {
        if (current == null) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.report_dialog_title)
                .setMessage(R.string.report_dialog_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.report_button, (dialog, which) -> {
                    HistoryEntry entry = current;
                    AnalysisResult result = new AnalysisResult(entry.getMessage(), entry.getScore(),
                            entry.getLevel() == RiskLevel.ELEVE ? "spam" : "legit",
                            entry.getLevel(), null, null, entry.getTimestamp(), entry.getEngine());
                    AppExecutors.run(() -> {
                        ReportManager.ReportResult outcome = new ReportManager(requireContext())
                                .submit(entry.getMessage(), result);
                        AppExecutors.onMain(() -> Toast.makeText(requireContext(),
                                outcome.sent ? R.string.report_sent : R.string.report_saved,
                                Toast.LENGTH_LONG).show());
                    });
                })
                .show();
    }
}
