package org.apache.linkis.common.utils;

import java.io.Closeable;
import java.io.IOException;

public class CloseIoUtils {

    public static void closeAll(Closeable... cs) {
        if (cs != null) {
            for (Closeable c : cs) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
