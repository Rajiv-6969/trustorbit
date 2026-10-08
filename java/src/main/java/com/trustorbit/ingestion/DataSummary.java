package com.trustorbit.ingestion;

import com.trustorbit.model.Observation;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Counts per product/satellite: records, real values, fill values, files and date range.
 *
 * @param rows one row per product/satellite pair
 */
public record DataSummary(List<Row> rows) {

  /**
   * @param key e.g. {@code LST / INSAT-3DS}
   * @param records all station records
   * @param values real readings
   * @param missing records without a usable value
   * @param files source files
   * @param stations distinct stations
   * @param first earliest timestamp
   * @param last latest timestamp
   */
  public record Row(
      String key, long records, long values, long missing, long files, long stations,
      Instant first, Instant last) {}

  /**
   * @param observations observations to summarise
   * @return the summary
   */
  public static DataSummary of(List<Observation> observations) {
    Map<String, List<Observation>> groups =
        observations.stream()
            .collect(
                Collectors.groupingBy(
                    o -> o.product() + " / " + o.satellite(), TreeMap::new, Collectors.toList()));
    List<Row> rows =
        groups.entrySet().stream()
            .map(
                e -> {
                  List<Observation> g = e.getValue();
                  long values = g.stream().filter(Observation::hasValue).count();
                  return new Row(
                      e.getKey(),
                      g.size(),
                      values,
                      g.size() - values,
                      g.stream().map(Observation::fileName).distinct().count(),
                      g.stream().map(Observation::stationId).distinct().count(),
                      g.stream().map(Observation::timestamp).min(Instant::compareTo).orElse(null),
                      g.stream().map(Observation::timestamp).max(Instant::compareTo).orElse(null));
                })
            .toList();
    return new DataSummary(rows);
  }

  /** @return a plain-text table */
  public String render() {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "%-18s %7s %7s %8s %6s %8s  %s%n",
            "product/satellite", "records", "values", "missing", "files", "stations", "range"));
    for (Row r : rows) {
      sb.append(
          String.format(
              "%-18s %7d %7d %8d %6d %8d  %s .. %s%n",
              r.key(), r.records(), r.values(), r.missing(), r.files(), r.stations(),
              r.first() == null ? "-" : r.first().toString().substring(0, 10),
              r.last() == null ? "-" : r.last().toString().substring(0, 10)));
    }
    return sb.toString();
  }
}
