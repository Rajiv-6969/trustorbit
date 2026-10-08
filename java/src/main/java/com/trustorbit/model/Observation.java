package com.trustorbit.model;

import java.time.Instant;

/**
 * One satellite reading for one station, exactly as read from the source file.
 *
 * @param stationId station code, e.g. {@code BLR}
 * @param source data centre, e.g. {@code MOSDAC}
 * @param satellite e.g. {@code INSAT-3DS}
 * @param product product code, e.g. {@code LST} or {@code HEM}
 * @param timestamp observation time in UTC
 * @param latitude latitude of the satellite pixel used
 * @param longitude longitude of the satellite pixel used
 * @param value reading in {@code units}, or {@code null} when missing
 * @param units units as given by the source (e.g. {@code K})
 * @param qualityFlag the product's own quality flag, or empty if it has none
 * @param status {@link Status} explaining why a value is present or missing
 * @param fileName file the value came from
 * @param readAt when TrustOrbit read the file
 */
public record Observation(
    String stationId,
    String source,
    String satellite,
    String product,
    Instant timestamp,
    double latitude,
    double longitude,
    Double value,
    String units,
    String qualityFlag,
    Status status,
    String fileName,
    Instant readAt) {

  /** Why a value is (or is not) usable. Missing values are never treated as real readings. */
  public enum Status {
    /** A real reading. */
    OK,
    /** The file holds its fill value (no retrieval, e.g. cloud). */
    FILL,
    /** The product's quality flag marks the pixel invalid. */
    INVALID_QC,
    /** No satellite pixel close enough to the station. */
    NO_PIXEL
  }

  /** @return true if this is a real reading */
  public boolean hasValue() {
    return status == Status.OK && value != null;
  }
}
