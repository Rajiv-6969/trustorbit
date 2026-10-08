package com.trustorbit.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.trustorbit.AppConfig;
import java.util.List;
import org.junit.jupiter.api.Test;

class StationCatalogTest {

  private final StationCatalog catalog = load();

  private static StationCatalog load() {
    try {
      return StationCatalog.load(AppConfig.load().stationsFile());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void loadsTheTenIndianStations() {
    assertEquals(10, catalog.all().size());
    for (Station s : catalog.all()) {
      assertTrue(s.latitude() > 6 && s.latitude() < 37.5, s.id());
      assertTrue(s.longitude() > 68 && s.longitude() < 98, s.id());
    }
    assertEquals("Bengaluru", catalog.find("BLR").orElseThrow().name());
  }

  @Test
  void nearestNeighboursOfBengaluruAreChennaiThenThiruvananthapuram() {
    List<Station> n = catalog.neighbours(catalog.find("BLR").orElseThrow(), 2);
    assertEquals(List.of("MAA", "TRV"), n.stream().map(Station::id).toList());
  }

  @Test
  void bengaluruToChennaiIsAbout290Km() {
    assertEquals(290, Geo.distanceKm(12.9716, 77.5946, 13.0827, 80.2707), 10);
  }
}
