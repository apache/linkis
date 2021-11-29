/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.storage.excel

import java.io._
import java.util

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.storage.domain.{BigIntType, DataType, IntType, LongType, ShortIntType, TinyIntType}
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.apache.poi.ss.usermodel._
import org.apache.poi.xssf.streaming.{SXSSFSheet, SXSSFWorkbook}

import scala.collection.mutable.ArrayBuffer


class StorageExcelWriter(val charset: String, val sheetName: String, val dateFormat: String, val outputStream: OutputStream) extends ExcelFsWriter {

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

  def init = {
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
    //自适应列宽
    sheet.trackAllColumnsForAutoSizing()
    for (elem <- 0 to columnCounter) {
      sheet.autoSizeColumn(elem)
    }
    workBook
  }

  def createCellStyle(dataType: DataType): CellStyle = {
    val style = workBook.createCellStyle()
    format = workBook.createDataFormat()
    dataType match {
      case BigIntType | TinyIntType | ShortIntType | IntType | LongType  => style.setDataFormat(format.getFormat("0"))
      case _ => style.setDataFormat(format.getFormat("@"))
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
      dataType match {
        case BigIntType | TinyIntType | ShortIntType | IntType | LongType => cell.setCellValue(if (elem.toString.equals("NULL")) 0 else elem.toString.toDouble)
        case _ => cell.setCellValue(elem.toString) //read时候进行null替换等等
      }
      cell.setCellStyle(getCellStyle(dataType))
      colunmPoint += 1
    }
    rowPoint += 1
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


