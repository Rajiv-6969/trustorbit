package com.trustorbit.ingestion;

import com.trustorbit.model.Geo;
import com.trustorbit.model.Observation;
import com.trustorbit.model.Observation.Status;
import com.trustorbit.model.Station;
import com.trustorbit.model.StationCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.dataset.VariableDS;

/**
 * Reads ISRO INSAT HDF5/NetCDF files with NetCDF-Java and extracts the value at each station.
 *
 * <p>The file is opened in "enhanced" mode, so {@code scale_factor}/{@code add_offset} are applied
 * and {@code _FillValue}/{@code missing_value} pixels become NaN. Those are recorded as {@link
 * Status#FILL} with no value – never as real readings.
 */
public final class IsroFileReader implements DataSource {

  private static final String SOURCE = "MOSDAC";
  private static final Set<String> COORDINATE_NAMES =
      Set.of("latitude", "longitude", "lat", "lon", "geox", "geoy", "x", "y", "time");

  private final List<Path> files;
  private final StationCatalog stations;
  private final double maxPixelDistanceKm;
  private final Map<String, int[][]> pixelCache = new HashMap<>();

  /**
   * @param files HDF5/NetCDF files following the MOSDAC naming convention
   * @param stations stations to extract
   * @param maxPixelDistanceKm stations further than this from the nearest valid pixel get {@link
   *     Status#NO_PIXEL}
   */
  public IsroFileReader(List<Path> files, StationCatalog stations, double maxPixelDistanceKm) {
    this.files = List.copyOf(files);
    this.stations = stations;
    this.maxPixelDistanceKm = maxPixelDistanceKm;
  }

  /**
   * Lists .h5/.nc files in a folder (recursively), or returns the single file given.
   *
   * @param input a file or folder
   * @return matching files, sorted
   * @throws IOException if the folder cannot be listed
   */
  public static List<Path> findFiles(Path input) throws IOException {
    if (Files.isRegularFile(input)) {
      return List.of(input);
    }
    if (!Files.isDirectory(input)) {
      return List.of();
    }
    try (Stream<Path> walk = Files.walk(input)) {
      return walk.filter(p -> IsroFileName.matches(p.getFileName().toString())).sorted().toList();
    }
  }

  @Override
  public String describe() {
    return files.size() + " ISRO file(s)";
  }

  @Override
  public List<Observation> read() throws IOException {
    List<Observation> out = new ArrayList<>();
    for (Path file : files) {
      out.addAll(readFile(file));
    }
    return out;
  }

  /**
   * Extracts one observation per station from a single file.
   *
   * @param file the HDF5/NetCDF file
   * @return one observation per station
   * @throws IOException if the file cannot be opened or has no recognisable data variable
   */
  public List<Observation> readFile(Path file) throws IOException {
    String fileName = file.getFileName().toString();
    IsroFileName name = IsroFileName.parse(fileName);
    IsroProduct product =
        IsroProduct.byDatasetId(name.datasetId())
            .orElseThrow(() -> new IOException("Unsupported product " + name.datasetId()));
    Instant readAt = Instant.now();

    try (NetcdfDataset ds = NetcdfDatasets.openDataset(file.toString())) {
      Variable data = findDataVariable(ds, product);
      Variable lat = findVariable(ds, List.of("Latitude", "latitude", "lat"));
      Variable lon = findVariable(ds, List.of("Longitude", "longitude", "lon"));
      if (lat == null || lon == null) {
        throw new IOException("No latitude/longitude variables in " + fileName);
      }
      Variable qc = findQualityVariable(ds, data);
      String units = data.attributes().findAttributeString("units", "");
      int[][] pixels = locatePixels(name.datasetId(), data, lat, lon);

      List<Observation> out = new ArrayList<>();
      List<Station> list = stations.all();
      for (int i = 0; i < list.size(); i++) {
        Station s = list.get(i);
        int[] yx = pixels[i];
        if (yx == null) {
          out.add(
              new Observation(
                  s.id(), SOURCE, product.satellite(), product.product(), name.timestamp(),
                  s.latitude(), s.longitude(), null, units, "", Status.NO_PIXEL, fileName, readAt));
          continue;
        }
        double pixLat = readAt(lat, yx);
        double pixLon = readAt(lon, yx);
        double value = readAt(data, yx);
        String flag = qc == null ? "" : formatFlag(readAt(qc, yx));
        boolean missing =
            Double.isNaN(value) || (data instanceof VariableDS vds && vds.isMissing(value));
        out.add(
            new Observation(
                s.id(), SOURCE, product.satellite(), product.product(), name.timestamp(),
                round(pixLat, 4), round(pixLon, 4), missing ? null : value, units, flag,
                missing ? Status.FILL : Status.OK, fileName, readAt));
      }
      return out;
    } catch (InvalidRangeException e) {
      throw new IOException("Could not read pixel from " + fileName, e);
    }
  }

  /** Finds the nearest valid pixel for every station; cached per product and grid size. */
  private int[][] locatePixels(String datasetId, Variable data, Variable lat, Variable lon)
      throws IOException {
    String key = datasetId + Arrays.toString(lat.getShape());
    int[][] cached = pixelCache.get(key);
    if (cached != null) {
      return cached;
    }
    Array latArr = lat.read();
    Array lonArr = lon.read();
    boolean grid1d = lat.getRank() == 1;
    int ny = grid1d ? lat.getShape()[0] : lat.getShape()[0];
    int nx = grid1d ? lon.getShape()[0] : lat.getShape()[1];
    List<Station> list = stations.all();
    int[][] result = new int[list.size()][];
    for (int i = 0; i < list.size(); i++) {
      Station s = list.get(i);
      double best = Double.MAX_VALUE;
      int bestY = -1;
      int bestX = -1;
      double cosLat = Math.cos(Math.toRadians(s.latitude()));
      for (int y = 0; y < ny; y++) {
        for (int x = 0; x < nx; x++) {
          double pLat = grid1d ? latArr.getDouble(y) : latArr.getDouble(y * nx + x);
          double pLon = grid1d ? lonArr.getDouble(x) : lonArr.getDouble(y * nx + x);
          if (Double.isNaN(pLat) || Double.isNaN(pLon) || Math.abs(pLat) > 90) {
            continue;
          }
          double dLat = pLat - s.latitude();
          double dLon = (pLon - s.longitude()) * cosLat;
          double d = dLat * dLat + dLon * dLon;
          if (d < best) {
            best = d;
            bestY = y;
            bestX = x;
          }
        }
      }
      if (bestY >= 0) {
        double pLat = grid1d ? latArr.getDouble(bestY) : latArr.getDouble(bestY * nx + bestX);
        double pLon = grid1d ? lonArr.getDouble(bestX) : lonArr.getDouble(bestY * nx + bestX);
        double km = Geo.distanceKm(s.latitude(), s.longitude(), pLat, pLon);
        result[i] = km <= maxPixelDistanceKm ? new int[] {bestY, bestX} : null;
      }
    }
    pixelCache.put(key, result);
    return result;
  }

  /** Reads one element at (y, x), using index 0 for any leading dimensions (e.g. time). */
  private static double readAt(Variable v, int[] yx) throws IOException, InvalidRangeException {
    int rank = v.getRank();
    int[] origin = new int[rank];
    int[] shape = new int[rank];
    Arrays.fill(shape, 1);
    if (rank == 1) {
      // 1-D coordinate: latitude uses y, longitude uses x
      String n = v.getShortName().toLowerCase(Locale.ROOT);
      origin[0] = n.startsWith("lon") ? yx[1] : yx[0];
    } else {
      origin[rank - 2] = yx[0];
      origin[rank - 1] = yx[1];
    }
    return v.read(origin, shape).getDouble(0);
  }

  private static Variable findDataVariable(NetcdfDataset ds, IsroProduct product)
      throws IOException {
    Variable v = findVariable(ds, product.variableNames());
    if (v != null) {
      return v;
    }
    // Fallback: the largest numeric 2-D+ variable that is not a coordinate.
    Variable best = null;
    for (Variable candidate : ds.getVariables()) {
      String n = candidate.getShortName().toLowerCase(Locale.ROOT);
      if (candidate.getRank() >= 2
          && candidate.getDataType().isNumeric()
          && !COORDINATE_NAMES.contains(n)
          && (best == null || candidate.getSize() > best.getSize())) {
        best = candidate;
      }
    }
    if (best == null) {
      throw new IOException("No data variable found for " + product.product());
    }
    return best;
  }

  private static Variable findVariable(NetcdfDataset ds, List<String> names) {
    for (String name : names) {
      for (Variable v : ds.getVariables()) {
        if (v.getShortName().equalsIgnoreCase(name)) {
          return v;
        }
      }
    }
    return null;
  }

  /** A product quality-flag variable has the same grid as the data and "flag"/"qc" in its name. */
  private static Variable findQualityVariable(NetcdfDataset ds, Variable data) {
    int[] shape = data.getShape();
    for (Variable v : ds.getVariables()) {
      String n = v.getShortName().toLowerCase(Locale.ROOT);
      if ((n.contains("flag") || n.contains("qc") || n.contains("quality"))
          && Arrays.equals(v.getShape(), shape)) {
        return v;
      }
    }
    return null;
  }

  private static String formatFlag(double flag) {
    return Double.isNaN(flag) ? "" : String.valueOf((long) flag);
  }

  private static double round(double v, int places) {
    double f = Math.pow(10, places);
    return Math.round(v * f) / f;
  }
}
