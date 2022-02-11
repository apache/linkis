package org.apache.linkis.gateway.authentication.bo

import java.util.Date


trait Token {

  def getTokenName(): String

  /**
    * 1 token corresponds to multiple users
    */
  def getLegalUsers(): Set[User]

  def isUserLegal(user: String): Boolean

  /**
    * 1 token corresponds to multiple Hosts
    */
  def getLegalHosts(): Set[String]

  def isHostLegal(host: String): Boolean

  def getCreateTime(): Date

  def setUpdateTime(updateTime: Date)

  def getUpdateTime(): Date

  /**
    * Unit: second
    */
  def setElapseDay(elapseDay: Long)

  def getElapseDay(): Long

  /**
    * 1 token corresponds to 1 business owner
    */
  def setBusinessOwner(businessOwner: String)

  def getBusinessOwner(): String

  def setUpdateBy(updateBy: String)

  def getUpdateBy(): String

  /**
    * decided by createdTime and elapseTime,
    * if elapseTime = -1 then will not stale forever
    */
  def isStale(): Boolean
}
