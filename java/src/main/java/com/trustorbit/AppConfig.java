package com.trustorbit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Settings for the whole program.
 *
 * <p>Tunable numbers (weights, thresholds) come from {@code config.properties} on the classpath.
 * Secrets (MOSDAC login) come from environment variables or the git-ignored {@code .env} file in
 * the repository root, so they never end up in source control.
 */
public final class AppConfig {

  private final Path repoRoot;
  private final Properties properties;
  private final Map<String, String> env;

  AppConfig(Path repoRoot, Properties properties, Map<String, String> env) {
    this.repoRoot = repoRoot;
    this.properties = properties;
    this.env = env;
  }

  /**
   * Loads settings, locating the repository root from the current working directory.
   *
   * @return the configuration
   * @throws IOException if {@code config.properties} or {@code .env} cannot be read
   */
  public static AppConfig load() throws IOException {
    Path root = findRepoRoot(Path.of("").toAbsolutePath());
    Properties props = new Properties();
    try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
      if (in == null) {
        throw new IOException("config.properties missing from classpath");
      }
      props.load(in);
    }
    Map<String, String> env = new HashMap<>(readDotEnv(root.resolve(".env")));
    env.putAll(System.getenv());
    return new AppConfig(root, props, env);
  }

  /**
   * Walks up from {@code start} until a folder containing {@code data/stations.csv} is found.
   *
   * @param start folder to start from
   * @return the repository root
   */
  static Path findRepoRoot(Path start) {
    for (Path p = start; p != null; p = p.getParent()) {
      if (Files.isRegularFile(p.resolve("data").resolve("stations.csv"))) {
        return p;
      }
    }
    throw new IllegalStateException(
        "Run TrustOrbit from inside the repository (data/stations.csv not found above " + start + ")");
  }

  /**
   * Parses a simple {@code KEY=VALUE} file. Blank lines and {@code #} comments are ignored.
   *
   * @param file path to the .env file (may not exist)
   * @return the key/value pairs, empty if the file is missing
   * @throws IOException if the file exists but cannot be read
   */
  static Map<String, String> readDotEnv(Path file) throws IOException {
    Map<String, String> values = new HashMap<>();
    if (!Files.isRegularFile(file)) {
      return values;
    }
    List<String> lines = Files.readAllLines(file);
    for (String line : lines) {
      String trimmed = line.strip();
      int eq = trimmed.indexOf('=');
      if (trimmed.isEmpty() || trimmed.startsWith("#") || eq < 1) {
        continue;
      }
      String value = trimmed.substring(eq + 1).strip();
      if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
        value = value.substring(1, value.length() - 1);
      }
      values.put(trimmed.substring(0, eq).strip(), value);
    }
    return values;
  }

  /** @return repository root folder */
  public Path repoRoot() {
    return repoRoot;
  }

  /** @return {@code data/} */
  public Path dataDir() {
    return repoRoot.resolve("data");
  }

  /** @return {@code data/stations.csv} */
  public Path stationsFile() {
    return dataDir().resolve("stations.csv");
  }

  /** @return {@code data/isro/} – converted ISRO point time series (committed) */
  public Path isroDir() {
    return dataDir().resolve("isro");
  }

  /** @return {@code data/raw/} – downloaded HDF5 files (git-ignored, deleted after conversion) */
  public Path rawDir() {
    return dataDir().resolve("raw");
  }

  /** @return {@code website/public/data/} – JSON consumed by the website */
  public Path websiteDataDir() {
    return repoRoot.resolve("website").resolve("public").resolve("data");
  }

  /** @return {@code website/public/reports/} – generated PDF reports */
  public Path reportsDir() {
    return repoRoot.resolve("website").resolve("public").resolve("reports");
  }

  /**
   * @param key property name in config.properties
   * @return the value
   * @throws IllegalArgumentException if the key is missing
   */
  public String get(String key) {
    String value = properties.getProperty(key);
    if (value == null) {
      throw new IllegalArgumentException("Missing setting in config.properties: " + key);
    }
    return value.strip();
  }

  /**
   * @param key property name in config.properties
   * @return the value as a number
   */
  public double getDouble(String key) {
    return Double.parseDouble(get(key));
  }

  /**
   * Reads a secret from the environment or {@code .env}.
   *
   * @param name variable name, e.g. {@code MOSDAC_USERNAME}
   * @return the value, or {@code null} if it is not set or blank
   */
  public String secret(String name) {
    String value = env.get(name);
    return value == null || value.isBlank() ? null : value;
  }
}
