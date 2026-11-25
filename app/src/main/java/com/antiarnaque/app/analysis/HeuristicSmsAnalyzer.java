package com.antiarnaque.app.analysis;

import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.RiskLevel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Classifieur heuristique local, utilisé :
 *  - par défaut (aucune configuration réseau requise),
 *  - en repli lorsque le modèle Flask/TFLite est indisponible.
 *
 * Il ne remplace pas le modèle de machine learning : il garantit que
 * l'application reste utilisable et explicable même hors ligne.
 */
public final class HeuristicSmsAnalyzer implements SmsAnalyzer {

    private static final float BASE_SCORE = 0.03f;
    private static final float MAX_SCORE = 0.99f;

    private static final class Rule {
        final float weight;
        final String reason;
        final String[] keywords;
        final Pattern regex;

        Rule(float weight, String reason, String[] keywords) {
            this.weight = weight;
            this.reason = reason;
            this.keywords = keywords;
            this.regex = null;
        }

        Rule(float weight, String reason, Pattern regex) {
            this.weight = weight;
            this.reason = reason;
            this.keywords = null;
            this.regex = regex;
        }
    }

    private static final List<Rule> RULES = Arrays.asList(
            new Rule(0.32f, "Promesse de gain ou de récompense inhabituelle",
                    new String[]{"gagn", "gagnant", "loterie", "jackpot", "vous avez gagn",
                            "congrat", "winner", "recompense", "cadeau", "gratuit", "prix"}),
            new Rule(0.24f, "Demande de code confidentiel / OTP",
                    new String[]{"envoyez le code", "envoie le code", "partagez le code",
                            "donnez le code", "communiquez le code", "code recu",
                            "code de validation recu", "code otp", "mot de passe",
                            "code confidentiel", "numero pin"}),
            new Rule(0.22f, "Demande de paiement ou de transfert d'argent",
                    new String[]{"mobile money", "orange money", "mtn money", "airtel money",
                            "wave", "paiement", "payez", "paye ", "transferez", "transfert",
                            "envoyez de l'argent", "frais de", "commission"}),
            new Rule(0.18f, "Urgence artificielle pour forcer la décision",
                    new String[]{"urgent", "immediat", "maintenant", "derniere chance",
                            "expire", "24h", "aujourd'hui", "vite", "sans attendre"}),
            new Rule(0.18f, "Usurpation d'identité (agent, banque, service client)",
                    new String[]{"agent", "service client", "banque", "police", "admin",
                            "responsable", "operateur", "direction"}),
            new Rule(0.20f, "Menace de blocage ou de sanction",
                    new String[]{"compte bloque", "suspendu", "desactive", "desactivation",
                            "arrestation", "amende", "arret", "perte de votre compte"}),
            new Rule(0.14f, "Demande d'informations personnelles",
                    new String[]{"nom complet", "date de naissance", "numero de carte",
                            "informations bancaires", "vos donnees", "identite"}),
            new Rule(0.20f, "Lien raccourci ou lien externe suspect",
                    Pattern.compile("https?://|bit\\.ly|tinyurl|t\\.me|wa\\.me|cutt\\.ly", Pattern.CASE_INSENSITIVE)),
            new Rule(0.10f, "Numéro à composition rapide ou longue suite de chiffres",
                    Pattern.compile("\\b[0-9]{6,}\\b"))
    );

    @Override
    public String name() {
        return "heuristique";
    }

    @Override
    public AnalysisResult analyze(String message) throws AnalysisException {
        if (message == null || message.trim().isEmpty()) {
            throw new AnalysisException("Le message est vide.");
        }

        String normalized = normalize(message);
        float score = BASE_SCORE;
        List<String> reasons = new ArrayList<>();
        List<String> signals = new ArrayList<>();
        Set<String> uniqueSignals = new LinkedHashSet<>();

        for (Rule rule : RULES) {
            boolean matched = false;
            if (rule.keywords != null) {
                for (String keyword : rule.keywords) {
                    if (containsWord(normalized, keyword)) {
                        matched = true;
                        uniqueSignals.add(keyword);
                    }
                }
            } else if (rule.regex != null && rule.regex.matcher(normalized).find()) {
                matched = true;
                uniqueSignals.add(rule.reason);
            }
            if (matched) {
                score += rule.weight;
                reasons.add(rule.reason);
            }
        }

        score = Math.min(MAX_SCORE, score);
        RiskLevel level = RiskLevel.fromScore(score);
        signals.addAll(uniqueSignals);
        if (signals.size() > 8) {
            signals = new ArrayList<>(signals.subList(0, 8));
        }

        String label = level == RiskLevel.ELEVE ? "spam" : "legit";
        return new AnalysisResult(message.trim(), score, label, level, reasons, signals,
                System.currentTimeMillis(), "heuristique locale");
    }

    /** Minuscules, sans accents, espaces normalisés. */
    private static String normalize(String value) {
        String decomposed = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * Correspondance de mots : évite par exemple que "prix" déclenche sur "exprime".
     * Les fragments comme "gagn" ou "envoyez le code" sont volontairement tolérants.
     */
    private static boolean containsWord(String normalized, String keyword) {
        String word = normalize(keyword);
        if (normalized.contains(word)) {
            if (word.length() >= 6 || word.contains(" ")) {
                return true;
            }
            return Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(word) + "([^a-z0-9]|$)")
                    .matcher(normalized).find();
        }
        return false;
    }
}
