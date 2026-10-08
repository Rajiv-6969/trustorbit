package com.trustorbit.ingestion;

import com.trustorbit.AppConfig;
import com.trustorbit.model.Observation;
import com.trustorbit.model.StationCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Downloads only the MOSDAC files TrustOrbit needs (one time slot per day for half-hourly
 * products), converts each file to station CSV rows straight away and then deletes the raw file,
 * so the large HDF5 files never pile up or get committed.
 */
public final class MosdacDownloader {

  private final MosdacClient client;
  private final AppConfig config;
  private final StationCatalog stations;
  private final List<IsroProduct> products;

  /**
   * @param client MOSDAC API client
   * @param config settings (folders, .env secrets)
   * @param stations stations to extract
   * @param products products to fetch
   */
  public MosdacDownloader(
      MosdacClient client, AppConfig config, StationCatalog stations, List<IsroProduct> products) {
    this.client = client;
    this.config = config;
    this.stations = stations;
    this.products = products;
  }

  /**
   * Prints what a download would fetch. No login needed.
   *
   * @param from first date (UTC)
   * @param to last date (UTC)
   * @throws IOException if MOSDAC cannot be reached
   * @throws InterruptedException if interrupted
   */
  public void printSearch(LocalDate from, LocalDate to) throws IOException, InterruptedException {
    System.out.printf("MOSDAC search %s .. %s (no login needed)%n", from, to);
    System.out.printf("%-20s %-10s %8s %8s %10s%n", "dataset", "satellite", "found", "wanted", "est. MB");
    for (IsroProduct p : products) {
      MosdacClient.SearchResult r = client.search(p.datasetId(), from, to);
      List<MosdacClient.Entry> wanted = wanted(p, r.entries());
      double perFile = r.totalResults() == 0 ? 0 : r.totalSizeMb() / r.totalResults();
      System.out.printf(
          "%-20s %-10s %8d %8d %10.0f%n",
          p.datasetId(), p.satellite(), r.totalResults(), wanted.size(), wanted.size() * perFile);
    }
  }

  /**
   * Downloads, converts and deletes. Already-converted files are skipped, so it is safe to re-run
   * (for example after hitting MOSDAC's daily quota).
   *
   * @param from first date (UTC)
   * @param to last date (UTC)
   * @throws IOException on download or conversion errors
   * @throws InterruptedException if interrupted
   */
  public void download(LocalDate from, LocalDate to) throws IOException, InterruptedException {
    String user = config.secret("MOSDAC_USERNAME");
    String password = config.secret("MOSDAC_PASSWORD");
    if (user == null || password == null) {
      throw new IOException(
          "Set MOSDAC_USERNAME and MOSDAC_PASSWORD in the .env file (see .env.example).");
    }
    boolean keepRaw = Boolean.parseBoolean(config.get("ingestion.keepRawFiles"));
    double maxKm = config.getDouble("ingestion.maxPixelDistanceKm");
    IsroFileReader reader = new IsroFileReader(List.of(), stations, maxKm);

    client.login(user, password);
    System.out.println("Logged in to MOSDAC as " + user);
    try {
      for (IsroProduct p : products) {
        MosdacClient.SearchResult r = client.search(p.datasetId(), from, to);
        Path csv = config.isroDir().resolve(ObservationCsv.fileNameFor(p.product(), p.satellite()));
        Set<String> done = ObservationCsv.convertedFiles(csv);
        List<MosdacClient.Entry> todo =
            wanted(p, r.entries()).stream().filter(e -> !done.contains(e.identifier())).toList();
        System.out.printf("%s: %d file(s) to fetch%n", p.datasetId(), todo.size());
        int n = 0;
        for (MosdacClient.Entry e : todo) {
          n++;
          Path raw = config.rawDir().resolve(p.datasetId()).resolve(e.identifier());
          if (!Files.exists(raw) && !client.download(e, raw)) {
            System.out.printf("  [%d/%d] %s not available on MOSDAC - skipped%n", n, todo.size(), e.identifier());
            continue;
          }
          List<Observation> obs = reader.readFile(raw);
          ObservationCsv.appendByProduct(config.isroDir(), obs);
          long values = obs.stream().filter(Observation::hasValue).count();
          System.out.printf(
              "  [%d/%d] %s -> %d/%d stations with a value%n",
              n, todo.size(), e.identifier(), values, obs.size());
          if (!keepRaw) {
            Files.delete(raw);
          }
        }
      }
    } finally {
      client.logout();
    }
  }

  private static List<MosdacClient.Entry> wanted(IsroProduct p, List<MosdacClient.Entry> entries) {
    List<MosdacClient.Entry> out = new ArrayList<>();
    for (MosdacClient.Entry e : entries) {
      if (IsroFileName.matches(e.identifier()) && p.wants(IsroFileName.parse(e.identifier()))) {
        out.add(e);
      }
    }
    return out;
  }
}
