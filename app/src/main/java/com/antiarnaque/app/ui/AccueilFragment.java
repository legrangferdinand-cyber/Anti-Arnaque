package com.antiarnaque.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antiarnaque.app.R;
import com.antiarnaque.app.data.HistoryDao;
import com.antiarnaque.app.data.ReportDao;
import com.antiarnaque.app.model.HistoryEntry;
import com.antiarnaque.app.util.AppExecutors;

import java.util.List;
import java.util.Locale;

/**
 * Première interface de l'application : présentation, accès rapides et
 * indicateurs locaux (analyses enregistrées, dernier résultat, signalements
 * en attente). Aucune donnée n'est envoyée depuis cet écran.
 */
public final class AccueilFragment extends Fragment {

    private TextView analysesCount;
    private TextView lastResult;
    private TextView pendingReports;
    private HistoryDao historyDao;
    private ReportDao reportDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accueil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        analysesCount = view.findViewById(R.id.home_analyses_count);
        lastResult = view.findViewById(R.id.home_last_result);
        pendingReports = view.findViewById(R.id.home_pending_reports);

        historyDao = new HistoryDao(requireContext());
        reportDao = new ReportDao(requireContext());

        view.findViewById(R.id.home_action_analyze).setOnClickListener(v -> openTab(R.id.nav_analyze));
        view.findViewById(R.id.home_action_history).setOnClickListener(v -> openTab(R.id.nav_history));
        view.findViewById(R.id.home_action_report).setOnClickListener(v -> openTab(R.id.nav_reports));
        view.findViewById(R.id.home_action_tips).setOnClickListener(v -> openTab(R.id.nav_tips));

        refreshStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStats();
    }

    private void openTab(int itemId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectTab(itemId);
        }
    }

    /** Les accès SQLite se font hors du thread UI. */
    private void refreshStats() {
        AppExecutors.run(() -> {
            final List<HistoryEntry> entries = historyDao.getAll();
            final int count = entries.size();
            final String last;
            if (count == 0) {
                last = getString(R.string.home_last_none);
            } else {
                HistoryEntry recent = entries.get(0);
                last = String.format(Locale.getDefault(), getString(R.string.home_last_format),
                        recent.getLevel().getLabel(), recent.getScorePercent());
            }
            final int pending = reportDao.countPending();

            AppExecutors.onMain(() -> {
                if (!isAdded()) {
                    return;
                }
                analysesCount.setText(String.valueOf(count));
                lastResult.setText(last);
                pendingReports.setText(String.valueOf(pending));
            });
        });
    }
}
