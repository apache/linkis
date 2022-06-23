package org.apache.linkis.computation.client;

import org.apache.linkis.computation.client.interactive.LogListener;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TestLogListener implements LogListener {

    @Override
    public void onLogUpdate(List<String> logs) {
        String log = logs.get(3);
        if (StringUtils.isNoneBlank(log)) {
            System.out.println(log);
        }
    }
}
