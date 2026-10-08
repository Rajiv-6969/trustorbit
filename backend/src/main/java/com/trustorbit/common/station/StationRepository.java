package com.trustorbit.common.station;

import org.springframework.data.jpa.repository.JpaRepository;

/** CRUD access to {@link Station} rows stored in PostGIS. */
public interface StationRepository extends JpaRepository<Station, String> {}
