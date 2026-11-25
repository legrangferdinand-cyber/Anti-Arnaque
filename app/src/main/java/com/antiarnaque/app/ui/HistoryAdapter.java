package com.antiarnaque.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.antiarnaque.app.R;
import com.antiarnaque.app.model.HistoryEntry;
import com.antiarnaque.app.model.RiskLevel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Liste des analyses enregistrées. */
public final class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface Listener {
        void onOpen(HistoryEntry entry);

        void onDelete(HistoryEntry entry);
    }

    private final List<HistoryEntry> entries;
    private final Listener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public HistoryAdapter(List<HistoryEntry> entries, Listener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntry entry = entries.get(position);
        String snippet = entry.getMessage().replace("\n", " ");
        if (snippet.length() > 110) {
            snippet = snippet.substring(0, 110) + "…";
        }
        holder.snippet.setText(snippet);
        holder.date.setText(dateFormat.format(new Date(entry.getTimestamp())));
        holder.score.setText(holder.itemView.getContext()
                .getString(R.string.score_format, entry.getScorePercent()));
        holder.level.setText(entry.getLevel().getLabel());

        int color;
        if (entry.getLevel() == RiskLevel.ELEVE) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_high);
        } else if (entry.getLevel() == RiskLevel.MOYEN) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_medium);
        } else {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.risk_low);
        }
        holder.score.setTextColor(color);
        holder.level.setTextColor(color);

        holder.itemView.setOnClickListener(v -> listener.onOpen(entry));
        holder.delete.setOnClickListener(v -> listener.onDelete(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView snippet;
        final TextView date;
        final TextView score;
        final TextView level;
        final ImageButton delete;

        ViewHolder(View view) {
            super(view);
            snippet = view.findViewById(R.id.text_snippet);
            date = view.findViewById(R.id.text_date);
            score = view.findViewById(R.id.text_item_score);
            level = view.findViewById(R.id.text_item_level);
            delete = view.findViewById(R.id.button_delete);
        }
    }
}
