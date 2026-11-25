package com.antiarnaque.app.analysis;

import com.antiarnaque.app.model.AnalysisResult;
import com.antiarnaque.app.model.RiskLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client de l'API Flask (ou de toute API compatible) :
 *
 * POST {baseUrl}/api/analyze
 * Content-Type: application/json
 * {"message": "..."}
 *
 * Réponse attendue :
 * {"score": 0.93, "label": "spam", "reasons": ["..."], "signals": ["..."], "engine": "tflite"}
 */
public final class RemoteSmsAnalyzer implements SmsAnalyzer {

    private static final int CONNECT_TIMEOUT_MS = 6000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final String baseUrl;

    public RemoteSmsAnalyzer(String baseUrl) {
        String url = baseUrl == null ? "" : baseUrl.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.baseUrl = url;
    }

    @Override
    public String name() {
        return "api-distante";
    }

    @Override
    public AnalysisResult analyze(String message) throws AnalysisException {
        if (baseUrl.isEmpty()) {
            throw new AnalysisException("URL du serveur non configurée.");
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + "/api/analyze");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("message", message);
            byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream os = connection.getOutputStream();
            os.write(body);
            os.flush();
            os.close();

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new AnalysisException("Réponse du serveur : HTTP " + code);
            }

            JSONObject json = readJson(connection.getInputStream());
            float score = parseScore(json);
            String label = json.optString("label", "");
            if (label.isEmpty()) {
                label = score >= 0.5f ? "spam" : "legit";
            }
            RiskLevel level;
            String risk = json.optString("risk_level", "").toUpperCase(Locale.ROOT);
            if ("FAIBLE".equals(risk) || "LOW".equals(risk)
                    || "MOYEN".equals(risk) || "MEDIUM".equals(risk)
                    || "ELEVE".equals(risk) || "HIGH".equals(risk)) {
                level = RiskLevel.fromName(risk);
            } else {
                level = RiskLevel.fromScore(score);
            }

            List<String> reasons = parseArray(json.optJSONArray("reasons"));
            List<String> signals = parseArray(json.optJSONArray("signals"));
            String engine = json.optString("engine", "modèle distant");

            return new AnalysisResult(message.trim(), score, label, level, reasons, signals,
                    System.currentTimeMillis(), engine);
        } catch (JSONException e) {
            throw new AnalysisException("Réponse JSON invalide du serveur.", e);
        } catch (Exception e) {
            if (e instanceof AnalysisException) {
                throw (AnalysisException) e;
            }
            throw new AnalysisException("Impossible de contacter le serveur : " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static float parseScore(JSONObject json) throws JSONException {
        if (!json.has("score")) {
            throw new JSONException("Champ 'score' absent");
        }
        float score = (float) json.getDouble("score");
        // Certains modèles renvoient un pourcentage (0..100) plutôt qu'une probabilité.
        if (score > 1f) {
            score = score / 100f;
        }
        return Math.max(0f, Math.min(1f, score));
    }

    private static List<String> parseArray(JSONArray array) {
        List<String> result = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, null);
                if (value != null && !value.isEmpty()) {
                    result.add(value);
                }
            }
        }
        if (result.isEmpty()) {
            result.add("Le modèle n'a pas fourni d'explication détaillée.");
        }
        return result;
    }

    private static JSONObject readJson(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return new JSONObject(sb.toString());
    }
}
