package com.trustorbit.ingestion;

import com.trustorbit.model.Observation;
import java.io.IOException;
import java.util.List;

/**
 * Anything TrustOrbit can read observations from. New sources (e.g. NASA, later) only need a new
 * implementation – the rest of the pipeline does not change.
 */
public interface DataSource {

  /** @return a short human description, e.g. the file name */
  String describe();

  /**
   * Reads all observations.
   *
   * @return observations in source order
   * @throws IOException if the source cannot be read
   */
  List<Observation> read() throws IOException;
}
