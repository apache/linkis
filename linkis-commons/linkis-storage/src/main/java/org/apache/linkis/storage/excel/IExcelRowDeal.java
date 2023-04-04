package org.apache.linkis.storage.excel;

import org.apache.poi.hssf.record.BoundSheetRecord;

import java.util.List;

interface IExcelRowDeal {
  void dealRow(BoundSheetRecord[] orderedBSRs, int sheetIndex, int curRow, List<String> rowlist);
}
