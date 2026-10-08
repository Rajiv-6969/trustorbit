package com.trustorbit.ingestion;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses MOSDAC INSAT file names such as {@code 3SIMG_01OCT2025_0600_L2B_LST_V01R00.h5}.
 *
 * @param satelliteCode {@code 3D}, {@code 3R} or {@code 3S}
 * @param timestamp acquisition start time in UTC
 * @param datasetId e.g. {@code 3SIMG_L2B_LST}
 * @param timeSlot HHmm UTC, e.g. {@code 0600}
 */
public record IsroFileName(
    String satelliteCode, Instant timestamp, String datasetId, String timeSlot) {

  private static final Pattern PATTERN =
      Pattern.compile(
          "^(3[DRS])IMG_(\\d{2}[A-Za-z]{3}\\d{4})_(\\d{4})_(L\\w+?)_V\\d+R\\d+\\.(h5|nc|hdf5)$");

  private static final DateTimeFormatter DATE =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("ddMMMyyyy")
          .toFormatter(Locale.ENGLISH);

  /**
   * @param fileName file name only (no folder)
   * @return the parsed name
   * @throws IllegalArgumentException if the name does not follow the MOSDAC INSAT convention
   */
  public static IsroFileName parse(String fileName) {
    Matcher m = PATTERN.matcher(fileName);
    if (!m.matches()) {
      throw new IllegalArgumentException("Not a MOSDAC INSAT file name: " + fileName);
    }
    LocalDate date = LocalDate.parse(m.group(2), DATE);
    String slot = m.group(3);
    LocalTime time = LocalTime.of(Integer.parseInt(slot.substring(0, 2)), Integer.parseInt(slot.substring(2)));
    Instant instant = date.atTime(time).toInstant(ZoneOffset.UTC);
    return new IsroFileName(m.group(1), instant, m.group(1) + "IMG_" + m.group(4), slot);
  }

  /**
   * @param fileName any file name
   * @return true if it follows the MOSDAC INSAT convention
   */
  public static boolean matches(String fileName) {
    return PATTERN.matcher(fileName).matches();
  }
}
