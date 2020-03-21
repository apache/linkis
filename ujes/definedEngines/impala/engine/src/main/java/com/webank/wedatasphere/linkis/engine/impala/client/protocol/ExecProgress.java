package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

/**
 * 进度信息
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class ExecProgress {
    private long totalScanRanges;
    private long completedScanRanges;
    
    /**
     * @param totalScanRanges
     * @param completedScanRanges
     */
    public ExecProgress(long totalScanRanges, long completedScanRanges) {
        super();
        this.totalScanRanges = totalScanRanges;
        this.completedScanRanges = completedScanRanges;
    }
    
    public long getTotalScanRanges() {
        return totalScanRanges;
    }
    public void setTotalScanRanges(long totalScanRanges) {
        this.totalScanRanges = totalScanRanges;
    }
    public long getCompletedScanRanges() {
        return completedScanRanges;
    }
    public void setCompletedScanRanges(long completedScanRanges) {
        this.completedScanRanges = completedScanRanges;
    }
    
}
