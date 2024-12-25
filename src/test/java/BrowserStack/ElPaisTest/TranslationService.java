package BrowserStack.ElPaisTest;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class TranslationService {
    private static final String RAPID_API_KEY = "95b0846351mshed38779a23f0b4dp1e6a11jsnbef326f4441d";
    private static final String RAPID_API_HOST = "rapid-translate-multi-traduction.p.rapidapi.com";
    private static final String ENDPOINT_URL = "https://rapid-translate-multi-traduction.p.rapidapi.com/t";
    public static String translateEStoEN(String textInSpanish) {
        return translate("es", "en", textInSpanish);
    }

    public static String translate(String fromLang, String toLang, String text) {
        String translatedText = "";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(ENDPOINT_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("x-rapidapi-host", RAPID_API_HOST);
            post.setHeader("x-rapidapi-key",  RAPID_API_KEY);
            JSONObject requestBody = new JSONObject();
            requestBody.put("from", fromLang);
            requestBody.put("to",   toLang);
            requestBody.put("q",    text);
            StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int statusCode = response.getCode();
                String respBody = EntityUtils.toString(response.getEntity());
                System.out.println("Status code: " + statusCode);
                System.out.println("Response body:\n" + respBody);
                if (statusCode == 200) {
                    try {
                        JSONArray arr = new JSONArray(respBody);
                        translatedText = arr.optString(0, "");
                    } catch (JSONException je) {
                        System.err.println("Failed to parse JSON array: " + je.getMessage());
                        System.err.println("Body was: " + respBody);
                    }
                } else {
                    System.err.println("Rapid Translate API returned HTTP " + statusCode);
                    System.err.println("Response body: " + respBody);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return translatedText;
    }
}
