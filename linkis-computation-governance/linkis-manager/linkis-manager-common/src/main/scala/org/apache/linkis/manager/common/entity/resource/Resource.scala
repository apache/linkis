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

package org.apache.linkis.manager.common.entity.resource

import org.apache.linkis.common.utils.{ByteTimeUtils, Logging}
import org.apache.linkis.manager.common.entity.resource.ResourceType._
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary._
import org.apache.linkis.manager.common.exception.ResourceWarnException

import org.apache.commons.lang3.StringUtils

import java.text.MessageFormat

import scala.collection.JavaConverters._

import org.json4s.{CustomSerializer, DefaultFormats, Extraction}
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.jackson.Serialization

abstract class Resource {
  def add(r: Resource): Resource

  def minus(r: Resource): Resource

  def multiplied(r: Resource): Resource

  def multiplied(rate: Float): Resource

  def divide(r: Resource): Resource

  def divide(rate: Int): Resource

  def moreThan(r: Resource): Boolean

  /**
   * Part is greater than(部分大于)
   *
   * @param r
   * @return
   */
  def caseMore(r: Resource): Boolean

  def equalsTo(r: Resource): Boolean

  def notLess(r: Resource): Boolean

  def +(r: Resource): Resource = add(r)

  def -(r: Resource): Resource = minus(r)

  def *(rate: Float): Resource = multiplied(rate)

  def *(rate: Double): Resource = multiplied(rate.toFloat)

  def /(rate: Int): Resource = divide(rate)

  def ==(r: Resource): Boolean = equalsTo(r)

  def >(r: Resource): Boolean = moreThan(r)

  def >=(r: Resource): Boolean = notLess(r)

  def <(r: Resource): Boolean = ! >=(r)

  def <=(r: Resource): Boolean = ! >(r)

  def toJson: String
}

object Resource extends Logging {

  def initResource(resourceType: ResourceType): Resource = resourceType match {
    case CPU => new CPUResource(0)
    case Memory => new MemoryResource(0)
    case Load => new LoadResource(0, 0)
    case Instance => new InstanceResource(0)
    case LoadInstance => new LoadInstanceResource(0, 0, 0)
    case Yarn => new YarnResource(0, 0, 0)
    case DriverAndYarn =>
      new DriverAndYarnResource(new LoadInstanceResource(0, 0, 0), new YarnResource(0, 0, 0))
    case Special => new SpecialResource(new java.util.HashMap[String, AnyVal]())
    case Default => new LoadResource(0, 0)
    case _ =>
      throw new ResourceWarnException(
        NOT_RESOURCE_POLICY.getErrorCode,
        NOT_RESOURCE_POLICY.getErrorDesc
      )
  }

  def getZeroResource(resource: Resource): Resource = resource match {
    case m: MemoryResource => new MemoryResource(0)
    case i: InstanceResource => new InstanceResource(0)
    case c: CPUResource => new CPUResource(0)
    case l: LoadResource => new LoadResource(0, 0)
    case li: LoadInstanceResource => new LoadInstanceResource(0, 0, 0)
    case yarn: YarnResource => new YarnResource(0, 0, 0)
    case dy: DriverAndYarnResource =>
      if (dy.yarnResource != null && dy.yarnResource.queueName != null) {
        new DriverAndYarnResource(
          new LoadInstanceResource(0, 0, 0),
          new YarnResource(0, 0, 0, dy.yarnResource.queueName)
        )
      } else {
        new DriverAndYarnResource(new LoadInstanceResource(0, 0, 0), new YarnResource(0, 0, 0))
      }
    case s: SpecialResource => new SpecialResource(new java.util.HashMap[String, AnyVal]())
    case r: Resource =>
      throw new ResourceWarnException(
        NOT_RESOURCE_TYPE.getErrorCode,
        MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
      )
  }

}

case class UserAvailableResource(moduleName: String, resource: Resource)

class MemoryResource(val memory: Long) extends Resource {

  private implicit def toMemoryResource(r: Resource): MemoryResource = r match {
    case t: MemoryResource => t
    case _ => new MemoryResource(Long.MaxValue)
  }

  override def add(r: Resource): Resource = new MemoryResource(memory + r.memory)

  override def minus(r: Resource): Resource = new MemoryResource(memory - r.memory)

  override def multiplied(r: Resource): Resource = new MemoryResource(memory * r.memory)

  override def multiplied(rate: Float): Resource = new MemoryResource((memory * rate).toLong)

  override def divide(r: Resource): Resource = new MemoryResource(memory / r.memory)

  override def divide(rate: Int): Resource = new MemoryResource(memory / rate)

  override def moreThan(r: Resource): Boolean = memory > r.memory

  override def notLess(r: Resource): Boolean = memory >= r.memory

  /**
   * Part is greater than(部分大于)
   *
   * @param r
   * @return
   */
  override def caseMore(r: Resource): Boolean = moreThan(r)

  override def equalsTo(r: Resource): Boolean = memory == r.memory

  override def toJson: String = s""" "memory":"${ByteTimeUtils.bytesToString(memory)}" """

  override def toString: String = toJson

}

class CPUResource(val cores: Int) extends Resource {

  private implicit def toCPUResource(r: Resource): CPUResource = r match {
    case t: CPUResource => t
    case _ => new CPUResource(Integer.MAX_VALUE)
  }

  protected def toResource(cores: Int): Resource = new CPUResource(cores)

  override def add(r: Resource): Resource = toResource(cores + r.cores)

  override def minus(r: Resource): Resource = toResource(cores - r.cores)

  override def multiplied(r: Resource): Resource = toResource(cores * r.cores)

  override def multiplied(rate: Float): Resource = toResource((cores * rate).toInt)

  override def divide(r: Resource): Resource = toResource(cores / r.cores)

  override def divide(rate: Int): Resource = toResource(cores / rate)

  override def moreThan(r: Resource): Boolean = cores > r.cores

  /**
   * Part is greater than(部分大于)
   *
   * @param r
   * @return
   */
  override def caseMore(r: Resource): Boolean = moreThan(r)

  override def notLess(r: Resource): Boolean = cores >= r.cores

  override def equalsTo(r: Resource): Boolean = cores == r.cores

  override def toJson: String = s""" "cpu":$cores """

  override def toString: String = toJson
}

class LoadResource(val memory: Long, val cores: Int) extends Resource {

  private implicit def toLoadResource(r: Resource): LoadResource = r match {
    case t: LoadResource => t
    case m: MemoryResource => new LoadResource(m.memory, 0)
    case c: CPUResource => new LoadResource(0, c.cores)
    case _ => new LoadResource(Long.MaxValue, Integer.MAX_VALUE)
  }

  def add(r: Resource): LoadResource =
    new LoadResource(memory + r.memory, cores + r.cores)

  def minus(r: Resource): LoadResource =
    new LoadResource(memory - r.memory, cores - r.cores)

  def multiplied(r: Resource): LoadResource =
    new LoadResource(memory * r.memory, cores * r.cores)

  def multiplied(rate: Float): LoadResource =
    new LoadResource((memory * rate).toLong, math.round(cores * rate))

  def divide(r: Resource): LoadResource =
    new LoadResource(memory / r.memory, cores / r.cores)

  def divide(rate: Int): LoadResource =
    new LoadResource(memory / rate, cores / rate)

  def moreThan(r: Resource): Boolean =
    memory > r.memory && cores > r.cores

  def caseMore(r: Resource): Boolean =
    memory > r.memory || cores > r.cores

  def equalsTo(r: Resource): Boolean =
    memory == r.memory && cores == r.cores

  override def notLess(r: Resource): Boolean =
    memory >= r.memory && cores >= r.cores

  override def toJson: String =
    s"""{"memory":"${ByteTimeUtils.bytesToString(memory)}","cpu":$cores}"""

  override def toString: String = toJson
}

class LoadInstanceResource(val memory: Long, val cores: Int, val instances: Int) extends Resource {

  implicit def toLoadInstanceResource(r: Resource): LoadInstanceResource = r match {
    case t: LoadInstanceResource => t
    case l: LoadResource => new LoadInstanceResource(l.memory, l.cores, 0)
    case m: MemoryResource => new LoadInstanceResource(m.memory, 0, 0)
    case c: CPUResource => new LoadInstanceResource(0, c.cores, 0)
    case d: DriverAndYarnResource => d.loadInstanceResource // yarn resource has special logic
    case _ => new LoadInstanceResource(Long.MaxValue, Integer.MAX_VALUE, Integer.MAX_VALUE)
  }

  def add(r: Resource): LoadInstanceResource =
    new LoadInstanceResource(memory + r.memory, cores + r.cores, instances + r.instances)

  def minus(r: Resource): LoadInstanceResource =
    new LoadInstanceResource(memory - r.memory, cores - r.cores, instances - r.instances)

  def multiplied(r: Resource): LoadInstanceResource =
    new LoadInstanceResource(memory * r.memory, cores * r.cores, instances * r.instances)

  def multiplied(rate: Float): LoadInstanceResource =
    new LoadInstanceResource(
      (memory * rate).toLong,
      math.round(cores * rate),
      (instances * rate).toInt
    )

  def divide(r: Resource): LoadInstanceResource =
    new LoadInstanceResource(memory / r.memory, cores / r.cores, instances / r.instances)

  def divide(rate: Int): LoadInstanceResource =
    new LoadInstanceResource(memory / rate, cores / rate, instances * rate)

  def moreThan(r: Resource): Boolean =
    memory > r.memory && cores > r.cores && instances > r.instances

  def caseMore(r: Resource): Boolean =
    memory > r.memory || cores > r.cores || instances > r.instances

  def equalsTo(r: Resource): Boolean =
    memory == r.memory && cores == r.cores && instances == r.instances

  override def notLess(r: Resource): Boolean =
    memory >= r.memory && cores >= r.cores && instances >= r.instances

  override def toJson: String =
    s"""{"instance":$instances,"memory":"${ByteTimeUtils.bytesToString(memory)}","cpu":$cores}"""

  override def toString: String =
    s"Number of instances(实例数)：$instances，(RAM)内存：${ByteTimeUtils.bytesToString(memory)},cpu:$cores"

}

class InstanceResource(val instances: Int) extends CPUResource(instances) {
  override protected def toResource(cores: Int): Resource = new InstanceResource(cores)

  override def toJson: String = s"Instance: $instances"

  override def toString: String = toJson
}

/**
 * Queue resource information, no initial registration, only user resource usage limit
 * 队列资源信息，无初始化注册，只有用户资源使用上限
 *
 * @param queueName
 * @param queueMemory
 * @param queueCores
 * @param queueInstances
 */
class YarnResource(
    val queueMemory: Long,
    val queueCores: Int,
    val queueInstances: Int,
    val queueName: String = "default",
    val applicationId: String = ""
) extends Resource {

  implicit def toYarnResource(r: Resource): YarnResource = r match {
    case t: YarnResource => t
    case _ => new YarnResource(Long.MaxValue, Integer.MAX_VALUE, Integer.MAX_VALUE, "default")
  }

  def add(r: Resource): YarnResource = {
    new YarnResource(
      queueMemory + r.queueMemory,
      queueCores + r.queueCores,
      queueInstances + r.queueInstances,
      r.queueName
    )
  }

  def minus(r: Resource): YarnResource = {
    new YarnResource(
      queueMemory - r.queueMemory,
      queueCores - r.queueCores,
      queueInstances - r.queueInstances,
      r.queueName
    )
  }

  def multiplied(r: Resource): YarnResource = {
    new YarnResource(
      queueMemory * r.queueMemory,
      queueCores * r.queueCores,
      queueInstances * r.queueInstances,
      r.queueName
    )
  }

  def multiplied(rate: Float): YarnResource =
    new YarnResource(
      (queueMemory * rate).toInt,
      (queueCores * rate).toInt,
      (queueInstances * rate).toInt,
      queueName
    )

  def divide(r: Resource): YarnResource = {
    new YarnResource(
      queueMemory / r.queueMemory,
      queueCores / r.queueCores,
      queueInstances / r.queueInstances,
      r.queueName
    )
  }

  def divide(rate: Int): YarnResource =
    new YarnResource(queueMemory / rate, queueCores / rate, queueInstances / rate, queueName)

  def moreThan(r: Resource): Boolean = {
    queueMemory > r.queueMemory && queueCores > r.queueCores
  }

  def caseMore(r: Resource): Boolean = {
    queueMemory > r.queueMemory || queueCores > r.queueCores
  }

  def equalsTo(r: Resource): Boolean =
    queueName == r.queueName && queueMemory == r.queueMemory && queueCores == r.queueCores

  override def notLess(r: Resource): Boolean =
    queueMemory >= r.queueMemory && queueCores >= r.queueCores

  override def toJson: String =
    s"""{"queueName":"$queueName","queueMemory":"${ByteTimeUtils.bytesToString(
      queueMemory
    )}", "queueCpu":$queueCores, "instance":$queueInstances}"""

  override def toString: String =
    s"Queue name(队列名)：$queueName，Queue memory(队列内存)：${ByteTimeUtils.bytesToString(queueMemory)}，Queue core number(队列核数)：$queueCores, Number of queue instances(队列实例数)：${queueInstances}"

}

class DriverAndYarnResource(
    val loadInstanceResource: LoadInstanceResource,
    val yarnResource: YarnResource
) extends Resource
    with Logging {

  private implicit def DriverAndYarnResource(r: Resource): DriverAndYarnResource = r match {
    case t: DriverAndYarnResource => t
    case y: YarnResource => new DriverAndYarnResource(new LoadInstanceResource(0, 0, 0), y)
    case t: LoadInstanceResource => new DriverAndYarnResource(t, new YarnResource(0, 0, 0))
    case l: LoadResource =>
      new DriverAndYarnResource(
        new LoadInstanceResource(l.memory, l.cores, 0),
        new YarnResource(0, 0, 0)
      )
    case m: MemoryResource =>
      new DriverAndYarnResource(new LoadInstanceResource(m.memory, 0, 0), new YarnResource(0, 0, 0))
    case c: CPUResource =>
      new DriverAndYarnResource(new LoadInstanceResource(0, c.cores, 0), new YarnResource(0, 0, 0))
    case _ =>
      new DriverAndYarnResource(
        new LoadInstanceResource(Long.MaxValue, Integer.MAX_VALUE, Integer.MAX_VALUE),
        new YarnResource(Long.MaxValue, Integer.MAX_VALUE, Integer.MAX_VALUE)
      )
  }

  def isModuleOperate(r: Resource): Boolean = {
    if (this.isModuleOperate || r.isModuleOperate) {
      true
    } else if (this.yarnResource.queueName.equals(r.yarnResource.queueName)) {
      logger.debug(s"Not module operate this:$this other:$r")
      false
    } else {
      true
    }
  }

  def isModuleOperate: Boolean = {
    if (this.yarnResource != null && StringUtils.isNotEmpty(this.yarnResource.queueName)) {
      false
    } else {
      true
    }
  }

  override def add(r: Resource): DriverAndYarnResource = {
    if (isModuleOperate(r)) {
      new DriverAndYarnResource(
        this.loadInstanceResource.add(r.loadInstanceResource),
        this.yarnResource
      )
    } else {
      new DriverAndYarnResource(
        this.loadInstanceResource.add(r.loadInstanceResource),
        this.yarnResource.add(r.yarnResource)
      )
    }
  }

  override def minus(r: Resource): Resource = {
    if (isModuleOperate(r)) {
      new DriverAndYarnResource(
        this.loadInstanceResource.minus(r.loadInstanceResource),
        this.yarnResource
      )
    } else {
      new DriverAndYarnResource(
        this.loadInstanceResource.minus(r.loadInstanceResource),
        this.yarnResource.minus(r.yarnResource)
      )
    }
  }

  override def multiplied(r: Resource): Resource = {
    throw new ResourceWarnException(
      OPERATION_MULTIPLIED.getErrorCode,
      OPERATION_MULTIPLIED.getErrorDesc
    )
  }

  override def multiplied(rate: Float): Resource = {
    if (isModuleOperate) {
      new DriverAndYarnResource(this.loadInstanceResource.multiplied(rate), this.yarnResource)
    } else {
      new DriverAndYarnResource(
        this.loadInstanceResource.multiplied(rate),
        this.yarnResource.multiplied(rate)
      )
    }
  }

  override def divide(r: Resource): Resource =
    throw new ResourceWarnException(
      OPERATION_MULTIPLIED.getErrorCode,
      OPERATION_MULTIPLIED.getErrorDesc
    )

  override def divide(rate: Int): Resource = if (isModuleOperate) {
    new DriverAndYarnResource(this.loadInstanceResource.divide(rate), this.yarnResource)
  } else {
    new DriverAndYarnResource(
      this.loadInstanceResource.divide(rate),
      this.yarnResource.divide(rate)
    )
  }

  override def moreThan(r: Resource): Boolean = {
    if (isModuleOperate(r)) {
      this.loadInstanceResource.moreThan(r.loadInstanceResource)
    } else {
      this.loadInstanceResource.moreThan(r.loadInstanceResource) && this.yarnResource.moreThan(
        r.yarnResource
      )
    }
  }

  override def caseMore(r: Resource): Boolean = if (isModuleOperate(r)) {
    this.loadInstanceResource.caseMore(r.loadInstanceResource)
  } else {
    this.loadInstanceResource.caseMore(r.loadInstanceResource) || this.yarnResource.caseMore(
      r.yarnResource
    )
  }

  override def equalsTo(r: Resource): Boolean = if (isModuleOperate(r)) {
    this.loadInstanceResource.equalsTo(r.loadInstanceResource)
  } else {
    this.loadInstanceResource.equalsTo(r.loadInstanceResource) && this.yarnResource.equalsTo(
      r.yarnResource
    )
  }

  override def notLess(r: Resource): Boolean = if (isModuleOperate(r)) {
    this.loadInstanceResource.notLess(r.loadInstanceResource)
  } else {
    this.loadInstanceResource.notLess(r.loadInstanceResource) && this.yarnResource.notLess(
      r.yarnResource
    )
  }

  override def toJson: String = {
    var load = "null"
    var yarn = "null"
    if (loadInstanceResource != null) load = loadInstanceResource.toJson
    if (yarnResource != null) yarn = yarnResource.toJson
    s"""{"driver":${load}, "yarn":${yarn}}"""
  }

  override def toString: String =
    s"Driver resources(Driver资源)：$loadInstanceResource,Queue resource(队列资源):$yarnResource"

}

class SpecialResource(val resources: java.util.Map[String, AnyVal]) extends Resource {
  def this(resources: Map[String, AnyVal]) = this(resources.asJava)

  private def specialResourceOperator(
      r: Resource,
      op: (AnyVal, AnyVal) => AnyVal
  ): SpecialResource = r match {
    case s: SpecialResource =>
      val rs = s.resources
      new SpecialResource(resources.asScala.map { case (k, v) =>
        val v1 = rs.get(k)
        k -> op(v, v1)
      }.toMap)
    case _ => new SpecialResource(Map.empty[String, AnyVal])
  }

  override def add(r: Resource): Resource = specialResourceOperator(
    r,
    (v1, v2) =>
      v1 match {
        case i: Int => i + v2.asInstanceOf[Int]
        case d: Double => d + v2.asInstanceOf[Double]
        case l: Long => l + v2.asInstanceOf[Long]
        case f: Float => f + v2.asInstanceOf[Float]
        case s: Short => s + v2.asInstanceOf[Short]
        case _ =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
          )
      }
  )

  override def minus(r: Resource): Resource = specialResourceOperator(
    r,
    (v1, v2) =>
      v1 match {
        case i: Int => i - v2.asInstanceOf[Int]
        case d: Double => d - v2.asInstanceOf[Double]
        case l: Long => l - v2.asInstanceOf[Long]
        case f: Float => f - v2.asInstanceOf[Float]
        case s: Short => s - v2.asInstanceOf[Short]
        case _ =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
          )
      }
  )

  override def multiplied(r: Resource): Resource = specialResourceOperator(
    r,
    (v1, v2) =>
      v1 match {
        case i: Int => i * v2.asInstanceOf[Int]
        case d: Double => d * v2.asInstanceOf[Double]
        case l: Long => l * v2.asInstanceOf[Long]
        case f: Float => f * v2.asInstanceOf[Float]
        case s: Short => s * v2.asInstanceOf[Short]
        case _ =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
          )
      }
  )

  override def multiplied(rate: Float): Resource = new SpecialResource(
    resources.asScala
      .map {
        case (k, i: Int) => k -> (i * rate).toInt
        case (k, d: Double) => k -> d * rate
        case (k, l: Long) => k -> (l * rate).toLong
        case (k, f: Float) => k -> f * rate
        case (k, s: Short) => k -> (s * rate).toShort
        case (k, v) =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, v.getClass)
          )
      }
      .toMap[String, AnyVal]
  )

  override def divide(r: Resource): Resource = specialResourceOperator(
    r,
    (v1, v2) =>
      v1 match {
        case i: Int => i / v2.asInstanceOf[Int]
        case d: Double => d / v2.asInstanceOf[Double]
        case l: Long => l / v2.asInstanceOf[Long]
        case f: Float => f / v2.asInstanceOf[Float]
        case s: Short => s / v2.asInstanceOf[Short]
        case _ =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
          )
      }
  )

  override def divide(rate: Int): Resource = new SpecialResource(
    resources.asScala
      .map {
        case (k, i: Int) => k -> i / rate
        case (k, d: Double) => k -> d / rate
        case (k, l: Long) => k -> l / rate
        case (k, f: Float) => k -> f / rate
        case (k, s: Short) => k -> (s / rate).toShort
        case (k, v) =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, v.getClass)
          )
      }
      .toMap[String, AnyVal]
  )

  override def moreThan(r: Resource): Boolean = r match {
    case s: SpecialResource =>
      val rs = s.resources
      !resources.asScala.exists {
        case (k, i: Int) => i <= rs.get(k).asInstanceOf[Int]
        case (k, d: Double) => d <= rs.get(k).asInstanceOf[Double]
        case (k, l: Long) => l <= rs.get(k).asInstanceOf[Long]
        case (k, f: Float) => f <= rs.get(k).asInstanceOf[Float]
        case (k, s: Short) => s <= rs.get(k).asInstanceOf[Short]
        case (k, v) =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, v.getClass)
          )
      }
    case _ => true
  }

  /**
   * Part is greater than(部分大于)
   *
   * @param r
   * @return
   */
  override def caseMore(r: Resource): Boolean = r match {
    case s: SpecialResource =>
      val rs = s.resources
      resources.asScala.exists {
        case (k, i: Int) => i > rs.get(k).asInstanceOf[Int]
        case (k, d: Double) => d > rs.get(k).asInstanceOf[Double]
        case (k, l: Long) => l > rs.get(k).asInstanceOf[Long]
        case (k, f: Float) => f > rs.get(k).asInstanceOf[Float]
        case (k, s: Short) => s > rs.get(k).asInstanceOf[Short]
        case (k, v) =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, v.getClass)
          )
      }
    case _ => true
  }

  override def equalsTo(r: Resource): Boolean = r match {
    case s: SpecialResource =>
      val rs = s.resources
      !resources.asScala.exists {
        case (k, i: Int) => i != rs.get(k).asInstanceOf[Int]
        case (k, d: Double) => d != rs.get(k).asInstanceOf[Double]
        case (k, l: Long) => l != rs.get(k).asInstanceOf[Long]
        case (k, f: Float) => f != rs.get(k).asInstanceOf[Float]
        case (k, s: Short) => s != rs.get(k).asInstanceOf[Short]
        case (k, v) =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, v.getClass)
          )
      }
    case _ => true
  }

  override def notLess(r: Resource): Boolean = r match {
    case s: SpecialResource =>
      val rs = s.resources
      !resources.asScala.exists {
        case (k, i: Int) => i < rs.get(k).asInstanceOf[Int]
        case (k, d: Double) => d < rs.get(k).asInstanceOf[Double]
        case (k, l: Long) => l < rs.get(k).asInstanceOf[Long]
        case (k, f: Float) => f < rs.get(k).asInstanceOf[Float]
        case (k, s: Short) => s < rs.get(k).asInstanceOf[Short]
        case (k, v) =>
          throw new ResourceWarnException(
            NOT_RESOURCE_TYPE.getErrorCode,
            MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, v.getClass)
          )
      }
    case _ => true
  }

  override def toJson: String = s"Special:$resources"
}

object ResourceSerializer
    extends CustomSerializer[Resource](implicit formats =>
      (
        {
          case JObject(List(("memory", memory))) => new MemoryResource(memory.extract[Long])
          case JObject(List(("cores", cores))) => new CPUResource(cores.extract[Int])
          case JObject(List(("instance", instances))) =>
            new InstanceResource(instances.extract[Int])
          case JObject(List(("memory", memory), ("cores", cores))) =>
            new LoadResource(memory.extract[Long], cores.extract[Int])
          case JObject(List(("memory", memory), ("cores", cores), ("instance", instances))) =>
            new LoadInstanceResource(
              memory.extract[Long],
              cores.extract[Int],
              instances.extract[Int]
            )
          case JObject(
                List(
                  ("applicationId", applicationId),
                  ("queueName", queueName),
                  ("queueMemory", queueMemory),
                  ("queueCores", queueCores),
                  ("queueInstances", queueInstances)
                )
              ) =>
            new YarnResource(
              queueMemory.extract[Long],
              queueCores.extract[Int],
              queueInstances.extract[Int],
              queueName.extract[String],
              applicationId.extract[String]
            )
          case JObject(
                List(
                  (
                    "DriverAndYarnResource",
                    JObject(
                      List(
                        ("loadInstanceResource", loadInstanceResource),
                        ("yarnResource", yarnResource)
                      )
                    )
                  )
                )
              ) =>
            implicit val formats = DefaultFormats
            new DriverAndYarnResource(
              loadInstanceResource.extract[LoadInstanceResource],
              yarnResource.extract[YarnResource]
            )
          case JObject(List(("resources", resources))) =>
            new SpecialResource(resources.extract[Map[String, AnyVal]])
          case JObject(list) =>
            throw new ResourceWarnException(
              NOT_RESOURCE_STRING.getErrorCode,
              NOT_RESOURCE_STRING.getErrorDesc + list
            )
        },
        {
          case m: MemoryResource => ("memory", m.memory)
          case c: CPUResource => ("cores", c.cores)
          case i: InstanceResource => ("instance", i.instances)
          case l: LoadResource => ("memory", l.memory) ~ ("cores", l.cores)
          case li: LoadInstanceResource =>
            ("memory", li.memory) ~ ("cores", li.cores) ~ ("instance", li.instances)
          case yarn: YarnResource =>
            (
              "applicationId",
              yarn.applicationId
            ) ~ ("queueName", yarn.queueName) ~ ("queueMemory", yarn.queueMemory) ~ ("queueCores", yarn.queueCores) ~ ("queueInstances", yarn.queueInstances)
          case dy: DriverAndYarnResource =>
            implicit val formats = DefaultFormats
            (
              "DriverAndYarnResource",
              new JObject(
                List(
                  ("loadInstanceResource", Extraction.decompose(dy.loadInstanceResource)),
                  ("yarnResource", Extraction.decompose(dy.yarnResource))
                )
              )
            )
          case s: SpecialResource =>
            ("resources", Serialization.write(s.resources.asScala.toMap))
          case r: Resource =>
            throw new ResourceWarnException(
              NOT_RESOURCE_TYPE.getErrorCode,
              MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
            )
        }
      )
    )
