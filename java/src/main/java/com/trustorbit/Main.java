package com.trustorbit;

import com.trustorbit.ingestion.CsvSource;
import com.trustorbit.ingestion.DataSummary;
import com.trustorbit.ingestion.IsroFileReader;
import com.trustorbit.ingestion.IsroProduct;
import com.trustorbit.ingestion.MosdacClient;
import com.trustorbit.ingestion.MosdacDownloader;
import com.trustorbit.ingestion.ObservationCsv;
import com.trustorbit.model.Observation;
import com.trustorbit.model.StationCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * TrustOrbit command-line entry point.
 *
 * <p>Pipeline: ingest → clean → score → store → export. Run with {@code ./mvnw -q exec:java
 * -Dexec.args="run"} from the {@code java/} folder.
 */
public final class Main {

  private static final String USAGE =
      """
      TrustOrbit - satellite data reliability

      Usage: ./mvnw -q exec:java -Dexec.args="<command> [arguments]"

      Commands
        run                         full pipeline on data/isro/*.csv
        summary                     data summary of data/isro/*.csv
        search <from> <to>          list matching MOSDAC files (no login needed)
        download <from> <to>        download + convert MOSDAC files (needs MOSDAC login in .env)
        convert <file-or-folder>    convert downloaded .h5/.nc files into data/isro/*.csv
        score <file.csv>            score any CSV                     (Phase 3)
        evaluate                    synthetic precision/recall test   (Phase 3)
        report <region> <from> <to> make a PDF quality report         (Phase 4)

      Dates are YYYY-MM-DD (UTC).
      """;

  private Main() {}

  /**
   * Runs one command.
   *
   * @param args command and its arguments
   */
  public static void main(String[] args) {
    if (args.length == 0 || args[0].equals("help") || args[0].equals("--help")) {
      System.out.println(USAGE);
      return;
    }
    try {
      AppConfig config = AppConfig.load();
      int exit = run(config, args);
      if (exit != 0) {
        System.exit(exit);
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  static int run(AppConfig config, String[] args) throws IOException, InterruptedException {
    StationCatalog stations = StationCatalog.load(config.stationsFile());
    switch (args[0]) {
      case "run" -> {
        return runPipeline(config);
      }
      case "summary" -> {
        System.out.println(DataSummary.of(ingest(config)).render());
        return 0;
      }
      case "search" -> {
        requireArgs(args, 3);
        MosdacDownloader downloader =
            new MosdacDownloader(new MosdacClient(), config, stations, IsroProduct.defaults());
        downloader.printSearch(LocalDate.parse(args[1]), LocalDate.parse(args[2]));
        return 0;
      }
      case "download" -> {
        requireArgs(args, 3);
        MosdacDownloader downloader =
            new MosdacDownloader(new MosdacClient(), config, stations, IsroProduct.defaults());
        downloader.download(LocalDate.parse(args[1]), LocalDate.parse(args[2]));
        return 0;
      }
      case "convert" -> {
        requireArgs(args, 2);
        return convert(config, stations, Path.of(args[1]));
      }
      case "score", "evaluate", "report" -> {
        System.out.println("'" + args[0] + "' is planned for a later phase (see the README timeline).");
        return 2;
      }
      default -> {
        System.out.println("Unknown command: " + args[0] + "\n" + USAGE);
        return 2;
      }
    }
  }

  private static int runPipeline(AppConfig config) throws IOException {
    System.out.println("TrustOrbit pipeline");
    System.out.println("[1/5] ingest  - reading " + config.isroDir());
    List<Observation> observations = ingest(config);
    if (observations.isEmpty()) {
      System.out.println(
          "No ISRO data yet. Download it with: download <from> <to>  (see DATA_SOURCES.md)");
      return 1;
    }
    System.out.println(DataSummary.of(observations).render());
    System.out.println("[2/5] clean   - Phase 2 (M2 preprocessing)");
    System.out.println("[3/5] score   - Phase 3 (M3 quality engine)");
    System.out.println("[4/5] store   - Phase 4 (M4 SQLite)");
    System.out.println("[5/5] export  - Phase 4 (M4 JSON + PDF)");
    return 0;
  }

  private static List<Observation> ingest(AppConfig config) throws IOException {
    List<Observation> all = new ArrayList<>();
    if (!Files.isDirectory(config.isroDir())) {
      return all;
    }
    try (Stream<Path> files = Files.list(config.isroDir())) {
      for (Path csv : files.filter(p -> p.toString().endsWith(".csv")).sorted().toList()) {
        all.addAll(new CsvSource(csv).read());
      }
    }
    return all;
  }

  private static int convert(AppConfig config, StationCatalog stations, Path input)
      throws IOException {
    List<Path> files = IsroFileReader.findFiles(input);
    if (files.isEmpty()) {
      System.out.println("No .h5 or .nc files found in " + input);
      return 1;
    }
    double maxKm = config.getDouble("ingestion.maxPixelDistanceKm");
    List<Observation> observations = new IsroFileReader(files, stations, maxKm).read();
    ObservationCsv.appendByProduct(config.isroDir(), observations);
    System.out.printf(
        "Converted %d file(s) into %d station records in %s%n",
        files.size(), observations.size(), config.isroDir());
    return 0;
  }

  private static void requireArgs(String[] args, int count) {
    if (args.length < count) {
      throw new IllegalArgumentException("Not enough arguments.\n" + USAGE);
    }
  }
}
