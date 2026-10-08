package com.trustorbit.common.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/** Small helpers for WGS84 geometry. */
public final class Geo {

  /** SRID for WGS84 longitude/latitude. */
  public static final int WGS84 = 4326;

  private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), WGS84);

  private Geo() {}

  /**
   * Builds a WGS84 point.
   *
   * @param latitude decimal degrees, -90..90
   * @param longitude decimal degrees, -180..180
   * @return a JTS point with x = longitude and y = latitude
   */
  public static Point point(double latitude, double longitude) {
    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Invalid coordinate: " + latitude + "," + longitude);
    }
    return FACTORY.createPoint(new Coordinate(longitude, latitude));
  }

  /**
   * Great-circle distance using the haversine formula.
   *
   * @return distance in kilometres
   */
  public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
    return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}
