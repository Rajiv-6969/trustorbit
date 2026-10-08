package com.trustorbit.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Uses a real MOSDAC search response (metadata only) saved on 2026-10-08. */
class MosdacClientTest {

  @Test
  void parsesRealSearchResponseAndKeepsOneFilePerDay() throws Exception {
    String body;
    try (InputStream in = getClass().getResourceAsStream("/mosdac/search-3SIMG_L2B_LST-2025-10-01.json")) {
      body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    MosdacClient.SearchResult r = MosdacClient.parseSearch(body);
    assertEquals(45, r.totalResults());
    assertEquals(45, r.entries().size());

    List<String> kept =
        r.entries().stream()
            .map(MosdacClient.Entry::identifier)
            .filter(id -> IsroProduct.LST_3DS.wants(IsroFileName.parse(id)))
            .toList();
    assertEquals(List.of("3SIMG_01OCT2025_0600_L2B_LST_V01R00.h5"), kept);
  }
}
