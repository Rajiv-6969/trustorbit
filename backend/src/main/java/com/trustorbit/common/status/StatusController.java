package com.trustorbit.common.status;

import com.trustorbit.common.station.StationRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lightweight liveness/status endpoint used by the front end to detect snapshot mode. */
@RestController
public class StatusController {

  private final StationRepository stations;

  /**
   * @param stations station storage
   */
  public StatusController(StationRepository stations) {
    this.stations = stations;
  }

  /**
   * @return engine name, server time and basic counts
   */
  @GetMapping("/api/status")
  public Map<String, Object> status() {
    return Map.of(
        "engine",
        "TrustOrbit",
        "status",
        "orbiting",
        "serverTime",
        Instant.now().toString(),
        "stations",
        stations.count());
  }
}
