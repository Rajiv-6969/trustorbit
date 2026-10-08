package com.trustorbit.model;

/**
 * A named sample location in India.
 *
 * @param id short code such as {@code BLR}
 * @param name city name
 * @param state state or union territory
 * @param latitude decimal degrees (WGS84)
 * @param longitude decimal degrees (WGS84)
 */
public record Station(String id, String name, String state, double latitude, double longitude) {

  /** Validates coordinates. */
  public Station {
    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Invalid coordinates for " + id);
    }
  }
}
