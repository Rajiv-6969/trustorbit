package com.trustorbit.common.station;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class StationCsvReaderTest {

  @Test
  void readsTheRealStationListWithValidIndianCoordinates() throws Exception {
    try (InputStream in = new ClassPathResource("seed/stations.csv").getInputStream()) {
      List<Station> stations = StationCsvReader.read(in);

      assertThat(stations).hasSize(10);
      assertThat(stations).extracting(Station::getId).contains("BLR", "IXL", "TRV");
      assertThat(stations)
          .allSatisfy(
              s -> {
                assertThat(s.latitude()).isBetween(6.0, 37.5);
                assertThat(s.longitude()).isBetween(68.0, 98.0);
              });
      Station blr = stations.getFirst();
      assertThat(blr.getName()).isEqualTo("Bengaluru");
      assertThat(blr.getLocation().getSRID()).isEqualTo(4326);
    }
  }
}
