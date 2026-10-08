package com.trustorbit.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal CSV helper (RFC 4180 quoting) so the project needs no extra CSV library.
 */
public final class Csv {

  private Csv() {}

  /**
   * Splits one CSV line into fields, honouring double quotes.
   *
   * @param line a CSV line
   * @return the fields
   */
  public static List<String> parseLine(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (quoted) {
        if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i++;
        } else if (c == '"') {
          quoted = false;
        } else {
          current.append(c);
        }
      } else if (c == '"') {
        quoted = true;
      } else if (c == ',') {
        fields.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }
    fields.add(current.toString());
    return fields;
  }

  /**
   * Parses lines where the first line is a header.
   *
   * @param lines all lines of the file
   * @return one map per data row, keyed by header name
   */
  public static List<Map<String, String>> readWithHeader(List<String> lines) {
    List<Map<String, String>> rows = new ArrayList<>();
    if (lines.isEmpty()) {
      return rows;
    }
    List<String> header = parseLine(stripBom(lines.getFirst()));
    for (String line : lines.subList(1, lines.size())) {
      if (line.isBlank()) {
        continue;
      }
      List<String> values = parseLine(line);
      Map<String, String> row = new LinkedHashMap<>();
      for (int i = 0; i < header.size(); i++) {
        row.put(header.get(i).strip(), i < values.size() ? values.get(i).strip() : "");
      }
      rows.add(row);
    }
    return rows;
  }

  /**
   * Joins fields into one CSV line, quoting where needed.
   *
   * @param fields values (null becomes empty)
   * @return the CSV line without a newline
   */
  public static String formatLine(List<String> fields) {
    List<String> out = new ArrayList<>(fields.size());
    for (String f : fields) {
      String v = f == null ? "" : f;
      if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
        v = "\"" + v.replace("\"", "\"\"") + "\"";
      }
      out.add(v);
    }
    return String.join(",", out);
  }

  private static String stripBom(String s) {
    return s.startsWith("\uFEFF") ? s.substring(1) : s;
  }
}
