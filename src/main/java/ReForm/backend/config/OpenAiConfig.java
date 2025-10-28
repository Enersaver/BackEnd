package ReForm.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;

    /**
     * OpenAI Vision API 직접 호출 메서드
     * @param prompt 텍스트 프롬프트
     * @param imageUrl 이미지 URL
     * @return AI 응답 텍스트
     */
    public String callVisionAPI(String prompt, String imageUrl) {
        try {
            String apiUrl = "https://api.openai.com/v1/chat/completions";

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);

            // Body - Vision API 형식
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("temperature", 0.2);

            List<Map<String, Object>> messages = List.of(
                    Map.of(
                            "role", "user",
                            "content", List.of(
                                    Map.of("type", "text", "text", prompt),
                                    Map.of(
                                            "type", "image_url",
                                            "image_url", Map.of("url", imageUrl)
                                    )
                            )
                    )
            );
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            // 응답에서 텍스트 추출
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
            return "No response from OpenAI";
        } catch (Exception e) {
            return "Error calling Vision API: " + e.getMessage();
        }
    }
}