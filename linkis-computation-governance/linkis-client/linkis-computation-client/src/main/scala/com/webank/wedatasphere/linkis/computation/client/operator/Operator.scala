package com.webank.wedatasphere.linkis.computation.client.operator

/**
  * Created by enjoyyin on 2021/6/6.
  */
trait Operator[T] {

  def getName: String

  def apply(): T

}