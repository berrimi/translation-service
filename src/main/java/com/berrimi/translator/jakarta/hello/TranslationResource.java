package com.berrimi.translator.jakarta.hello;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

class LLMClient {

  private static final String API_KEY = loadKey();

  private static String loadKey() {
    try (InputStream input = LLMClient.class.getClassLoader()
        .getResourceAsStream("translator.properties")) {

      if (input == null) {
        System.err.println("translator.properties not found in resources!");
        return null;
      }

      Properties props = new Properties();
      props.load(input);
      return props.getProperty("API_KEY");
    } catch (IOException e) {
      return null;
    }
  }

  public static String translate(String text, String to) {

    try {

      HttpClient client = HttpClient.newHttpClient();

      String prompt = "Detect the language of the following text and translate it to " + to +
          ". Return only the translation, without explanation or additional text:\n" + text;

      String json = """
          {
            "contents": [
              {
                "parts": [
                  { "text": "%s" }
                ]
              }
            ]
          }
          """.formatted(prompt.replace("\"", "\\\""));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(
              "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                  + API_KEY))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(json))
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      return parseGeminiResponse(response.body());

    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  private static String parseGeminiResponse(String responseJson) {
    try {
      // Very minimal extraction to keep it simple.
      int index = responseJson.indexOf("\"text\":");
      if (index == -1) {
        return "Invalid response: " + responseJson;
      }

      int start = responseJson.indexOf("\"", index + 7) + 1;
      int end = responseJson.indexOf("\"", start);
      return responseJson.substring(start, end);

    } catch (Exception e) {
      return "Parsing error";
    }
  }
}

@Path("translate")
public class TranslationResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response translate(@QueryParam("text") String text, @QueryParam("to") @DefaultValue("darija") String toLang) {

    if (text == null || text.isBlank()) {
      return Response.status(400).entity("{\"error\": \"Text cannot be empty\"}").build();
    }

    String result = LLMClient.translate(text, toLang);

    return Response.ok("{\"translation\": \"" + result + "\"}").build();

  }

}
