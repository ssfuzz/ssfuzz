@@ -1,5 +1,5 @@
 /*
- * Copyright (c) 2012, 2019, Oracle and/or its affiliates. All rights reserved.
+ * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
@@ -367,9 +367,7 @@ public static LocalDate ofEpochDay(long epochDay) {
         int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
         yearEst += marchMonth0 / 10;
 
-        // check year now we are certain it is correct
-        int year = YEAR.checkValidIntValue(yearEst);
-        return new LocalDate(year, month, dom);
+        return new LocalDate((int)yearEst, month, dom);
     }
 
     //-----------------------------------------------------------------------