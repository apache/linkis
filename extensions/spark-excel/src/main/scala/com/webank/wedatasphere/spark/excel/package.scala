package com.webank.wedatasphere.spark

import org.apache.poi.ss.usermodel.Row.MissingCellPolicy
import org.apache.poi.ss.usermodel.{Cell, Row}

package object excel {

  implicit class RichRow(val row: Row) extends AnyVal {

    def eachCellIterator(startColumn: Int, endColumn: Int): Iterator[Option[Cell]] = new Iterator[Option[Cell]] {
      private val lastCellInclusive = row.getLastCellNum - 1
      private val endCol = Math.min(endColumn, Math.max(startColumn, lastCellInclusive))
      require(startColumn >= 0 && startColumn <= endCol)

      private var nextCol = startColumn

      override def hasNext: Boolean = nextCol <= endCol && nextCol <= lastCellInclusive

      override def next(): Option[Cell] = {
        val next =
          if (nextCol > endCol) throw new NoSuchElementException(s"column index = $nextCol")
          else Option(row.getCell(nextCol, MissingCellPolicy.RETURN_NULL_AND_BLANK))
        nextCol += 1
        next
      }
    }

  }

}
