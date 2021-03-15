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

import java.math.BigDecimal
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

import com.monitorjbl.xlsx.StreamingReader
import org.apache.commons.lang.StringUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.ss.usermodel.{Cell, CellType, DataFormatter, DateUtil, Sheet, Workbook, WorkbookFactory, Row => SheetRow}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class ExcelRelation(
  location: String,
  sheetName: Option[String],
  useHeader: Boolean,
  treatEmptyValuesAsNulls: Boolean,
  inferSheetSchema: Boolean,
  addColorColumns: Boolean = true,
  userSchema: Option[StructType] = None,
  startColumn: Int = 0,
  endColumn: Int = Int.MaxValue,
  timestampFormat: Option[String] = None,
  maxRowsInMemory: Option[Int] = None,
  excerptSize: Int = 10,
  parameters: Map[String, String] = null,dateFormat: List[String],indexes:Array[Int]
)(@transient val sqlContext: SQLContext)
    extends BaseRelation
    with TableScan
    with PrunedScan{

  private val path = new Path(location)

  def extractCells(row: org.apache.poi.ss.usermodel.Row): Vector[Option[Cell]] =
    row.eachCellIterator(startColumn, endColumn).to[Vector]

  private def openWorkbook(): Workbook = {

    val fs = FileSystem.get(path.toUri, sqlContext.sparkContext.hadoopConfiguration)
    // if failed try to refresh nfs cache
    val inputStream =
    try{
      fs.open(path)
    } catch {
      case e:Throwable =>
        fs.listFiles(path.getParent, false)
        fs.open(path)
      case _ =>
        fs.listFiles(path.getParent, false)
        fs.open(path)
    }

    maxRowsInMemory
      .map { maxRowsInMem =>
        StreamingReader
          .builder()
          .rowCacheSize(maxRowsInMem)
          .bufferSize(maxRowsInMem * 1024)
          .open(inputStream)
      }
      .getOrElse(WorkbookFactory.create(inputStream))
  }

  /**
    * 抽取10row 用于自动推断
    * @return
    */
  private def getExcerpt(): (SheetRow, List[SheetRow]) = {
    val workbook = openWorkbook()
    val sheet = findSheet(workbook, sheetName)
    val sheetIterator = sheet.iterator.asScala
    var currentRow: org.apache.poi.ss.usermodel.Row = null
    while (sheetIterator.hasNext && currentRow == null) {
      currentRow = sheetIterator.next
    }
    if (currentRow == null) {
      workbook.close()
      throw new RuntimeException(s"Sheet $sheetName in $path doesn't seem to contain any data")
    }
    val firstRow = currentRow
    var counter = 0
    val excerpt = new Array[org.apache.poi.ss.usermodel.Row](excerptSize)
    while (sheetIterator.hasNext && counter < excerptSize) {
      excerpt(counter) = sheetIterator.next
      counter += 1
    }
    workbook.close()
    (firstRow, excerpt.take(counter).to[List])
  }

  /**
    * 删掉已经自动推断的行
    * @param wb
    * @param excerptSize
    * @return
    */
  private def restIterator(wb: Workbook, excerptSize: Int): Iterator[org.apache.poi.ss.usermodel.Row] = {
    val sheet = findSheet(wb, sheetName)
    val i = sheet.iterator.asScala
    i.drop(excerptSize + 1)
    i
  }

  /**
    * 返回推断后的全部数据
    * @param workbook
    * @param firstRowWithData
    * @param excerpt
    * @return
    */
  private def dataIterator(workbook: Workbook, firstRowWithData: SheetRow, excerpt: List[SheetRow]) = {
    val init = if (useHeader) excerpt else firstRowWithData :: excerpt
    init.iterator ++ restIterator(workbook, excerpt.size)
  }

  private def getFirstRow(wb:Workbook):org.apache.poi.ss.usermodel.Row ={
    val names = sheetName.getOrElse("sheet1")
    var  sheet:Sheet = null
    if("sheet1".equals(names) || ! names.contains(',')){
      sheet = findSheet(wb, sheetName)
    } else {
      val sheetName = names.split(',')(0)
      sheet = findSheet(wb,Some(sheetName))
    }
    val i = sheet.iterator.asScala
    if(i.hasNext)i.next() else null
  }


  /**
    * 遍历所有sheet
    * @param wb
    * @return
    */
  private def dataAllIterator(wb: Workbook): Iterator[org.apache.poi.ss.usermodel.Row] = {
    val names = sheetName.getOrElse("sheet1")
    if("sheet1".equals(names) || ! names.contains(',')){
      val sheet = findSheet(wb, sheetName)
      val i = sheet.iterator.asScala
      if (useHeader) i.drop(1)
      i
    } else {
      val sheetNames = names.split(',')
      var resIterator  =  Iterator[org.apache.poi.ss.usermodel.Row]()
      for(name <- sheetNames){
        val sheet = findSheet(wb, Some(name))
        val i = sheet.iterator.asScala
        if (useHeader) i.drop(1)
        resIterator = resIterator ++ i
      }
      resIterator
    }
  }

  override val schema: StructType = inferSchema

  /**
    * 数据格式转换
    */
  val dataFormatter = new DataFormatter()

  //val dateFormat: SimpleDateFormat = new SimpleDateFormat(parameters.getOrElse( "dateFormat", "yyyy-MM-dd"), Locale.US)

  val nullValue =parameters.getOrElse( "nullValue", " ")
  val nanValue = parameters.getOrElse( "nanValue", "null")
  val quote = parameters.getOrElse( "quote", "\"")
  val escape = parameters.getOrElse( "escape", "\\")
  val escapeQuotes = parameters.getOrElse("escapeQuotes", false)

  val timestampParser = if (timestampFormat.isDefined) {
    Some(new SimpleDateFormat(timestampFormat.get))
  } else {
    None
  }


  /**
    * 获得对应的sheet
    * @param workBook
    * @param sheetName
    * @return
    */
  private def findSheet(workBook: Workbook, sheetName: Option[String]): Sheet = {
    sheetName
      .map { sn =>
        Option(workBook.getSheet(sn)).getOrElse(throw new IllegalArgumentException(s"Unknown sheet $sn"))
      }
      .getOrElse(workBook.sheetIterator.next)
  }

  /**
    * build Row
    * @return
    */
  override def buildScan: RDD[Row] = buildScan(schema.map(_.name).toArray)

  override def buildScan(requiredColumns: Array[String]): RDD[Row] = {
    val lookups = requiredColumns
      .map { c =>
        val columnNameRegex = s"(.*?)(_color)?".r
        val columnNameRegex(columnName, isColor) = c
        val columnIndex = schema.indexWhere(_.name == columnName)

        val cellExtractor: Cell => Any = if (isColor == null) {
          castTo(_, schema(columnIndex).dataType,new SimpleDateFormat(dateFormat(columnIndex),Locale.US),null)
        } else {
          _.getCellStyle.getFillForegroundColorColor match {
            case null => ""
            case c: org.apache.poi.xssf.usermodel.XSSFColor => c.getARGBHex
            case c => throw new RuntimeException(s"Unknown color type $c: ${c.getClass}")
          }
        }
      { row: SheetRow =>
        val index = indexes(columnIndex)
        val cell = row.getCell(index + startColumn)
        if (cell == null) {
          null
        } else {
          cellExtractor(cell)
        }
      }
      }
      .to[Vector]
    val workbook = openWorkbook()
    val rows = dataAllIterator(workbook).map(row => lookups.map(l => l(row)))
    val result = rows.to[Vector]
    val rdd = sqlContext.sparkContext.parallelize(result.map(Row.fromSeq))
    workbook.close()
    rdd
  }

  private def stringToDouble(value: String): Double = {
    Try(value.toDouble) match {
      case Success(d) => d
      case Failure(_) => Double.NaN
    }
  }

  private def castTo(cell: Cell, castType: DataType,dateFormat: SimpleDateFormat,timestampFormat:SimpleDateFormat): Any = {
    val cellType = cell.getCellTypeEnum
    if (cellType == CellType.BLANK) {
      return null
    }

    lazy val dataFormatter = new DataFormatter()
    lazy val stringValue =
      cell.getCellTypeEnum match {
        case CellType.FORMULA =>
          cell.getCachedFormulaResultTypeEnum match {
            case CellType.STRING => cell.getRichStringCellValue.getString
            case CellType.NUMERIC => cell.getNumericCellValue.toString
            case _ => dataFormatter.formatCellValue(cell)
          }
        case CellType.ERROR =>"" //logInfo(s"解析excel遇到错误公式数据${cell.getStringCellValue},置为空值")
        case _ => dataFormatter.formatCellValue(cell)
      }
    lazy val numericValue =
      cell.getCellTypeEnum match {
        case CellType.NUMERIC => cell.getNumericCellValue
        case CellType.STRING => stringToDouble(cell.getStringCellValue)
        case CellType.FORMULA =>
          cell.getCachedFormulaResultTypeEnum match {
            case CellType.NUMERIC => cell.getNumericCellValue
            case CellType.STRING => stringToDouble(cell.getRichStringCellValue.getString)
          }
      }
    lazy val bigDecimal = new BigDecimal(numericValue)
    castType match {
      case _: ByteType => numericValue.toByte
      case _: ShortType => numericValue.toShort
      case _: IntegerType => numericValue.toInt
      case _: LongType => numericValue.toLong
      case _: FloatType => numericValue.toFloat
      case _: DoubleType => numericValue
      case _: BooleanType => cell.getBooleanCellValue
      case _: DecimalType => if (cellType == CellType.STRING && (cell.getStringCellValue == "" || "null".equalsIgnoreCase(cell.getStringCellValue))) null else bigDecimal
      case _: TimestampType =>
        cellType match {
          case CellType.NUMERIC => new Timestamp(DateUtil.getJavaDate(numericValue).getTime)
          case _ => parseTimestamp(stringValue)
        }
      case _: DateType =>
        if("null".equalsIgnoreCase(cell.getStringCellValue)||StringUtils.isEmpty(cell.getStringCellValue)){
          null
        }else{
          new java.sql.Date(Try(dateFormat.parse(cell.getStringCellValue).getTime).getOrElse(DateUtil.getJavaDate(numericValue).getTime))}

      case _: StringType => stringValue.replaceAll("\n|\t", " ")
      case t => throw new RuntimeException(s"Unsupported cast from $cell to $t")
    }
  }

  private def parseTimestamp(stringValue: String): Timestamp = {
    timestampParser match {
      case Some(parser) => new Timestamp(parser.parse(stringValue).getTime)
      case None => Timestamp.valueOf(stringValue)
    }
  }

  private def getSparkType(cell: Option[Cell]): DataType = {
    cell match {
      case Some(c) =>
        c.getCellTypeEnum match {
          case CellType.FORMULA =>
            c.getCachedFormulaResultTypeEnum match {
              case CellType.STRING => StringType
              case CellType.NUMERIC => DoubleType
              case _ => NullType
            }
          case CellType.STRING if c.getStringCellValue == "" || "null".equalsIgnoreCase(c.getStringCellValue) => NullType
          case CellType.STRING => StringType
          case CellType.BOOLEAN => BooleanType
          case CellType.NUMERIC => if (DateUtil.isCellDateFormatted(c)) TimestampType else DoubleType
          case CellType.BLANK => NullType
        }
      case None => NullType
    }
  }

  private def parallelize[T : scala.reflect.ClassTag](seq: Seq[T]): RDD[T] = sqlContext.sparkContext.parallelize(seq)

  /**
    * Generates a header from the given row which is null-safe and duplicate-safe.
    */
  protected def makeSafeHeader(row: Array[String]): Array[String] = {
    if (useHeader) {
      val duplicates = {
        val headerNames = row
          .filter(_ != null)
        headerNames.diff(headerNames.distinct).distinct
      }

      row.zipWithIndex.map {
        case (value, index) =>
          if (value == null || value.isEmpty) {
            // When there are empty strings or the, put the index as the suffix.
            s"_c$index"
          } else if (duplicates.contains(value)) {
            // When there are duplicates, put the index as the suffix.
            s"$value$index"
          } else {
            value
          }
      }
    } else {
      row.zipWithIndex.map {
        case (_, index) =>
          // Uses default column names, "_c#" where # is its position of fields
          // when header option is disabled.
          s"_c$index"
      }
    }
  }

  private def inferSchema(): StructType = this.userSchema.getOrElse {
    val (firstRowWithData, excerpt) = getExcerpt()

    val rawHeader = extractCells(firstRowWithData).map {
      case Some(value) => value.getStringCellValue
      case _ => ""
    }.toArray

    val header = makeSafeHeader(rawHeader)

    val baseSchema = if (this.inferSheetSchema) {
      val stringsAndCellTypes = excerpt
        .map(r => extractCells(r).map(getSparkType))
      InferSchema(parallelize(stringsAndCellTypes), header.toArray)
    } else {
      // By default fields are assumed to be StringType
      val schemaFields = header.map { fieldName =>
        StructField(fieldName.toString, StringType, nullable = true)
      }
      StructType(schemaFields)
    }
    if (addColorColumns) {
      header.foldLeft(baseSchema) { (schema, header) =>
        schema.add(s"${header}_color", StringType, nullable = true)
      }
    } else {
      baseSchema
    }
  }
}
