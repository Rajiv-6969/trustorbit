package com.trustorbit.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The list of sample stations, loaded from {@code data/stations.csv}. */
public final class StationCatalog {

  private final List<Station> stations;

  /**
   * @param stations stations in display order
   */
  public StationCatalog(List<Station> stations) {
    this.stations = List.copyOf(stations);
  }

  /**
   * Reads {@code station_id,name,state,latitude,longitude}.
   *
   * @param file the CSV file
   * @return the catalog
   * @throws IOException if the file cannot be read
   */
  public static StationCatalog load(Path file) throws IOException {
    List<Station> list = new ArrayList<>();
    for (Map<String, String> row : Csv.readWithHeader(Files.readAllLines(file))) {
      list.add(
          new Station(
              row.get("station_id"),
              row.get("name"),
              row.get("state"),
              Double.parseDouble(row.get("latitude")),
              Double.parseDouble(row.get("longitude"))));
    }
    return new StationCatalog(list);
  }

  /** @return all stations */
  public List<Station> all() {
    return stations;
  }

  /**
   * @param id station code
   * @return the station, if known
   */
  public Optional<Station> find(String id) {
    return stations.stream().filter(s -> s.id().equals(id)).findFirst();
  }

  /**
   * Nearest other stations, closest first (used later for neighbour-consistency checks).
   *
   * @param station the reference station
   * @param count how many neighbours to return
   * @return neighbours sorted by distance
   */
  public List<Station> neighbours(Station station, int count) {
    return stations.stream()
        .filter(s -> !s.id().equals(station.id()))
        .sorted(
            Comparator.comparingDouble(
                s ->
                    Geo.distanceKm(
                        station.latitude(), station.longitude(), s.latitude(), s.longitude())))
        .limit(count)
        .toList();
  }
}
