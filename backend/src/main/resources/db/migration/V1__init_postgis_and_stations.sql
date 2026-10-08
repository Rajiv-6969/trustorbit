-- TrustOrbit schema v1: enable PostGIS and store the sample stations.
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE station (
    station_id VARCHAR(8) PRIMARY KEY,
    name       TEXT NOT NULL,
    state      TEXT NOT NULL,
    location   geometry(Point, 4326) NOT NULL
);

CREATE INDEX station_location_gix ON station USING GIST (location);
