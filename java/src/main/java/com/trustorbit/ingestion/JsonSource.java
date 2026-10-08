package com.trustorbit.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustorbit.model.Observation;
import com.trustorbit.model.Observation.Status;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads observations from a JSON array of objects that use the same field names as the CSV
 * columns (e.g. {@code {"station_id":"BLR","timestamp_utc":"2025-10-01T06:00:00Z","value":301.2}}).
 */
public final class JsonSource implements DataSource {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final Path file;

  /**
   * @param file the JSON file
   */
  public JsonSource(Path file) {
    this.file = file;
  }

  @Override
  public String describe() {
    return file.getFileName().toString();
  }

  @Override
  public List<Observation> read() throws IOException {
    JsonNode root = MAPPER.readTree(file.toFile());
    if (!root.isArray()) {
      throw new IOException(describe() + ": expected a JSON array");
    }
    Instant readAt = Instant.now();
    List<Observation> out = new ArrayList<>();
    for (JsonNode n : root) {
      JsonNode v = n.get("value");
      Double value = v == null || v.isNull() ? null : v.asDouble();
      String statusText = n.path("status").asText("");
      Status status =
          statusText.isEmpty() ? (value == null ? Status.FILL : Status.OK) : Status.valueOf(statusText);
      out.add(
          new Observation(
              n.path("station_id").asText(),
              n.path("source").asText("JSON"),
              n.path("satellite").asText(""),
              n.path("product").asText(""),
              Instant.parse(n.path("timestamp_utc").asText()),
              n.path("lat").asDouble(Double.NaN),
              n.path("lon").asDouble(Double.NaN),
              value,
              n.path("units").asText(""),
              n.path("quality_flag").asText(""),
              status,
              n.path("file_name").asText(describe()),
              readAt));
    }
    return out;
  }
}
