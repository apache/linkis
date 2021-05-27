package com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler

import java.util.regex.Pattern

import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.plans.ast.ASTContext
import org.slf4j.{Logger, LoggerFactory}

import scala.util.matching.Regex

class CommentConverterCheckRuler extends ConverterCheckRuler {

  override def apply(in: JobReq, context: ASTContext): Unit = {
    in match {
      case computationJobReq: ComputationJobReq =>
        computationJobReq.getCodeLanguageLabel.getCodeType.toLowerCase match {
          case "sql" | "hql" | "psql" => computationJobReq.setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit.parseCodes(SQLCommentHelper.dealComment))
          case "python" | "py" => computationJobReq.setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit.parseCodes(PythonCommentHelper.dealComment))
          case "scala" | "java" => computationJobReq.setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit.parseCodes(ScalaCommentHelper.dealComment))
          case "sh" | "shell" =>
          case _ =>
        }
      case _ =>
    }
  }

  override def getName: String = "CommentConverterCheckRuler"
}

trait CommentHelper{
  val commentPattern:Regex
  def dealComment(code:String):String
}

object SQLCommentHelper extends CommentHelper {
  override val commentPattern: Regex = """\s*--.+\s*""".r.unanchored
  private val comment = "(?ms)('(?:''|[^'])*')|--.*?$|/\\*.*?\\*/|#.*?$|"
  private val logger:Logger = LoggerFactory.getLogger(getClass)
  override def dealComment(code: String): String = {
    try{
      val p = Pattern.compile(comment)
      val sql = p.matcher(code).replaceAll("$1")
      sql
    }catch{
      case e:Exception => logger.warn("sql comment failed")
        code
      case t:Throwable => logger.warn("sql comment failed")
        code
    }
  }
}

object PythonCommentHelper extends CommentHelper{
  override val commentPattern: Regex = """^\s*#.+\s*""".r.unanchored
  val pythonCommentPattern:String = "(?ms)([\"'](?:|[^'])*['\"])|#.*?$|/\\*.*?\\*/"
  override def dealComment(code: String): String = {
    code
  }
}


object ScalaCommentHelper extends CommentHelper{
  override val commentPattern: Regex = """^\s*//.+\s*""".r.unanchored
  private val scalaCommentPattern:String = "(?ms)([\"'](?:|[^'])*['\"])|//.*?$|/\\*.*?\\*/"
  override def dealComment(code: String): String = code
}


object CommentMain{
  def main(args: Array[String]): Unit = {
    val sqlCode = "select * from default.user;--你好;show tables"
    val sqlCode1 = "select * from default.user--你好;show tables"
    println(SQLCommentHelper.dealComment(sqlCode))
  }
}
