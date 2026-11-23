package com.antiarnaque.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Résultat d'une analyse, quelle que soit la source (heuristique locale,
 * modèle TFLite ou API Flask). Contrat d'échange entre le front et le ML.
 */
public final class AnalysisResult {

    private final String message;
    private float score;          // 0..1
    private String label;         // "spam" ou "legit"
    private RiskLevel level;
    private final List<String> reasons;
    private final List<String> signals;
    private final long timestamp;
    private String engine;

    public AnalysisResult(String message, float score, String label, RiskLevel level,
                          List<String> reasons, List<String> signals, long timestamp, String engine) {
        this.message = message;
        this.score = Math.max(0f, Math.min(1f, score));
        this.label = label;
        this.level = level != null ? level : RiskLevel.fromScore(this.score);
        this.reasons = reasons != null ? new ArrayList<>(reasons) : new ArrayList<String>();
        this.signals = signals != null ? new ArrayList<>(signals) : new ArrayList<String>();
        this.timestamp = timestamp;
        this.engine = engine != null ? engine : "inconnu";
    }

    public String getMessage() {
        return message;
    }

    public float getScore() {
        return score;
    }

    public int getScorePercent() {
        return Math.round(score * 100f);
    }

    public String getLabel() {
        return label;
    }

    public RiskLevel getLevel() {
        return level;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public List<String> getSignals() {
        return signals;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        if (engine != null && !engine.isEmpty()) {
            this.engine = engine;
        }
    }

    /** Texte lisible des explications, stocké dans l'historique SQLite. */
    public String getReasonsText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reasons.size(); i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append("• ").append(reasons.get(i));
        }
        if (sb.length() == 0) {
            sb.append("Aucun signe suspect détecté.");
        }
        return sb.toString();
    }

    public String getSignalsText() {
        if (signals.isEmpty()) {
            return "Aucun mot-clé suspect relevé.";
        }
        StringBuilder sb = new StringBuilder();
        for (String signal : signals) {
            if (sb.length() > 0) {
                sb.append("  ·  ");
            }
            sb.append(signal);
        }
        return sb.toString();
    }

    public boolean isSpam() {
        return "spam".equalsIgnoreCase(label) || level == RiskLevel.ELEVE;
    }
}
