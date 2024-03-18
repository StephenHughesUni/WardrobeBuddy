package com.example.wardrobebuddy.com.firebaseauth;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAiHelper {
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions"; // Use the chat completions endpoint
    private static final String API_KEY = "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"; // Replace with your actual OpenAI API Key
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    public String getAIResponse(String prompt, String base64Image) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "gpt-4-vision-preview"); // Adjust the model as needed

            JSONArray contentArray = new JSONArray();
            contentArray.put(new JSONObject().put("type", "text").put("text", prompt));
            contentArray.put(new JSONObject().put("type", "image_url").put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + base64Image)));

            jsonObject.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", contentArray)));
            jsonObject.put("max_tokens", 300);

            Log.d("OpenAiHelper", "Payload: " + jsonObject.toString());

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Log.d("OpenAiHelper", "Sending request to OpenAI...");

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                assert response.body() != null;
                String responseBody = response.body().string();
                Log.d("OpenAiHelper", "Response from OpenAI: " + responseBody);

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    Log.d("OpenAiHelper", "Extracted Content: " + content);

                    // Extract Size, Price, and Article Number
                    String[] lines = content.split("\n");
                    StringBuilder extractedInfo = new StringBuilder();
                    for (String line : lines) {
                        // Now also checking for "Brand:" in addition to the other fields
                        if (line.contains("Brand:") || line.contains("Size:") || line.contains("Price:") || line.contains("Article Number:")) {
                            extractedInfo.append(line).append("\n");
                        }
                    }

                    String finalExtractedInfo = extractedInfo.toString().trim();
                    Log.d("OpenAiHelper", "Final Extracted Info: " + finalExtractedInfo);
                    return finalExtractedInfo;
                } else {
                    Log.e("OpenAiHelper", "No choices in response.");
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e("OpenAiHelper", "Error in sending or processing response: " + e.getMessage());
            return null;
        }
    }
}
