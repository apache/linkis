package org.apache.linkis.storage.excel;

import org.apache.poi.hssf.record.BoundSheetRecord;

import java.util.ArrayList;
import java.util.List;

class FirstRowDeal implements IExcelRowDeal {

  private List<String> sheetNames = new ArrayList<>();
  private List<String> row;

  public List<String> getSheetNames() {
    return sheetNames;
  }

  public void setSheetNames(List<String> sheetNames) {
    this.sheetNames = sheetNames;
  }

  public List<String> getRow() {
    return row;
  }

  public void setRow(List<String> row) {
    this.row = row;
  }

  @Override
  public void dealRow(
          BoundSheetRecord[] orderedBSRs, int sheetIndex, int curRow, List<String> rowlist) {
    for (BoundSheetRecord record : orderedBSRs) {
      sheetNames.add(record.getSheetname());
    }
    row = rowlist;
    throw new ExcelAnalysisException("Finished to deal first row");
  }
}
