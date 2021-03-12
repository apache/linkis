package com.webank.wedatasphere.linkis.storage.source

import java.util

import com.webank.wedatasphere.linkis.storage.LineRecord
import com.webank.wedatasphere.linkis.storage.script.ScriptRecord
import org.apache.commons.math3.util.Pair

import scala.collection.JavaConversions._


class TextFileSource(fileSplits: Array[FileSplit]) extends AbstractFileSource(fileSplits) {

  shuffle({
    case s: ScriptRecord if "".equals(s.getLine) => new LineRecord("\n")
    case record => record
  })

  override def collect(): Array[Pair[Object, util.ArrayList[Array[String]]]] = {
    val collects: Array[Pair[Object, util.ArrayList[Array[String]]]] = super.collect()
    if (!getParams.getOrDefault("ifMerge", "true").toBoolean) return collects
    val snds: Array[util.ArrayList[Array[String]]] = collects.map(_.getSecond)
    snds.foreach { snd =>
      val str = new StringBuilder
      snd.foreach {
        case Array("\n") => str.append("\n")
        case Array(y) => str.append(y).append("\n")
      }
      snd.clear()
      snd.add(Array(str.toString()))
    }
    collects
  }

}
