/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.storage.excel

import java.io._
import java.util

import com.webank.wedatasphere.linkis.common.io.{MetaData, Record}
import com.webank.wedatasphere.linkis.storage.domain.DataType
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.apache.poi.ss.usermodel._
import org.apache.poi.xssf.streaming.{SXSSFSheet, SXSSFWorkbook}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by johnnwang on 2018/11/12.
  */
class StorageExcelWriter(val charset: String, val sheetName: String, val dateFormat: String, val outputStream: OutputStream) extends ExcelFsWriter {

  private var workBook: SXSSFWorkbook = _
  private var sheet: SXSSFSheet = _
  private var format: DataFormat = _
  private var types: Array[DataType] = _
  private var rowPoint = 0
  private var columnCounter = 0
  private val styles = new util.HashMap[String, CellStyle]()
  private var isFlush = true
  private val os = new ByteArrayOutputStream()
  private var is: ByteArrayInputStream = _

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
    dataType.toString match {
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
      cell.setCellValue(elem.toString) //read时候进行null替换等等
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


