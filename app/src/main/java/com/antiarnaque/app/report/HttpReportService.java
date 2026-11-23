package com.antiarnaque.app.report;

import com.antiarnaque.app.model.ReportEntry;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Envoie un signalement anonymisé :
 *
 * POST {baseUrl}/api/reports
 * {"message_hash":"...","score":0.93,"reason":"...","timestamp":1234567890}
 */
public final class HttpReportService implements ReportService {

    private final String baseUrl;

    public HttpReportService(String baseUrl) {
        String url = baseUrl == null ? "" : baseUrl.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        this.baseUrl = url;
    }

    @Override
    public boolean send(ReportEntry report) throws Exception {
        if (baseUrl.isEmpty()) {
            throw new IllegalStateException("URL de signalement non configurée.");
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + "/api/reports");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("message_hash", report.getMessageHash());
            payload.put("score", report.getScore());
            payload.put("reason", report.getReason());
            payload.put("timestamp", report.getTimestamp());

            byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream os = connection.getOutputStream();
            os.write(body);
            os.flush();
            os.close();

            int code = connection.getResponseCode();
            return code >= 200 && code < 300;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
