package com.judgeapp.ai;

import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.google.gson.*;

public class GeminiAPI {
    // Ollama config (chạy local, không cần API key)
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "mistral"; // Hoặc "llama2", "phi" (phải download trước)
    
    // Caching for repeated prompts (Simple in-memory cache)
    private static final Map<String, String> responseCache = new HashMap<>();
    
    // Rate limiting: track last request time
    private static long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 500; // 500ms cho local LLM

    private static String call(String jsonBody) throws Exception {
        // Implement rate limiting - wait if needed
        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            Thread.sleep(MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest);
        }
        
        try {
            // Ollama request
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
                    throw new Exception("Model '" + MODEL + "' chưa được download. Chạy: ollama pull " + MODEL);
                }
                throw new Exception("Ollama lỗi (HTTP " + res.statusCode() + "): " + errorMsg);
            }
            
            // Parse Ollama response
            JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
            String response = json.get("response").getAsString();
            
            if (response == null || response.trim().isEmpty()) {
                throw new Exception("Ollama không trả về response");
            }
            
            return response.trim();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Request bị interrupt: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                throw new Exception("Ollama chưa chạy! Chạy: ollama serve", e);
            }
            throw e;
        }
    }
    
    // Extract prompt from Gemini JSON format
    private static String extractPromptFromJson(String jsonBody) throws Exception {
        try {
            JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();
            return json.getAsJsonArray("contents")
                .get(0).getAsJsonObject()
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
        } catch (Exception e) {
            return jsonBody; // Fallback
        }
    }

    public static String ask(String prompt) throws Exception {
        // Check cache first
        if (responseCache.containsKey(prompt)) {
            System.out.println("[Cache Hit] Using cached response for prompt");
            return responseCache.get(prompt);
        }
        
        String body = "{\"contents\":[{\"parts\":[{\"text\":" + new Gson().toJson(prompt) + "}]}]}";
        String result = call(body);
        
        // Cache the result
        responseCache.put(prompt, result);
        return result;
    }

    // Đọc ảnh đề bài bằng Ollama (Vision model nếu có)
    public static String readImageProblem(String imagePath) throws Exception {
        // Ollama vision models: llava (requires special setup)
        // Tạm thời: chỉ hỗ trợ text
        throw new Exception("Ollama local không hỗ trợ vision API. " +
            "Để dùng OCR ảnh, hãy:\n" +
            "  1. Sử dụng Tesseract OCR trực tiếp\n" +
            "  2. Hoặc nâng cấp lên paid Gemini plan\n" +
            "  3. Hoặc dùng Groq API (miễn phí, có vision)");
    }

    // AI sinh code Generator (sinh testcase ngẫu nhiên)
    public static String generateGeneratorCode(String problemContent) throws Exception {
        String prompt = """
            Cho đề bài lập trình thi đấu sau:
            %s
            
            Hãy viết một chương trình Java (class Main) chuyên để sinh testcase ngẫu nhiên cho bài này.
            Yêu cầu:
            - Dùng Random để sinh input ngẫu nhiên hợp lệ theo đúng ràng buộc của đề
            - In ra input theo đúng format đề yêu cầu
            - Code phải compile và chạy được
            - Chỉ trả về code Java thuần, không giải thích, không markdown
            """.formatted(problemContent);
        String raw = ask(prompt);
        return raw.replaceAll("```java", "").replaceAll("```", "").trim();
    }

    // AI sinh code Checker
    public static String generateCheckerCode(String problemContent) throws Exception {
        String prompt = """
            Cho đề bài lập trình thi đấu sau:
            %s
            
            Hãy viết một chương trình Java (class Main) làm Checker để kiểm tra output của thí sinh.
            Chương trình nhận vào từ stdin theo format:
            - Dòng 1: input của testcase
            - Dòng 2: output đúng (expected)
            - Dòng 3: output của thí sinh (actual)
            In ra "AC" nếu đúng, "WA" nếu sai.
            Chỉ trả về code Java thuần, không giải thích, không markdown.
            """.formatted(problemContent);
        String raw = ask(prompt);
        return raw.replaceAll("```java", "").replaceAll("```", "").trim();
    }

    // AI sinh code mẫu AC
    public static String generateSampleCode(String problemContent, String language) throws Exception {
        String prompt = """
            Viết code %s giải bài lập trình sau. Code phải AC (đúng và đủ nhanh).
            Chỉ trả về code thuần, không giải thích, không markdown:
            %s
            """.formatted(language, problemContent);
        String raw = ask(prompt);
        return raw.replaceAll("```" + language.toLowerCase(), "")
                  .replaceAll("```cpp", "").replaceAll("```java", "")
                  .replaceAll("```python", "").replaceAll("```", "").trim();
    }
}