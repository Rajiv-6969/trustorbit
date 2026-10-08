package com.trustorbit.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.trustorbit.model.Observation;
import com.trustorbit.model.Observation.Status;
import com.trustorbit.model.Station;
import com.trustorbit.model.StationCatalog;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.write.NetcdfFormatWriter;

/**
 * Tests the reader on a tiny SYNTHETIC NetCDF file built inside the test (3 x 4 pixels). It mimics
 * the layout of an INSAT L2B file (2-D Latitude/Longitude, scaled 16-bit LST with a fill value) but
 * contains made-up numbers and is never used as data.
 */
class IsroFileReaderTest {

  private static final short FILL = -999;

  @Test
  void extractsNearestPixelAppliesScaleAndMarksFillAsMissing(@TempDir Path dir) throws Exception {
    Path file = dir.resolve("3SIMG_01OCT2025_0600_L2B_LST_V01R00.nc");
    writeSyntheticFile(file);
    StationCatalog stations =
        new StationCatalog(
            List.of(
                new Station("AAA", "Pixel 0,0", "-", 12.0, 77.0),
                new Station("BBB", "Fill pixel", "-", 12.1, 77.1),
                new Station("FAR", "Far away", "-", 30.0, 90.0)));

    List<Observation> obs = new IsroFileReader(List.of(file), stations, 10).read();

    assertEquals(3, obs.size());
    Observation a = obs.get(0);
    assertEquals(Status.OK, a.status());
    assertEquals(300.0, a.value(), 1e-4); // stored 30000 x scale 0.01
    assertEquals("K", a.units());
    assertEquals("INSAT-3DS", a.satellite());
    assertEquals("LST", a.product());
    assertEquals(Instant.parse("2025-10-01T06:00:00Z"), a.timestamp());

    Observation b = obs.get(1);
    assertEquals(Status.FILL, b.status());
    assertNull(b.value());

    assertEquals(Status.NO_PIXEL, obs.get(2).status());
  }

  private static void writeSyntheticFile(Path file) throws Exception {
    NetcdfFormatWriter.Builder b = NetcdfFormatWriter.createNewNetcdf3(file.toString());
    b.addDimension("y", 3);
    b.addDimension("x", 4);
    b.addVariable("Latitude", DataType.FLOAT, "y x");
    b.addVariable("Longitude", DataType.FLOAT, "y x");
    b.addVariable("LST", DataType.SHORT, "y x")
        .addAttribute(new Attribute("scale_factor", 0.01f))
        .addAttribute(new Attribute("_FillValue", FILL))
        .addAttribute(new Attribute("units", "K"));
    float[] lat = new float[12];
    float[] lon = new float[12];
    short[] lst = new short[12];
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 4; x++) {
        int i = y * 4 + x;
        lat[i] = 12.0f + 0.1f * y;
        lon[i] = 77.0f + 0.1f * x;
        lst[i] = (short) (30000 + i);
      }
    }
    lst[1 * 4 + 1] = FILL; // pixel nearest to station BBB
    try (NetcdfFormatWriter w = b.build()) {
      w.write("Latitude", Array.factory(DataType.FLOAT, new int[] {3, 4}, lat));
      w.write("Longitude", Array.factory(DataType.FLOAT, new int[] {3, 4}, lon));
      w.write("LST", Array.factory(DataType.SHORT, new int[] {3, 4}, lst));
    }
  }
}
