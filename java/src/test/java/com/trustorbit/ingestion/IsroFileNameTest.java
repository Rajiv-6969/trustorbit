package com.trustorbit.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class IsroFileNameTest {

  @Test
  void parsesHalfHourlyLstFileName() {
    IsroFileName n = IsroFileName.parse("3SIMG_01OCT2025_0600_L2B_LST_V01R00.h5");
    assertEquals("3S", n.satelliteCode());
    assertEquals(Instant.parse("2025-10-01T06:00:00Z"), n.timestamp());
    assertEquals("3SIMG_L2B_LST", n.datasetId());
    assertEquals("0600", n.timeSlot());
  }

  @Test
  void parsesDailyRainfallFileName() {
    IsroFileName n = IsroFileName.parse("3RIMG_02OCT2025_0015_L3B_HEM_DLY_V01R00.h5");
    assertEquals("3RIMG_L3B_HEM_DLY", n.datasetId());
    assertEquals(Instant.parse("2025-10-02T00:15:00Z"), n.timestamp());
  }

  @Test
  void rejectsOtherFiles() {
    assertFalse(IsroFileName.matches("notes.txt"));
    assertThrows(IllegalArgumentException.class, () -> IsroFileName.parse("LST.h5"));
  }

  @Test
  void productsKeepOnlyTheirTimeSlot() {
    assertTrue(IsroProduct.LST_3DS.wants(IsroFileName.parse("3SIMG_01OCT2025_0600_L2B_LST_V01R00.h5")));
    assertFalse(IsroProduct.LST_3DS.wants(IsroFileName.parse("3SIMG_01OCT2025_0630_L2B_LST_V01R00.h5")));
    assertTrue(IsroProduct.HEM_3DR.wants(IsroFileName.parse("3RIMG_02OCT2025_0015_L3B_HEM_DLY_V01R00.h5")));
  }
}
