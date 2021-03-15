/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.spark.excel

import java.io.{BufferedOutputStream, OutputStream}
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.ss.usermodel.{IndexedColors, _}
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.spark.sql.DataFrame

import scala.language.postfixOps

object ExcelFileSaver {
  final val DEFAULT_SHEET_NAME = "Sheet1"
  final val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
  final val DEFAULT_TIMESTAMP_FORMAT = "yyyy-mm-dd hh:mm:ss.000"
  final val DEFAULT_EXPORT_NULL_VALUE = "SHUFFLEOFF"
}

class ExcelFileSaver(fs: FileSystem) {

  import ExcelFileSaver._

  def save(
            location: Path,
            dataFrame: DataFrame,
            sheetName: String = DEFAULT_SHEET_NAME,
            useHeader: Boolean = true,
            dateFormat: String = DEFAULT_DATE_FORMAT,
            timestampFormat: String = DEFAULT_TIMESTAMP_FORMAT,
            exportNullValue:String = DEFAULT_EXPORT_NULL_VALUE
          ): Unit = {
    fs.setVerifyChecksum(false)
    val headerRow = dataFrame.schema.map(_.name)
    val dataRows = dataFrame.toLocalIterator()
    val excelWriter = new ExcelWriter(sheetName, dateFormat, timestampFormat)
    if (useHeader) excelWriter.writeHead(headerRow)
    while (dataRows.hasNext) {
      val line = dataRows.next().toSeq
      excelWriter.writerRow(line,exportNullValue)
    }
    // if failed try to refresh nfs cache
    val out: OutputStream =
    try{
      fs.create(location)
    } catch {
      case e:Throwable =>
        fs.listFiles(location.getParent, false)
        fs.create(location)
      case _ =>
        fs.listFiles(location.getParent, false)
        fs.create(location)
    }
    excelWriter.close(new BufferedOutputStream(out))
  }

  def autoClose[A <: AutoCloseable, B](closeable: A)(fun: (A) => B): B = {
    try {
      fun(closeable)
    } finally {
      closeable.close()
    }
  }


}

class ExcelWriter(sheetName: String, dateFormat: String, timestampFormat: String) {


  val wb = new SXSSFWorkbook(100)
  val sheet = wb.createSheet(sheetName)
  val df = new SimpleDateFormat(dateFormat)
  val tf = new SimpleDateFormat(timestampFormat)
  val createHelper = wb.getCreationHelper
  val sdf = wb.createCellStyle()
  sdf.setDataFormat(createHelper.createDataFormat().getFormat(dateFormat))
  val stf = wb.createCellStyle()
  stf.setDataFormat(createHelper.createDataFormat().getFormat(timestampFormat))

  var rowNum = 0
  var columnsLen = 0

  def writeHead(headRow: Seq[String]): Unit = {
    columnsLen = headRow.length
    //设置header的格式
    val headerFont = wb.createFont()
    headerFont.setBold(true)
    headerFont.setFontHeightInPoints(14)
    headerFont.setColor(IndexedColors.RED.getIndex())
    val headerCellStyle = wb.createCellStyle()
    headerCellStyle.setFont(headerFont)
    val row = sheet.createRow(rowNum)
    for (i <- headRow.indices) {
      createCell(row, i, headRow(i), headerCellStyle)
    }
    rowNum = rowNum + 1
  }

  def writerRow(line: Seq[Any],exportNullValue:String): Unit = {
    val row = sheet.createRow(rowNum)
    for (i <- line.indices) {
      createCell(row, i, line(i),exportNullValue)
    }
    rowNum = rowNum + 1
  }

  def createCell(row: Row, col: Int, value: String, cellStyle: CellStyle): Unit = {
    val cell = row.createCell(col)
    cell.setCellValue(value)
    cell.setCellStyle(cellStyle)
  }

  def setDateValue(cell:Cell, date: Date): Unit ={
    cell.setCellStyle(sdf)
    cell.setCellValue(date)
  }
  def setTimestampValue(cell:Cell, date: Date): Unit ={
    cell.setCellStyle(stf)
    cell.setCellValue(date)
  }

  def createCell(row: Row, col: Int, value: Any,exportNullValue:String): Unit = {
    val cell = row.createCell(col)
    value match {
      case t: java.sql.Timestamp => setTimestampValue(cell, new Date(t.getTime))
      case d: java.sql.Date => setDateValue(cell, new Date(d.getTime))
      case s: String => {
        if(("NULL".equals(s) || "".equals(s)) && !"SHUFFLEOFF".equals(exportNullValue))
          cell.setCellValue(exportNullValue)
        else
          cell.setCellValue(s)
      }
      case f: Float => cell.setCellValue(f)
      case d: Double => cell.setCellValue(d)
      case b: Boolean => cell.setCellValue(b)
      case b: Byte => cell.setCellValue(b)
      case s: Short => cell.setCellValue(s)
      case i: Int => cell.setCellValue(i)
      case l: Long => cell.setCellValue(l)
      case b: BigDecimal => cell.setCellValue(b.doubleValue())
      case b: java.math.BigDecimal => cell.setCellValue(b.doubleValue())
      case null => if("SHUFFLEOFF".equals(exportNullValue)) cell.setCellValue("NULL") else cell.setCellValue(exportNullValue)
      case _ => cell.setCellValue(value.toString)
    }
  }

  def close(outputStream: OutputStream): Unit = {
    try {
      sheet.trackAllColumnsForAutoSizing()
      for (i <- 0 until  columnsLen) {
        sheet.autoSizeColumn(i)
      }
      wb.write(outputStream);
    } catch {
      case e: Throwable =>
        throw e
    } finally {
      outputStream.close()
      wb.close()
    }
  }
}
