package com.trustorbit.ingestion;

import com.trustorbit.model.Csv;
import com.trustorbit.model.Observation;
import com.trustorbit.model.Observation.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads observations from a CSV file in the TrustOrbit format (see {@link ObservationCsv}).
 *
 * <p>Only {@code station_id}, {@code timestamp_utc} and {@code value} are required; other columns
 * default sensibly so hand-made CSVs can be scored too.
 */
public final class CsvSource implements DataSource {

  private final Path file;

  /**
   * @param file the CSV file
   */
  public CsvSource(Path file) {
    this.file = file;
  }

  @Override
  public String describe() {
    return file.getFileName().toString();
  }

  @Override
  public List<Observation> read() throws IOException {
    Instant readAt = Instant.now();
    List<Observation> out = new ArrayList<>();
    for (Map<String, String> row : Csv.readWithHeader(Files.readAllLines(file))) {
      String raw = row.getOrDefault("value", "");
      Double value = raw.isEmpty() ? null : Double.valueOf(raw);
      String statusText = row.getOrDefault("status", "");
      Status status =
          statusText.isEmpty() ? (value == null ? Status.FILL : Status.OK) : Status.valueOf(statusText);
      out.add(
          new Observation(
              required(row, "station_id"),
              row.getOrDefault("source", "CSV"),
              row.getOrDefault("satellite", ""),
              row.getOrDefault("product", ""),
              Instant.parse(required(row, "timestamp_utc")),
              parseOrNaN(row.get("lat")),
              parseOrNaN(row.get("lon")),
              value,
              row.getOrDefault("units", ""),
              row.getOrDefault("quality_flag", ""),
              status,
              row.getOrDefault("file_name", describe()),
              readAt));
    }
    return out;
  }

  private String required(Map<String, String> row, String column) throws IOException {
    String v = row.get(column);
    if (v == null || v.isEmpty()) {
      throw new IOException(describe() + ": missing required column '" + column + "'");
    }
    return v;
  }

  private static double parseOrNaN(String s) {
    return s == null || s.isEmpty() ? Double.NaN : Double.parseDouble(s);
  }
}
