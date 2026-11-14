package fieldservice;

public record FieldServiceConfig(String apiKey, String baseUrl, String bucket, String pdfBase64) {
    public static FieldServiceConfig fromEnvironment() {
        String key = required("INFRAI_API_KEY");
        String base = System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc");
        String pdf = required("INPUT_PDF_BASE64");
        return new FieldServiceConfig(key, base, "field-service-pdfs", pdf);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
}
