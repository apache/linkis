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

package org.apache.linkis.engineplugin.spark.common

/**
 */
object SparkKind {
  val SCALA_LAN = "scala"
  val PYTHON_LAN = "python"
  val PYTHON_END = "py"
  val R_LAN = "r"
  val SQL_LAN = "sql"
  val ML_LAN = "ml"

  val SPARK_TYPE = "spark"
  val SPARKSCALA_TYPE = "sparkscala"
  val PYSPARK_TYPE = "pyspark"
  val SPARKR_TYPE = "sparkr"
  val SPARKMIX_TYPE = "sparkmix"
  val MIX_TYPE = "mix"
  val SPARKSQL_TYPE = "sparksql"
  val SPARK_DATA_CALC_TYPE = "spark_data_calc"
  val SPARKMLSQL_TYPE = "mlsql"
  val FUNCTION_MDQ_TYPE = "function.mdq"

  def getCodeKind(code: String): Kind = {
    getKind(Kind.getKind(code))
  }

  def getSessionKind(code: String): Kind with Product with Serializable = {
    val kindStr = Kind.getKindString(code)
    if (kindStr.indexOf("@") == 0) getKind(kindStr.substring(1)) else SparkMix()
  }

  private def getKind(kindStr: String): Kind with Product with Serializable = {
    kindStr match {
      case SPARKSCALA_TYPE | SCALA_LAN => SparkScala()
      case PYSPARK_TYPE | PYTHON_LAN | PYTHON_END => PySpark()
      case SPARKR_TYPE | R_LAN => SparkR()
      case SPARKMIX_TYPE | MIX_TYPE => SparkMix()
      case SQL_LAN | SPARKSQL_TYPE => SparkSQL()
      case SPARKMLSQL_TYPE | ML_LAN => SparkMLSQL()
      case _ => throw new RuntimeException("Unknown code kind: " + kindStr)
    }
  }

}

case class SparkScala() extends Kind {
  override val toString = SparkKind.SPARKSCALA_TYPE
}

case class PySpark() extends Kind {
  override val toString = SparkKind.PYSPARK_TYPE
}

case class SparkR() extends Kind {
  override val toString = SparkKind.SPARKR_TYPE
}

case class SparkMix() extends Kind {
  override val toString: String = SparkKind.SPARKMIX_TYPE
}

case class SparkSQL() extends Kind {
  override val toString: String = SparkKind.SPARKSQL_TYPE
}

case class SparkDataCalc() extends Kind {
  override val toString: String = SparkKind.SPARK_DATA_CALC_TYPE
}

case class SparkMLSQL() extends Kind {
  override val toString = SparkKind.SPARKMLSQL_TYPE
}
