@@ -1,5 +1,5 @@
 /*
- * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
+ * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
@@ -63,9 +63,12 @@
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertSame;
 import static org.testng.Assert.assertTrue;
+import static org.testng.Assert.fail;
 
+import java.time.DateTimeException;
 import java.time.LocalDate;
 import java.time.Month;
+import java.time.temporal.ChronoField;
 import java.time.temporal.ChronoUnit;
 import java.time.temporal.IsoFields;
 
@@ -420,6 +423,37 @@ public void test_toEpochDay_fromMJDays_symmetry() {
         }
     }
 
+    @Test
+    public void test_ofEpochDay_edges() {
+        long minDay = ChronoField.EPOCH_DAY.range().getMinimum();
+        long maxDay = ChronoField.EPOCH_DAY.range().getMaximum();
+        long minYear = ChronoField.YEAR.range().getMinimum();
+        long maxYear = ChronoField.YEAR.range().getMinimum();
+        int[] offsets = new int[] { 0, 1, 2, 3, 28, 29, 30, 31, 32, 363, 364, 365, 366, 367 };
+        for (int offset : offsets) {
+            LocalDate minDate = LocalDate.ofEpochDay(minDay + offset);
+            assertEquals(minDate, LocalDate.MIN.plusDays(offset));
+            assertTrue(ChronoField.YEAR.range().isValidValue(minDate.getYear()));
+
+            LocalDate maxDate = LocalDate.ofEpochDay(maxDay - offset);
+            assertEquals(maxDate, LocalDate.MAX.minusDays(offset));
+            assertTrue(ChronoField.YEAR.range().isValidValue(maxDate.getYear()));
+
+            try {
+                LocalDate.ofEpochDay(minDay - 1 - offset);
+                fail("Expected DateTimeException");
+            } catch (DateTimeException e) {
+                // expected
+            }
+            try {
+                LocalDate.ofEpochDay(maxDay + 1 + offset);
+                fail("Expected DateTimeException");
+            } catch (DateTimeException e) {
+                // expected
+            }
+        }
+    }
+
     void doTest_comparisons_LocalDate(LocalDate... localDates) {
         for (int i = 0; i < localDates.length; i++) {
             LocalDate a = localDates[i];