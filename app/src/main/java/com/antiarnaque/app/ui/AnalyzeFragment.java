package com.antiarnaque.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.antiarnaque.app.R;
import com.antiarnaque.app.analysis.AnalyzerRepository;
import com.antiarnaque.app.data.HistoryDao;
import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.RiskLevel;
import com.antiarnaque.app.report.ReportManager;
import com.antiarnaque.app.util.AppExecutors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Écran principal : coller un SMS, obtenir un score et une explication. */
public final class AnalyzeFragment extends Fragment {

    private EditText messageInput;
    private Button analyzeButton;
    private Button exampleButton;
    private Button clearButton;
    private ProgressBar loading;
    private LinearLayout resultCard;
    private TextView scoreValue;
    private TextView levelValue;
    private TextView verdictValue;
    private TextView reasonsValue;
    private TextView signalsValue;
    private TextView engineValue;
    private Button reportButton;

    private AnalyzerRepository analyzer;
    private HistoryDao historyDao;
    private AnalysisResult lastResult;
    private String lastMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyze, container, false);

        messageInput = view.findViewById(R.id.edit_message);
        analyzeButton = view.findViewById(R.id.button_analyze);
        exampleButton = view.findViewById(R.id.button_example);
        clearButton = view.findViewById(R.id.button_clear);
        loading = view.findViewById(R.id.progress_analyze);
        resultCard = view.findViewById(R.id.card_result);
        scoreValue = view.findViewById(R.id.text_score);
        levelValue = view.findViewById(R.id.text_level);
        verdictValue = view.findViewById(R.id.text_verdict);
        reasonsValue = view.findViewById(R.id.text_reasons);
        signalsValue = view.findViewById(R.id.text_signals);
        engineValue = view.findViewById(R.id.text_engine);
        reportButton = view.findViewById(R.id.button_report);

        analyzer = new AnalyzerRepository(requireContext());
        historyDao = new HistoryDao(requireContext());

        analyzeButton.setOnClickListener(v -> runAnalysis());
        exampleButton.setOnClickListener(v -> messageInput.setText(getString(R.string.demo_fraud_sms)));
        clearButton.setOnClickListener(v -> reset());
        reportButton.setOnClickListener(v -> askReport());

        return view;
    }

    private void runAnalysis() {
        String message = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            messageInput.setError(getString(R.string.error_empty_message));
            return;
        }
        if (message.length() < 5) {
            messageInput.setError(getString(R.string.error_short_message));
            return;
        }

        setBusy(true);
        AppExecutors.run(() -> {
            AnalysisResult result;
            try {
                result = analyzer.analyze(message);
                historyDao.insert(result);
            } catch (Exception e) {
                AppExecutors.onMain(() -> {
                    setBusy(false);
                    Toast.makeText(requireContext(), R.string.error_analysis_failed,
                            Toast.LENGTH_SHORT).show();
                });
                return;
            }
            AppExecutors.onMain(() -> {
                setBusy(false);
                display(result, message);
            });
        });
    }

    private void display(AnalysisResult result, String message) {
        lastResult = result;
        lastMessage = message;

        resultCard.setVisibility(View.VISIBLE);
        scoreValue.setText(getString(R.string.score_format, result.getScorePercent()));
        levelValue.setText(result.getLevel().getLabel());
        verdictValue.setText(result.isSpam()
                ? R.string.verdict_danger : R.string.verdict_safe);

        int color;
        if (result.getLevel() == RiskLevel.ELEVE) {
            color = ContextCompat.getColor(requireContext(), R.color.risk_high);
        } else if (result.getLevel() == RiskLevel.MOYEN) {
            color = ContextCompat.getColor(requireContext(), R.color.risk_medium);
        } else {
            color = ContextCompat.getColor(requireContext(), R.color.risk_low);
        }
        levelValue.setTextColor(color);
        scoreValue.setTextColor(color);

        reasonsValue.setText(result.getReasonsText());
        signalsValue.setText(result.getSignalsText());
        engineValue.setText(getString(R.string.engine_format, result.getEngine()));
        reportButton.setEnabled(true);
        reportButton.setText(R.string.report_button);
    }

    private void askReport() {
        if (lastResult == null || lastMessage == null) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.report_dialog_title)
                .setMessage(R.string.report_dialog_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.report_button, (dialog, which) -> {
                    AnalysisResult result = lastResult;
                    String message = lastMessage;
                    AppExecutors.run(() -> {
                        ReportManager.ReportResult outcome = new ReportManager(requireContext())
                                .submit(message, result);
                        AppExecutors.onMain(() -> {
                            reportButton.setText(R.string.report_done);
                            reportButton.setEnabled(false);
                            Toast.makeText(requireContext(),
                                    outcome.sent ? R.string.report_sent : R.string.report_saved,
                                    Toast.LENGTH_LONG).show();
                        });
                    });
                })
                .show();
    }

    private void setBusy(boolean busy) {
        loading.setVisibility(busy ? View.VISIBLE : View.GONE);
        analyzeButton.setEnabled(!busy);
        exampleButton.setEnabled(!busy);
    }

    private void reset() {
        messageInput.setText("");
        messageInput.setError(null);
        resultCard.setVisibility(View.GONE);
        lastResult = null;
        lastMessage = null;
        reportButton.setEnabled(false);
    }
}
