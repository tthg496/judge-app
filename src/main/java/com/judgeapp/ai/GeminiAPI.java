package com.judgeapp.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeminiAPI {
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "mistral";
    private static final Map<String, String> responseCache = new HashMap<>();
    private static long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 500;

    private static String call(String jsonBody) throws Exception {
        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            Thread.sleep(MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest);
        }

        try {
            String ollamaRequest = new Gson().toJson(Map.of(
                "model", MODEL,
                "prompt", extractPromptFromJson(jsonBody),
                "stream", false
            ));

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(ollamaRequest))
                .build();

            HttpResponse<String> res = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

            lastRequestTime = System.currentTimeMillis();

            if (res.statusCode() != 200) {
                String errorMsg = res.body();
                if (res.statusCode() == 500 && errorMsg.contains("unknown model")) {
                    throw new Exception("Model '" + MODEL + "' chua duoc download. Chay: ollama pull " + MODEL);
                }
                throw new Exception("Ollama loi (HTTP " + res.statusCode() + "): " + errorMsg);
            }

            JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
            String response = json.get("response").getAsString();
            if (response == null || response.trim().isEmpty()) {
                throw new Exception("Ollama khong tra ve response");
            }
            return response.trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Request bi interrupt: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                throw new Exception("Ollama chua chay! Chay: ollama serve", e);
            }
            throw e;
        }
    }

    private static String extractPromptFromJson(String jsonBody) {
        try {
            JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();
            return json.getAsJsonArray("contents")
                .get(0).getAsJsonObject()
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
        } catch (Exception e) {
            return jsonBody;
        }
    }

    public static String ask(String prompt) throws Exception {
        if (responseCache.containsKey(prompt)) {
            System.out.println("[Cache Hit] Using cached response for prompt");
            return responseCache.get(prompt);
        }

        String body = "{\"contents\":[{\"parts\":[{\"text\":" + new Gson().toJson(prompt) + "}]}]}";
        String result = call(body);
        responseCache.put(prompt, result);
        return result;
    }

    public static String readImageProblem(String imagePath) throws Exception {
        throw new Exception("Ollama local khong ho tro vision API. Hay dung OCRManager/Tesseract de doc anh.");
    }

    public static String generateGeneratorCode(String problemContent) throws Exception {
        String prompt = """
            You are writing a Java testcase generator for this programming problem:
            %s

            Requirements:
            - Return only plain Java code, no markdown, no explanation.
            - The program must be public class Main.
            - Each run prints exactly one valid input testcase.
            - Use Random and respect the input format and constraints.
            - Prefer edge-heavy random cases, not only tiny samples.
            """.formatted(problemContent);
        return stripCodeFence(ask(prompt), "java");
    }

    public static String generateCheckerCode(String problemContent) throws Exception {
        String prompt = """
            You are writing a Java special judge checker for this programming problem:
            %s

            Requirements:
            - Return only plain Java code, no markdown, no explanation.
            - The program must be public class Main.
            - It reads stdin with these exact section markers:
              __INPUT__
              <full original testcase input, possibly multiple lines>
              __EXPECTED__
              <expected output, possibly multiple lines>
              __ACTUAL__
              <contestant actual output, possibly multiple lines>
              __END__
            - Parse by markers, not by fixed line count.
            - Print AC if actual is accepted, otherwise print WA.
            - Use the original input to validate alternate correct outputs when needed.
            """.formatted(problemContent);
        return stripCodeFence(ask(prompt), "java");
    }

    public static String generateSampleCode(String problemContent, String language) throws Exception {
        String prompt = """
            Write a correct and efficient %s solution for this programming problem:
            %s

            Requirements:
            - Return only plain code, no markdown, no explanation.
            - If the language is Java, the program must be public class Main.
            """.formatted(language, problemContent);
        return stripCodeFence(ask(prompt), language);
    }

    private static String stripCodeFence(String raw, String language) {
        if (raw == null) return "";
        String code = raw.trim();
        String lang = language == null ? "" : language.toLowerCase();
        return code.replaceAll("```" + lang, "")
            .replaceAll("```cpp", "")
            .replaceAll("```c\\+\\+", "")
            .replaceAll("```java", "")
            .replaceAll("```python", "")
            .replaceAll("```", "")
            .trim();
    }
}
