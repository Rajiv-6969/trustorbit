package com.trustorbit.ingestion;

import com.trustorbit.model.Csv;
import com.trustorbit.model.Observation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads and writes the TrustOrbit observation CSV format used in {@code data/isro/}.
 *
 * <p>Columns: station_id, source, satellite, product, timestamp_utc, lat, lon, value, units,
 * quality_flag, status, file_name, read_at_utc. Missing values have an empty {@code value} and a
 * non-OK {@code status}.
 */
public final class ObservationCsv {

  /** Column order of the CSV files. */
  public static final List<String> HEADER =
      List.of(
          "station_id", "source", "satellite", "product", "timestamp_utc", "lat", "lon", "value",
          "units", "quality_flag", "status", "file_name", "read_at_utc");

  private ObservationCsv() {}

  /**
   * File name for one product/satellite pair, e.g. {@code lst_insat-3ds.csv}.
   *
   * @param product product code
   * @param satellite satellite name
   * @return the file name
   */
  public static String fileNameFor(String product, String satellite) {
    return (product + "_" + satellite).toLowerCase(Locale.ROOT) + ".csv";
  }

  /**
   * Appends observations to one CSV per product/satellite, skipping files already converted.
   *
   * @param folder target folder (created if needed)
   * @param observations observations to write
   * @throws IOException if writing fails
   */
  public static void appendByProduct(Path folder, List<Observation> observations)
      throws IOException {
    Files.createDirectories(folder);
    Map<String, List<Observation>> groups = new LinkedHashMap<>();
    for (Observation o : observations) {
      groups.computeIfAbsent(fileNameFor(o.product(), o.satellite()), k -> new ArrayList<>()).add(o);
    }
    for (Map.Entry<String, List<Observation>> e : groups.entrySet()) {
      Path csv = folder.resolve(e.getKey());
      Set<String> done = convertedFiles(csv);
      List<String> lines = new ArrayList<>();
      if (!Files.exists(csv)) {
        lines.add(String.join(",", HEADER));
      }
      e.getValue().stream()
          .filter(o -> !done.contains(o.fileName()))
          .sorted(Comparator.comparing(Observation::timestamp).thenComparing(Observation::stationId))
          .forEach(o -> lines.add(format(o)));
      Files.write(csv, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
  }

  /**
   * @param csv an observation CSV (may not exist)
   * @return the source file names already present in it
   * @throws IOException if the file cannot be read
   */
  public static Set<String> convertedFiles(Path csv) throws IOException {
    Set<String> names = new HashSet<>();
    if (Files.exists(csv)) {
      for (Map<String, String> row : Csv.readWithHeader(Files.readAllLines(csv))) {
        names.add(row.get("file_name"));
      }
    }
    return names;
  }

  /**
   * @param o an observation
   * @return its CSV line
   */
  public static String format(Observation o) {
    return Csv.formatLine(
        List.of(
            o.stationId(), o.source(), o.satellite(), o.product(), o.timestamp().toString(),
            String.valueOf(o.latitude()), String.valueOf(o.longitude()),
            o.value() == null ? "" : String.valueOf(o.value()), o.units(), o.qualityFlag(),
            o.status().name(), o.fileName(), o.readAt().toString()));
  }
}
