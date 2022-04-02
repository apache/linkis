package org.apache.linkis.entrance;

import org.apache.linkis.entrance.utils.FormattedTimeOperationPlaceholders;

import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Author: duhanmin
 * Description:
 * Date: 2022/4/2 17:29
 */
public class FormattedTimeOperationPlaceholdersTest {

    private static final Date date = new Date(1648892107169L);
    private static final ZonedDateTime zonedDateTime = FormattedTimeOperationPlaceholders.toZonedDateTime(date);

    @org.junit.Test
    public void testJsonFormat() {
        String jsonOld = "{\"name\":\"${yyyyMMdd%-1d}\",\"address\":{\"street\":\"${yyyyMMdd%-1y}\"},\"links\":[{\"name\":\"${yyyyMMdd%-1M}\"}]}";
        String jsonNew = FormattedTimeOperationPlaceholders.replaces(zonedDateTime, jsonOld);
        System.out.println(jsonOld + "\n" + jsonNew);
        assertEquals(jsonNew,"{\"name\":\"\\\"20220401\\\"\",\"address\":{\"street\":\"\\\"20210402\\\"\"},\"links\":[{\"name\":\"\\\"20220302\\\"\"}]}");
    }
    @org.junit.Test
    public void testTextFormat() {
        String strOld = "abc${yyyyMMdd%-1d}def";
        String strNew = FormattedTimeOperationPlaceholders.replaces(ZonedDateTime.now(), strOld);
        System.out.println(strOld + "\n" + strNew);
       assertEquals(strNew,"abc20220401def");
    }
}
