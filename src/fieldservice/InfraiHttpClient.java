package fieldservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public final class InfraiHttpClient {
    private final FieldServiceConfig config;
    private final HttpClient http = HttpClient.newHttpClient();

    public InfraiHttpClient(FieldServiceConfig config) { this.config = config; }

    public String post(String path, String body, String requestKey) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                    .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json").header("Idempotency-Key", requestKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (response.statusCode() == 429 && attempt < 3) {
                Thread.sleep((long) Math.pow(2, attempt) * 250L);
                continue;
            }
            if (!envelope.contains("\"ok\":true")) throw new IllegalStateException("Infrai request rejected: " + envelope);
            if (response.statusCode() >= 500) throw new IOException("Infrai transport status " + response.statusCode());
            return envelope;
        }
        throw new IOException("request retry budget exhausted");
    }

    public String fill(String pdfBase64, String fieldsJson, String requestKey) throws IOException, InterruptedException {
        return post("/v1/pdf/form/fill", "{\"pdf\":\"" + pdfBase64 + "\",\"fields\":" + fieldsJson + ",\"flatten\":true}", requestKey);
    }

    public void archive(String key, String dataBase64, String requestKey) throws IOException, InterruptedException {
        String body = "{\"data_base64\":\"" + dataBase64 + "\"}";
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.baseUrl() + "/v1/storage/object/put/" + config.bucket() + "/" + key))
                    .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json").header("Idempotency-Key", requestKey)
                    .PUT(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (response.statusCode() == 429 && attempt < 3) {
                Thread.sleep((long) Math.pow(2, attempt) * 250L);
                continue;
            }
            if (!envelope.contains("\"ok\":true")) throw new IllegalStateException("Infrai request rejected: " + envelope);
            if (response.statusCode() >= 500) throw new IOException("Infrai transport status " + response.statusCode());
            return;
        }
        throw new IOException("archive retry budget exhausted");
    }

    public static String dataBase64(String envelope) {
        String marker = "\"data_base64\":\"";
        int start = envelope.indexOf(marker);
        if (start < 0) throw new IllegalStateException("flattened PDF missing from response");
        start += marker.length();
        int end = envelope.indexOf('"', start);
        return envelope.substring(start, end);
    }
}
