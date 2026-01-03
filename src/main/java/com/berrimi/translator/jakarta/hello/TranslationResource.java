package com.berrimi.translator.jakarta.hello;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("translate")
public class TranslationResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response translate(@QueryParam("text") String text,
      @QueryParam("to") @DefaultValue("darija") String toLang,
      @QueryParam("username") String username) { // Added username parameter

    if (text == null || text.isBlank()) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", "Text cannot be empty")
          .build();
      return Response.status(400).entity(error.toString()).build();
    }

    // Get translation from LLM
    String result = LLMClient.translate(text, toLang);

    // If username is provided, save to history
    if (username != null && !username.isBlank()) {
      String historyId = UUID.randomUUID().toString();
      TranslationHistory history = new TranslationHistory(
          historyId,
          username,
          text,
          result,
          toLang);
      HistoryRepository.addTranslation(username, history);

      // Return response with history ID
      JsonObject json = Json.createObjectBuilder()
          .add("translation", result == null ? "" : result)
          .add("historyId", historyId)
          .build();

      return Response.ok(json.toString()).build();
    } else {
      // Return response without history ID for unauthenticated users
      JsonObject json = Json.createObjectBuilder()
          .add("translation", result == null ? "" : result)
          .build();

      return Response.ok(json.toString()).build();
    }
  }

  @GET
  @Path("history")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getHistory(@QueryParam("username") String username) {
    if (username == null || username.isBlank()) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", "Username is required")
          .build();
      return Response.status(400).entity(error.toString()).build();
    }

    List<TranslationHistory> history = HistoryRepository.getHistory(username);

    // Convert history list to JSON array
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (TranslationHistory item : history) {
      JsonObjectBuilder itemBuilder = Json.createObjectBuilder()
          .add("id", item.getId())
          .add("originalText", item.getOriginalText())
          .add("translatedText", item.getTranslatedText())
          .add("targetLang", item.getTargetLang())
          .add("timestamp", item.getTimestamp().toString());

      arrayBuilder.add(itemBuilder);
    }

    JsonObject response = Json.createObjectBuilder()
        .add("history", arrayBuilder)
        .add("count", history.size())
        .build();

    return Response.ok(response.toString()).build();
  }

  @GET
  @Path("clear-history")
  @Produces(MediaType.APPLICATION_JSON)
  public Response clearHistory(@QueryParam("username") String username) {
    if (username == null || username.isBlank()) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", "Username is required")
          .build();
      return Response.status(400).entity(error.toString()).build();
    }

    HistoryRepository.clearHistory(username);

    JsonObject response = Json.createObjectBuilder()
        .add("message", "History cleared successfully")
        .build();

    return Response.ok(response.toString()).build();
  }
}

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
          ". Return only the translation, without extra explanation:\n" + text;

      String body = """
          {
            "model": "openai/gpt-oss-20b:free",
            "messages": [
              { "role": "user", "content": "%s" }
            ]
          }
          """.formatted(escapeJson(prompt));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + API_KEY)
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      return parseOpenRouterResponse(response.body());

    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  private static String escapeJson(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static String parseOpenRouterResponse(String responseJson) {
    try {
      int idx = responseJson.indexOf("\"content\":");
      if (idx == -1) {
        return "Invalid response: " + responseJson;
      }
      int start = responseJson.indexOf("\"", idx + 10) + 1;
      int end = responseJson.indexOf("\"", start);
      return responseJson.substring(start, end);
    } catch (Exception e) {
      return "Parsing error: " + e.getMessage();
    }
  }
}
