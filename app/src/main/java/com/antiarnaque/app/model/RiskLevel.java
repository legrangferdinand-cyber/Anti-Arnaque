package com.antiarnaque.app.model;

/**
 * Niveau de risque affiché à l'utilisateur.
 * Les seuils sont définis une seule fois ici pour garantir la cohérence
 * entre l'analyse heuristique, le modèle distant et l'affichage.
 */
public enum RiskLevel {
    FAIBLE("Faible", 0),
    MOYEN("Moyen", 1),
    ELEVE("Élevé", 2);

    private final String label;
    private final int priority;

    RiskLevel(String label, int priority) {
        this.label = label;
        this.priority = priority;
    }

    public String getLabel() {
        return label;
    }

    public int getPriority() {
        return priority;
    }

    /** score attendu entre 0 et 1. */
    public static RiskLevel fromScore(float score) {
        if (score >= 0.75f) {
            return ELEVE;
        }
        if (score >= 0.40f) {
            return MOYEN;
        }
        return FAIBLE;
    }

    public static RiskLevel fromName(String name) {
        if (name == null) {
            return FAIBLE;
        }
        switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "ELEVE":
            case "HIGH":
            case "SPAM":
                return ELEVE;
            case "MOYEN":
            case "MEDIUM":
                return MOYEN;
            default:
                return FAIBLE;
        }
    }
}
