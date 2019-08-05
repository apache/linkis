package com.webank.wedatasphere.spark.excel

import java.io.{BufferedOutputStream, OutputStream}
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.ss.usermodel._
import org.apache.spark.sql.DataFrame

import scala.language.postfixOps

object ExcelFileSaver {
  final val DEFAULT_SHEET_NAME = "Sheet1"
  final val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
  final val DEFAULT_TIMESTAMP_FORMAT = "yyyy-mm-dd hh:mm:ss.000"
}

class ExcelFileSaver(fs: FileSystem) {

  import ExcelFileSaver._

  def save(
            location: Path,
            dataFrame: DataFrame,
            sheetName: String = DEFAULT_SHEET_NAME,
            useHeader: Boolean = true,
            dateFormat: String = DEFAULT_DATE_FORMAT,
            timestampFormat: String = DEFAULT_TIMESTAMP_FORMAT
          ): Unit = {
    fs.setVerifyChecksum(false)
    val headerRow = dataFrame.schema.map(_.name)
    val dataRows = dataFrame.toLocalIterator()
    val excelWriter = new ExcelWriter(sheetName, dateFormat, timestampFormat)
    if (useHeader) excelWriter.writeHead(headerRow)
    while (dataRows.hasNext) {
      val line = dataRows.next().toSeq
      excelWriter.writerRow(line)
    }
    excelWriter.close(new BufferedOutputStream(fs.create(location)))
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

  def writerRow(line: Seq[Any]): Unit = {
    val row = sheet.createRow(rowNum)
    for (i <- line.indices) {
      createCell(row, i, line(i))
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

  def createCell(row: Row, col: Int, value: Any): Unit = {
    val cell = row.createCell(col)
    value match {
      case t: java.sql.Timestamp => setTimestampValue(cell, new Date(t.getTime))
      case d: java.sql.Date => setDateValue(cell, new Date(d.getTime))
      case s: String => cell.setCellValue(s)
      case f: Float => cell.setCellValue(f)
      case d: Double => cell.setCellValue(d)
      case b: Boolean => cell.setCellValue(b)
      case b: Byte => cell.setCellValue(b)
      case s: Short => cell.setCellValue(s)
      case i: Int => cell.setCellValue(i)
      case l: Long => cell.setCellValue(l)
      case b: BigDecimal => cell.setCellValue(b.doubleValue())
      case b: java.math.BigDecimal => cell.setCellValue(b.doubleValue())
      case null => cell.setCellValue("NULL")
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
