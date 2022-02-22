package org.apache.linkis.jobhistory.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JobHistory Tester
 */
public class JobHistoryTest {

    @Test
    @DisplayName("Method description: ...")
    public void testGetUpdateTimeMills() throws Exception {
        long time = 1643738522000L;//2022-02-02 02:02:02.000
        String timestr = "2022-02-02 02:02:02.000";

        JobHistory jobHistory = new JobHistory();
        Date date = new Date(time);
        jobHistory.setCreated_time(date);
        jobHistory.setUpdated_time(date);
        assertEquals(timestr, jobHistory.getUpdateTimeMills());
    }


} 
