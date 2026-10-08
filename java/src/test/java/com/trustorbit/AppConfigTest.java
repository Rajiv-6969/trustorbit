package com.trustorbit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

  @Test
  void findsTheRepositoryRootFromTheJavaFolder() {
    Path root = AppConfig.findRepoRoot(Path.of("").toAbsolutePath());
    assertTrue(Files.isRegularFile(root.resolve("data/stations.csv")));
  }

  @Test
  void readsDotEnvIgnoringCommentsAndQuotes(@TempDir Path dir) throws Exception {
    Path env = dir.resolve(".env");
    Files.writeString(env, "# comment\nMOSDAC_USERNAME=orbit\nEMPTY=\nQUOTED=\"a b\"\n\n");
    Map<String, String> values = AppConfig.readDotEnv(env);
    assertEquals("orbit", values.get("MOSDAC_USERNAME"));
    assertEquals("a b", values.get("QUOTED"));
    assertEquals("", values.get("EMPTY"));
    assertFalse(values.containsKey("# comment"));
  }

  @Test
  void loadsDefaultSettings() throws Exception {
    AppConfig config = AppConfig.load();
    assertEquals(10.0, config.getDouble("ingestion.maxPixelDistanceKm"));
    double weights =
        config.getDouble("score.weight.completeness")
            + config.getDouble("score.weight.range")
            + config.getDouble("score.weight.anomaly")
            + config.getDouble("score.weight.drift")
            + config.getDouble("score.weight.crossSatellite")
            + config.getDouble("score.weight.neighbours");
    assertEquals(1.0, weights, 1e-9);
  }
}
