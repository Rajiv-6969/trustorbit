package com.trustorbit.common.station;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Inserts the sample stations on start-up if the table is empty (idempotent). */
@Component
@Order(1)
public class StationSeeder implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(StationSeeder.class);
  private final StationRepository repository;

  /**
   * @param repository station storage
   */
  public StationSeeder(StationRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws IOException {
    if (repository.count() > 0) {
      LOG.info("Stations already seeded ({} rows)", repository.count());
      return;
    }
    try (InputStream in = new ClassPathResource("seed/stations.csv").getInputStream()) {
      List<Station> stations = StationCsvReader.read(in);
      repository.saveAll(stations);
      LOG.info(
          "Seeded {} stations: {}",
          stations.size(),
          stations.stream().map(Station::getId).toList());
    }
  }
}
