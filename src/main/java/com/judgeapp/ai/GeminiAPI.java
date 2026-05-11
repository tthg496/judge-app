package com.judgeapp.ai;

import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.google.gson.*;

public class GeminiAPI {
    private static final String API_KEY = "AIzaSyB09pHheEENBogmOnSAji3A0PYtsRgrZ8U";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    private static String call(String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        if (json.has("error")) throw new Exception(json.get("error").getAsJsonObject().get("message").getAsString());
        return json.getAsJsonArray("candidates").get(0).getAsJsonObject()
            .getAsJsonObject("content").getAsJsonArray("parts")
            .get(0).getAsJsonObject().get("text").getAsString();
    }

    public static String ask(String prompt) throws Exception {
        String body = "{\"contents\":[{\"parts\":[{\"text\":" + new Gson().toJson(prompt) + "}]}]}";
        return call(body);
    }

    // Đọc ảnh đề bài bằng Gemini Vision
    public static String readImageProblem(String imagePath) throws Exception {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String ext = imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();
        String mimeType = ext.equals("png") ? "image/png" : "image/jpeg";

        String body = new Gson().toJson(Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(
                    Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64)),
                    Map.of("text", "Đây là ảnh đề bài lập trình thi đấu. Hãy đọc và trích xuất toàn bộ nội dung đề bài bằng tiếng Việt, bao gồm: tên bài, mô tả, input format, output format, ràng buộc (constraints), và các ví dụ.")
                )
            ))
        ));
        return call(body);
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