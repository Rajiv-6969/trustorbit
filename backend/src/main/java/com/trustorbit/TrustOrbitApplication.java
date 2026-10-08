package com.trustorbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/** Entry point of the TrustOrbit back end. "Not all satellite data is created equal." */
@SpringBootApplication
@EnableCaching
public class TrustOrbitApplication {

  /**
   * Starts the Spring Boot application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(TrustOrbitApplication.class, args);
  }
}
