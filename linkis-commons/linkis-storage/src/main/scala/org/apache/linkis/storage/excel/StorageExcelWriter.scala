/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.storage.excel

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.domain._
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.commons.io.IOUtils
import org.apache.poi.ss.usermodel._
import org.apache.poi.xssf.streaming.{SXSSFCell, SXSSFSheet, SXSSFWorkbook}

import java.io._
import java.math.BigDecimal
import java.util
import java.util.Date

import scala.collection.mutable.ArrayBuffer

class StorageExcelWriter(
    val charset: String,
    val sheetName: String,
    val dateFormat: String,
    val outputStream: OutputStream,
    val autoFormat: Boolean
) extends ExcelFsWriter
    with Logging {

  protected var workBook: SXSSFWorkbook = _
  protected var sheet: SXSSFSheet = _
  protected var format: DataFormat = _
  protected var types: Array[DataType] = _
  protected var rowPoint = 0
  protected var columnCounter = 0
  protected val styles = new util.HashMap[String, CellStyle]()
  protected var isFlush = true
  protected val os = new ByteArrayOutputStream()
  protected var is: ByteArrayInputStream = _

  def init: Unit = {
    workBook = new SXSSFWorkbook()
    sheet = workBook.createSheet(sheetName)
  }

  def getDefaultHeadStyle: CellStyle = {
    val headerFont = workBook.createFont
    headerFont.setBold(true)
    headerFont.setFontHeightInPoints(14.toShort)
    headerFont.setColor(IndexedColors.RED.getIndex)
    val headerCellStyle = workBook.createCellStyle
    headerCellStyle.setFont(headerFont)
    headerCellStyle
  }

  def getWorkBook: Workbook = {
    // 自适应列宽
    sheet.trackAllColumnsForAutoSizing()
    for (elem <- 0 to columnCounter) {
      sheet.autoSizeColumn(elem)
    }
    workBook
  }

  def createCellStyle(dataType: DataType): CellStyle = {
    val style = workBook.createCellStyle()
    format = workBook.createDataFormat()
    dataType.toString match {
      case _ => style.setDataFormat(format.getFormat("@"))
    }
    if (autoFormat) {
      dataType match {
        case StringType => style.setDataFormat(format.getFormat("@"))
        case TinyIntType => style.setDataFormat(format.getFormat("#"))
        case ShortIntType => style.setDataFormat(format.getFormat("#"))
        case IntType => style.setDataFormat(format.getFormat("#"))
        case LongType => style.setDataFormat(format.getFormat("#.##E+00"))
        case BigIntType => style.setDataFormat(format.getFormat("#.##E+00"))
        case FloatType => style.setDataFormat(format.getFormat("#.0000000000"))
        case DoubleType => style.setDataFormat(format.getFormat("#.0000000000"))
        case CharType => style.setDataFormat(format.getFormat("@"))
        case VarcharType => style.setDataFormat(format.getFormat("@"))
        case DateType => style.setDataFormat(format.getFormat("m/d/yy h:mm"))
        case TimestampType => style.setDataFormat(format.getFormat("m/d/yy h:mm"))
        case DecimalType => style.setDataFormat(format.getFormat("#.000000000"))
        case BigDecimalType => style.setDataFormat(format.getFormat("#.000000000"))
        case _ => style.setDataFormat(format.getFormat("@"))
      }
    }
    style
  }

  def getCellStyle(dataType: DataType): CellStyle = {
    val style = styles.get(dataType.typeName)
    if (style == null) {
      val newStyle = createCellStyle(dataType)
      styles.put(dataType.typeName, newStyle)
      newStyle
    } else {
      style
    }
  }

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    init
    val tableHead = sheet.createRow(0)
    val columns = metaData.asInstanceOf[TableMetaData].columns
    val columnType = new ArrayBuffer[DataType]()
    for (elem <- columns) {
      val headCell = tableHead.createCell(columnCounter)
      headCell.setCellValue(elem.columnName)
      headCell.setCellStyle(getDefaultHeadStyle)
      columnType += elem.dataType
      columnCounter += 1
    }
    types = columnType.toArray
    rowPoint += 1
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    // TODO: 是否需要替换null值
    val tableBody = sheet.createRow(rowPoint)
    var colunmPoint = 0
    val excelRecord = record.asInstanceOf[TableRecord].row
    for (elem <- excelRecord) {
      val cell = tableBody.createCell(colunmPoint)
      val dataType = types.apply(colunmPoint)
      if (autoFormat) {
        setCellTypeValue(dataType, elem, cell)
      } else {
        cell.setCellValue(DataType.valueToString(elem))
      }
      cell.setCellStyle(getCellStyle(dataType))
      colunmPoint += 1
    }
    rowPoint += 1
  }

  private def setCellTypeValue(dataType: DataType, elem: Any, cell: SXSSFCell): Unit = {
    if (null == elem) return
    Utils.tryCatch {
      dataType match {
        case StringType => cell.setCellValue(DataType.valueToString(elem))
        case TinyIntType => cell.setCellValue(elem.toString.toInt)
        case ShortIntType => cell.setCellValue(elem.toString.toInt)
        case IntType => cell.setCellValue(elem.toString.toInt)
        case LongType => cell.setCellValue(elem.toString.toLong)
        case BigIntType => cell.setCellValue(elem.toString.toLong)
        case FloatType => cell.setCellValue(elem.toString.toFloat)
        case DoubleType =>
          doubleCheck(elem.toString)
          cell.setCellValue(elem.toString.toDouble)
        case CharType => cell.setCellValue(DataType.valueToString(elem))
        case VarcharType => cell.setCellValue(DataType.valueToString(elem))
        case DateType => cell.setCellValue(getDate(elem))
        case TimestampType => cell.setCellValue(getDate(elem))
        case DecimalType =>
          doubleCheck(DataType.valueToString(elem))
          cell.setCellValue(DataType.valueToString(elem).toDouble)
        case BigDecimalType =>
          doubleCheck(DataType.valueToString(elem))
          cell.setCellValue(DataType.valueToString(elem).toDouble)
        case _ =>
          val value = DataType.valueToString(elem)
          cell.setCellValue(value)
      }
    } { case e: Exception =>
      cell.setCellValue(DataType.valueToString(elem))
    }
  }

  private def getDate(value: Any): Date = {
    if (value.isInstanceOf[Date]) {
      value.asInstanceOf[Date]
    } else {
      throw new NumberFormatException(
        s"Value ${value} with class : ${value.getClass.getName} is not a valid type of Date."
      );
    }
  }

  /**
   * Check whether the double exceeds the number of digits, which will affect the data accuracy
   * @param elemValue
   */
  private def doubleCheck(elemValue: String): Unit = {
    val value = new BigDecimal(elemValue).stripTrailingZeros
    if ((value.precision - value.scale) > 15) {
      throw new NumberFormatException(
        s"Value ${elemValue} error : This data exceeds 15 significant digits."
      );
    }
  }

  override def flush(): Unit = {
    getWorkBook.write(os)
    val content: Array[Byte] = os.toByteArray
    is = new ByteArrayInputStream(content)
    val buffer: Array[Byte] = new Array[Byte](1024)
    var bytesRead: Int = 0
    while (isFlush) {
      bytesRead = is.read(buffer, 0, 1024)
      if (bytesRead == -1) {
        isFlush = false
      } else {
        outputStream.write(buffer, 0, bytesRead)
      }
    }
  }

  override def close(): Unit = {
    if (isFlush) flush()
    IOUtils.closeQuietly(outputStream)
    IOUtils.closeQuietly(is)
    IOUtils.closeQuietly(os)
    IOUtils.closeQuietly(workBook)
  }

}
