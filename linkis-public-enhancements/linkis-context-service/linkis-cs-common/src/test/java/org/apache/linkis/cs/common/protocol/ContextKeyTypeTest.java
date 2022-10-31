package org.apache.linkis.cs.common.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextKeyTypeTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    int ymlIndex = ContextKeyType.YML_CONTEXT_KEY.getIndex();
    String ymlTypeName = ContextKeyType.YML_CONTEXT_KEY.getTypeName();

    Assertions.assertTrue(1 == ymlIndex);
    Assertions.assertEquals(
        "org.apache.linkis.cs.common.entity.source.CommonContextKey", ymlTypeName);
  }
}
