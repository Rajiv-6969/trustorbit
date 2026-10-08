package com.trustorbit.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.trustorbit.model.Observation;
import com.trustorbit.model.Observation.Status;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ObservationCsvTest {

  private static Observation obs(String station, Double value, Status status) {
    return new Observation(
        station, "MOSDAC", "INSAT-3DS", "LST", Instant.parse("2025-10-01T06:00:00Z"), 12.97,
        77.59, value, "K", "", status, "3SIMG_01OCT2025_0600_L2B_LST_V01R00.h5",
        Instant.parse("2026-10-08T10:00:00Z"));
  }

  @Test
  void writesAndReadsBackIncludingMissingValues(@TempDir Path dir) throws Exception {
    ObservationCsv.appendByProduct(dir, List.of(obs("BLR", 301.25, Status.OK), obs("MAA", null, Status.FILL)));
    Path csv = dir.resolve("lst_insat-3ds.csv");

    List<Observation> back = new CsvSource(csv).read();
    assertEquals(2, back.size());
    assertEquals(301.25, back.get(0).value());
    assertNull(back.get(1).value());
    assertEquals(Status.FILL, back.get(1).status());
  }

  @Test
  void skipsFilesThatWereAlreadyConverted(@TempDir Path dir) throws Exception {
    List<Observation> batch = List.of(obs("BLR", 301.25, Status.OK));
    ObservationCsv.appendByProduct(dir, batch);
    ObservationCsv.appendByProduct(dir, batch);
    assertEquals(2, Files.readAllLines(dir.resolve("lst_insat-3ds.csv")).size()); // header + 1
  }
}
