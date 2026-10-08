package com.trustorbit.common.station;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

/** A named sample location in India that we pull satellite time series for. */
@Entity
@Table(name = "station")
public class Station {

  @Id
  @Column(name = "station_id", length = 8)
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String state;

  /** WGS84 (SRID 4326) point; x = longitude, y = latitude. */
  @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
  private Point location;

  protected Station() {}

  /**
   * Creates a station.
   *
   * @param id short code such as {@code BLR}
   * @param name city name
   * @param state Indian state or union territory
   * @param location WGS84 point (x = lon, y = lat)
   */
  public Station(String id, String name, String state, Point location) {
    this.id = id;
    this.name = name;
    this.state = state;
    this.location = location;
  }

  /**
   * @return short station code, e.g. {@code BLR}
   */
  public String getId() {
    return id;
  }

  /**
   * @return city name
   */
  public String getName() {
    return name;
  }

  /**
   * @return state or union territory
   */
  public String getState() {
    return state;
  }

  /**
   * @return WGS84 location
   */
  public Point getLocation() {
    return location;
  }

  /**
   * @return latitude in decimal degrees
   */
  public double latitude() {
    return location.getY();
  }

  /**
   * @return longitude in decimal degrees
   */
  public double longitude() {
    return location.getX();
  }
}
