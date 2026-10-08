package com.trustorbit.common.station;

import com.trustorbit.common.geo.Geo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/** Parses {@code data/stations.csv} (station_id,name,state,latitude,longitude). */
public final class StationCsvReader {

  private StationCsvReader() {}

  /**
   * Reads all stations from a CSV stream.
   *
   * @param in UTF-8 CSV with a header row
   * @return stations in file order
   * @throws IOException if the stream cannot be read
   */
  public static List<Station> read(InputStream in) throws IOException {
    CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get();
    List<Station> stations = new ArrayList<>();
    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      for (CSVRecord row : format.parse(reader)) {
        stations.add(
            new Station(
                row.get("station_id").trim(),
                row.get("name").trim(),
                row.get("state").trim(),
                Geo.point(
                    Double.parseDouble(row.get("latitude")),
                    Double.parseDouble(row.get("longitude")))));
      }
    }
    return stations;
  }
}
