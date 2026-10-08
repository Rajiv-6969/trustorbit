package com.trustorbit;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustorbit.common.station.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Boots the whole app against a real PostGIS container (needs Docker). */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrustOrbitApplicationIT {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgis =
      new PostgreSQLContainer<>(
          DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"));

  @Autowired StationRepository stations;
  @Autowired TestRestTemplate http;

  @Test
  void migratesSchemaSeedsStationsAndReportsStatus() {
    assertThat(stations.count()).isEqualTo(10);
    assertThat(stations.findById("IXL")).get().extracting(s -> s.getName()).isEqualTo("Leh");
    String body = http.getForObject("/api/status", String.class);
    assertThat(body).contains("\"stations\":10");
  }
}
