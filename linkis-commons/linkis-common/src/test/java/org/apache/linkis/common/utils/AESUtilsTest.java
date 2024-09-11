package org.apache.linkis.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AESUtilsTest {

    @Test
    void byteStringAsMb() {
        String origin = "y13120886823";
        String key = "1234asdf";
        String encrypt = AESUtils.encrypt(origin, key);
        String decrypt = AESUtils.decrypt(encrypt, key);
        Assertions.assertEquals("VOzW1qzc0EoU7Gu4uUxG0Q==", encrypt);
        Assertions.assertEquals(origin, decrypt);
    }
}
