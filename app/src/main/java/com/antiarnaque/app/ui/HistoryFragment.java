package com.antiarnaque.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antiarnaque.app.R;
import com.antiarnaque.app.data.HistoryDao;
import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.HistoryEntry;
import com.antiarnaque.app.report.ReportManager;
import com.antiarnaque.app.util.AppExecutors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Historique local des analyses (SQLite). */
public final class HistoryFragment extends Fragment {

    private final List<HistoryEntry> entries = new ArrayList<>();
    private final List<HistoryEntry> allEntries = new ArrayList<>();
    private EditText searchInput;
    private HistoryAdapter adapter;
    private TextView emptyView;
    private HistoryDao historyDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_history);
        emptyView = view.findViewById(R.id.text_empty_history);
        searchInput = view.findViewById(R.id.edit_search_history);
        Button clearButton = view.findViewById(R.id.button_clear_history);
        historyDao = new HistoryDao(requireContext());

        adapter = new HistoryAdapter(entries, new HistoryAdapter.Listener() {
            @Override
            public void onOpen(HistoryEntry entry) {
                showDetail(entry);
            }

            @Override
            public void onDelete(HistoryEntry entry) {
                AppExecutors.run(() -> {
                    historyDao.deleteById(entry.getId());
                    AppExecutors.onMain(HistoryFragment.this::load);
                });
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));

        clearButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_history_title)
                .setMessage(R.string.clear_history_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> AppExecutors.run(() -> {
                    historyDao.clear();
                    AppExecutors.onMain(HistoryFragment.this::load);
                }))
                .show());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        AppExecutors.run(() -> {
            List<HistoryEntry> fresh = historyDao.getAll();
            AppExecutors.onMain(() -> {
                allEntries.clear();
                allEntries.addAll(fresh);
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.FRANCE);
        entries.clear();
        for (HistoryEntry entry : allEntries) {
            if (query.isEmpty()
                    || entry.getMessage().toLowerCase(Locale.FRANCE).contains(query)
                    || entry.getLevel().getLabel().toLowerCase(Locale.FRANCE).contains(query)) {
                entries.add(entry);
            }
        }
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDetail(HistoryEntry entry) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
        String title = getString(R.string.score_format, entry.getScorePercent())
                + " · " + entry.getLevel().getLabel();
        String detail = getString(R.string.detail_date, format.format(new Date(entry.getTimestamp())))
                + "\n\n" + entry.getReasons()
                + "\n\n" + getString(R.string.engine_format, entry.getEngine())
                + (entry.isReported() ? "\n\n" + getString(R.string.report_done) : "");

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(detail)
                .setNeutralButton(R.string.close, null)
                .setNegativeButton(R.string.delete, (dialog, which) -> AppExecutors.run(() -> {
                    historyDao.deleteById(entry.getId());
                    AppExecutors.onMain(HistoryFragment.this::load);
                }));

        if (!entry.isReported()) {
            builder.setPositiveButton(R.string.report_button, (dialog, which) -> {
                AnalysisResult result = new AnalysisResult(entry.getMessage(), entry.getScore(),
                        entry.getLevel() == com.antiarnaque.app.model.RiskLevel.ELEVE
                                ? "spam" : "legit", entry.getLevel(), null, null,
                        entry.getTimestamp(), entry.getEngine());
                AppExecutors.run(() -> {
                    ReportManager.ReportResult outcome = new ReportManager(requireContext())
                            .submit(entry.getMessage(), result);
                    AppExecutors.onMain(() -> {
                        Toast.makeText(requireContext(),
                                outcome.sent ? R.string.report_sent : R.string.report_saved,
                                Toast.LENGTH_SHORT).show();
                        load();
                    });
                });
            });
        }
        builder.show();
    }
}
