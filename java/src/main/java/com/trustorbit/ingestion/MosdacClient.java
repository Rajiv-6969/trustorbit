package com.trustorbit.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Talks to the official MOSDAC Data Download API (the same endpoints used by MOSDAC's own {@code
 * mdapi.py} tool, see https://mosdac.gov.in/downloadapi-manual).
 *
 * <p>Searching needs no login. Downloading needs a MOSDAC account; the password is only sent to
 * MOSDAC's token endpoint and is never logged or stored by TrustOrbit.
 */
public class MosdacClient {

  private static final String BASE = "https://mosdac.gov.in";
  private static final String SEARCH_URL = BASE + "/apios/datasets.json";
  private static final String TOKEN_URL = BASE + "/download_api/gettoken";
  private static final String REFRESH_URL = BASE + "/download_api/refresh-token";
  private static final String DOWNLOAD_URL = BASE + "/download_api/download";
  private static final String LOGOUT_URL = BASE + "/download_api/logout";
  private static final ObjectMapper JSON = new ObjectMapper();

  private final HttpClient http =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(30))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

  private String accessToken;
  private String refreshToken;
  private String username;

  /**
   * One downloadable file from a search.
   *
   * @param id MOSDAC record id (used to download)
   * @param identifier file name
   * @param sizeHint not provided per file by MOSDAC; kept for future use
   */
  public record Entry(String id, String identifier, long sizeHint) {}

  /**
   * Result of a search.
   *
   * @param totalResults number of files found
   * @param totalSizeMb total size reported by MOSDAC
   * @param entries the files
   */
  public record SearchResult(int totalResults, double totalSizeMb, List<Entry> entries) {}

  /**
   * Lists all files of a dataset between two dates (inclusive), following pagination.
   *
   * @param datasetId e.g. {@code 3SIMG_L2B_LST}
   * @param from first date (UTC)
   * @param to last date (UTC)
   * @return all matching files
   * @throws IOException if MOSDAC cannot be reached or rejects the query
   * @throws InterruptedException if interrupted
   */
  public SearchResult search(String datasetId, LocalDate from, LocalDate to)
      throws IOException, InterruptedException {
    List<Entry> entries = new ArrayList<>();
    int total = 0;
    double sizeMb = 0;
    int startIndex = 1;
    do {
      Map<String, String> q = new LinkedHashMap<>();
      q.put("datasetId", datasetId);
      q.put("startTime", from.toString());
      q.put("endTime", to.toString());
      q.put("startIndex", String.valueOf(startIndex));
      HttpResponse<String> res = send(HttpRequest.newBuilder(URI.create(SEARCH_URL + "?" + query(q))).GET());
      if (res.statusCode() != 200) {
        throw new IOException("MOSDAC search failed (" + res.statusCode() + "): " + message(res.body()));
      }
      SearchResult page = parseSearch(res.body());
      total = page.totalResults();
      sizeMb = page.totalSizeMb();
      entries.addAll(page.entries());
      if (page.entries().isEmpty()) {
        break;
      }
      startIndex += page.entries().size();
    } while (entries.size() < total);
    return new SearchResult(total, sizeMb, entries);
  }

  /**
   * Parses one page of the MOSDAC search response.
   *
   * @param body JSON text
   * @return the parsed page
   * @throws IOException if the JSON is malformed
   */
  public static SearchResult parseSearch(String body) throws IOException {
    JsonNode root = JSON.readTree(body);
    List<Entry> entries = new ArrayList<>();
    for (JsonNode e : root.path("entries")) {
      entries.add(new Entry(e.path("id").asText(), e.path("identifier").asText(), 0));
    }
    return new SearchResult(
        root.path("totalResults").asInt(), root.path("totalSizeMB").asDouble(), entries);
  }

  /**
   * Logs in and keeps the access token for later downloads.
   *
   * @param user MOSDAC user name
   * @param password MOSDAC password (sent only to MOSDAC)
   * @throws IOException if the login is rejected
   * @throws InterruptedException if interrupted
   */
  public void login(String user, String password) throws IOException, InterruptedException {
    String body = JSON.writeValueAsString(Map.of("username", user, "password", password));
    HttpResponse<String> res = send(jsonPost(TOKEN_URL, body));
    if (res.statusCode() != 200) {
      throw new IOException("MOSDAC login failed (" + res.statusCode() + "): " + message(res.body()));
    }
    JsonNode tokens = JSON.readTree(res.body());
    this.accessToken = tokens.path("access_token").asText(null);
    this.refreshToken = tokens.path("refresh_token").asText(null);
    this.username = user;
    if (accessToken == null) {
      throw new IOException("MOSDAC login returned no access token");
    }
  }

  /**
   * Downloads one file. Retries on per-minute rate limits and refreshes an expired token once.
   *
   * @param entry the file to download
   * @param target where to save it
   * @return true if downloaded, false if MOSDAC says the file is not available
   * @throws IOException on errors, including the daily download quota
   * @throws InterruptedException if interrupted
   */
  public boolean download(Entry entry, Path target) throws IOException, InterruptedException {
    if (accessToken == null) {
      throw new IllegalStateException("login() first");
    }
    boolean refreshed = false;
    for (int attempt = 0; attempt < 6; attempt++) {
      HttpRequest req =
          HttpRequest.newBuilder(URI.create(DOWNLOAD_URL + "?id=" + enc(entry.id())))
              .header("Authorization", "Bearer " + accessToken)
              .timeout(Duration.ofMinutes(10))
              .GET()
              .build();
      HttpResponse<InputStream> res = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
      int code = res.statusCode();
      if (code == 200) {
        if (res.headers().firstValue("Content-Disposition").filter(h -> h.contains("filename=")).isEmpty()) {
          res.body().close();
          return false; // MOSDAC's own signal for "file not available on the server"
        }
        Files.createDirectories(target.getParent());
        Path part = target.resolveSibling(target.getFileName() + ".part");
        try (InputStream in = res.body()) {
          Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(part, target, StandardCopyOption.REPLACE_EXISTING);
        return true;
      }
      String body = new String(res.body().readAllBytes(), StandardCharsets.UTF_8);
      JsonNode err = tryParse(body);
      if (code == 401 && "INVALID_TOKEN".equals(err.path("code").asText()) && !refreshed) {
        refresh();
        refreshed = true;
      } else if (code == 429 && "minute_limit".equals(err.path("type").asText())) {
        Thread.sleep(20_000);
      } else if (code == 429) {
        throw new IOException("MOSDAC daily download quota reached - run again tomorrow. " + message(body));
      } else if (code == 404 && "NOT_RELEASED".equals(err.path("code").asText())) {
        return false;
      } else if (code >= 500) {
        Thread.sleep(10_000L * (attempt + 1));
      } else {
        throw new IOException("Download of " + entry.identifier() + " failed (" + code + "): " + message(body));
      }
    }
    throw new IOException("Download of " + entry.identifier() + " kept failing; try again later");
  }

  /** Ends the MOSDAC session (best effort). */
  public void logout() {
    if (username == null) {
      return;
    }
    try {
      send(jsonPost(LOGOUT_URL, JSON.writeValueAsString(Map.of("username", username))));
    } catch (IOException | InterruptedException e) {
      System.err.println("MOSDAC logout failed (session will expire on its own): " + e.getMessage());
    } finally {
      accessToken = null;
      refreshToken = null;
      username = null;
    }
  }

  private void refresh() throws IOException, InterruptedException {
    String body = JSON.writeValueAsString(Map.of("refresh_token", refreshToken));
    HttpResponse<String> res = send(jsonPost(REFRESH_URL, body));
    if (res.statusCode() != 200) {
      throw new IOException("Could not refresh MOSDAC token; please run again");
    }
    JsonNode tokens = JSON.readTree(res.body());
    accessToken = tokens.path("access_token").asText(accessToken);
    refreshToken = tokens.path("refresh_token").asText(refreshToken);
  }

  private HttpResponse<String> send(HttpRequest.Builder req) throws IOException, InterruptedException {
    return http.send(req.timeout(Duration.ofSeconds(90)).build(), HttpResponse.BodyHandlers.ofString());
  }

  private static HttpRequest.Builder jsonPost(String url, String body) {
    return HttpRequest.newBuilder(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body));
  }

  private static String query(Map<String, String> params) {
    return params.entrySet().stream()
        .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
        .collect(Collectors.joining("&"));
  }

  private static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private static JsonNode tryParse(String body) {
    try {
      return JSON.readTree(body);
    } catch (IOException e) {
      return JSON.createObjectNode();
    }
  }

  private static String message(String body) {
    JsonNode n = tryParse(body);
    JsonNode m = n.path("message");
    if (m.isArray() && !m.isEmpty()) {
      return m.get(0).asText();
    }
    if (m.isTextual()) {
      return m.asText();
    }
    return n.path("error").asText(body.length() > 200 ? body.substring(0, 200) : body);
  }
}
