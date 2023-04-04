package org.apache.linkis.storage.excel;

import org.apache.poi.hssf.record.BoundSheetRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

class RowToCsvDeal implements IExcelRowDeal {

  private Map<String, Object> params;
  private List<String> sheetNames;
  private OutputStream outputStream;
  private Boolean hasHeader;
  private Boolean fisrtRow = true;

  public void init(Boolean hasHeader, List<String> sheetNames, OutputStream outputStream) {
    this.hasHeader = hasHeader;
    this.sheetNames = sheetNames;
    this.outputStream = outputStream;
  }

  @Override
  public void dealRow(
          BoundSheetRecord[] orderedBSRs, int sheetIndex, int curRow, List<String> rowlist) {
    String sheetName = orderedBSRs[sheetIndex].getSheetname();
    if (sheetNames == null || sheetNames.isEmpty() || sheetNames.contains(sheetName)) {
      if (!(curRow == 0 && hasHeader)) {
        try {
          if (fisrtRow) {
            fisrtRow = false;
          } else {
            outputStream.write("\n".getBytes());
          }
          int len = rowlist.size();
          for (int i = 0; i < len; i++) {
            outputStream.write(rowlist.get(i).replaceAll("\n|\t", " ").getBytes("utf-8"));
            if (i < len - 1) {
              outputStream.write("\t".getBytes());
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
