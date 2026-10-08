package com.trustorbit.common.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class GeoTest {

  @Test
  void pointStoresLongitudeAsXAndLatitudeAsY() {
    var p = Geo.point(12.97, 77.59);
    assertThat(p.getX()).isEqualTo(77.59);
    assertThat(p.getY()).isEqualTo(12.97);
  }

  @Test
  void bengaluruToChennaiIsAbout290Km() {
    assertThat(Geo.distanceKm(12.9716, 77.5946, 13.0827, 80.2707)).isCloseTo(290, within(10.0));
  }

  @Test
  void rejectsImpossibleCoordinates() {
    assertThatThrownBy(() -> Geo.point(-999, 77)).isInstanceOf(IllegalArgumentException.class);
  }
}
