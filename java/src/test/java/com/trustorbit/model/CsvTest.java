package com.trustorbit.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvTest {

  @Test
  void roundTripsQuotesAndCommas() {
    List<String> fields = Arrays.asList("BLR", "a,b", "say \"hi\"", "", null);
    String line = Csv.formatLine(fields);
    assertEquals("BLR,\"a,b\",\"say \"\"hi\"\"\",,", line);
    assertEquals(List.of("BLR", "a,b", "say \"hi\"", "", ""), Csv.parseLine(line));
  }

  @Test
  void readsRowsByHeaderName() {
    var rows = Csv.readWithHeader(List.of("\uFEFFid,value", "BLR,1.5", "", "MAA"));
    assertEquals(2, rows.size());
    assertEquals("1.5", rows.get(0).get("value"));
    assertEquals("", rows.get(1).get("value"));
  }
}
