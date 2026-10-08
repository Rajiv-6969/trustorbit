package com.trustorbit.ingestion;

import java.util.List;
import java.util.Optional;

/**
 * The MOSDAC products TrustOrbit uses. Product IDs were verified against the MOSDAC search API.
 *
 * <p>Two satellites (INSAT-3DR and INSAT-3DS) observe the same quantities, which enables the
 * cross-satellite check. Half-hourly products are thinned to one time slot per day.
 */
public enum IsroProduct {
  /** Land surface temperature, INSAT-3DR, half-hourly (we keep the 06:15 UTC file). */
  LST_3DR("3RIMG_L2B_LST", "INSAT-3DR", "LST", "0615", List.of("LST")),
  /** Land surface temperature, INSAT-3DS, half-hourly (we keep the 06:00 UTC file). */
  LST_3DS("3SIMG_L2B_LST", "INSAT-3DS", "LST", "0600", List.of("LST")),
  /** Daily rainfall (Hydro-Estimator), INSAT-3DR. */
  HEM_3DR("3RIMG_L3B_HEM_DLY", "INSAT-3DR", "HEM", null, List.of("HEM", "HEM_DLY", "Rainfall")),
  /** Daily rainfall (Hydro-Estimator), INSAT-3DS. */
  HEM_3DS("3SIMG_L3B_HEM_DLY", "INSAT-3DS", "HEM", null, List.of("HEM", "HEM_DLY", "Rainfall"));

  private final String datasetId;
  private final String satellite;
  private final String product;
  private final String timeSlot;
  private final List<String> variableNames;

  IsroProduct(
      String datasetId,
      String satellite,
      String product,
      String timeSlot,
      List<String> variableNames) {
    this.datasetId = datasetId;
    this.satellite = satellite;
    this.product = product;
    this.timeSlot = timeSlot;
    this.variableNames = variableNames;
  }

  /** @return the products downloaded by default */
  public static List<IsroProduct> defaults() {
    return List.of(values());
  }

  /**
   * @param datasetId MOSDAC dataset ID such as {@code 3SIMG_L2B_LST}
   * @return the matching product, if TrustOrbit knows it
   */
  public static Optional<IsroProduct> byDatasetId(String datasetId) {
    for (IsroProduct p : values()) {
      if (p.datasetId.equals(datasetId)) {
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  /** @return MOSDAC dataset ID */
  public String datasetId() {
    return datasetId;
  }

  /** @return satellite name */
  public String satellite() {
    return satellite;
  }

  /** @return product code (LST, HEM) */
  public String product() {
    return product;
  }

  /** @return HHmm UTC slot to keep, or {@code null} for daily products */
  public String timeSlot() {
    return timeSlot;
  }

  /** @return candidate variable names inside the file, most likely first */
  public List<String> variableNames() {
    return variableNames;
  }

  /**
   * @param name a MOSDAC file name
   * @return true if this file should be downloaded (right time slot)
   */
  public boolean wants(IsroFileName name) {
    return timeSlot == null || name.timeSlot().equals(timeSlot);
  }
}
