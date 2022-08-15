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

package org.apache.linkis.orchestrator.domain

import org.apache.linkis.common.utils.ArrayUtils.{copyArray, newArray}

/**
 */
trait TreeNode[TreeType <: TreeNode[TreeType]] extends Node {

  self: TreeType =>

  def getParents: Array[TreeType]

  def getChildren: Array[TreeType]

  def simpleString: String = this match {
    case product: Product =>
      product.productIterator
        .filter {
          case tn: TreeNode[_] if getChildren.contains(tn) || getParents.contains(tn) => false
          case Some(tn: TreeNode[_]) if getChildren.contains(tn) || getParents.contains(tn) =>
            false
          case tns: Seq[Any]
              if tns.toSet.subsetOf(getChildren.asInstanceOf[Set[Any]])
                || tns.toSet.subsetOf(getParents.asInstanceOf[Set[Any]]) =>
            false
          case _ => true
        }
        .flatMap {
          case tn: TreeNode[_] => tn.simpleString :: Nil
          case tns: Seq[_] => if (tns.nonEmpty) tns.mkString("[", ", ", "]") :: Nil else Nil
          case Some(tn: TreeNode[_]) => tn.simpleString :: Nil
          case null => Nil
          case None => Nil
          case Some(f) => f :: Nil
          case f => f :: Nil
        }
        .mkString(", ")
    case _ => getName
  }

  def verboseString: String

  def theSame(other: TreeType): Boolean = this.eq(other) || this == other

  def withNewChildren(children: Array[TreeType]): Unit

  def withNewParents(parents: Array[TreeType]): Unit

  def find(f: TreeType => Boolean): Option[TreeType] = {
    Option(this).filter(f(_)).orElse {
      getChildren.foldLeft(Option.empty[TreeType]) { case (l, r) =>
        l.orElse(r.find(f))
      }
    }
  }

  def foreach(f: TreeType => Unit): Unit = {
    f(this)
    getChildren.foreach(_.foreach(f))
  }

  def mapChildren(f: TreeType => TreeType): TreeType = {
    var changed = false
    Option(getChildren).foreach(_.foreach { child =>
      val newChild = f(child)
      if (!newChild.theSame(child)) {
        changed = true
        newChild.relateToTree(child)
        newChild
      } else child
    })
    if (changed) {
      val newTreeNode = newNode()
      newTreeNode.relateToTree(this)
      newTreeNode
    } else this
  }

  def transform(func: PartialFunction[TreeType, TreeType]): TreeType = {
    val newNode = func.applyOrElse(this, identity[TreeType])
    if (this.theSame(newNode)) {
      mapChildren(_.transform(func))
    } else {
      newNode.mapChildren(_.transform(func))
    }
  }

  protected def newNode(): TreeType

  def relateToTree(oldTreeNode: TreeType): Unit = {
    relateParents(oldTreeNode)
    relateChildren(oldTreeNode)
  }

  def relateParents(oldTreeNode: TreeType): Unit = {
    Option(oldTreeNode.getParents).foreach { parents =>
      parents.foreach { parent =>
        val children = addOrNewArray(parent.getChildren, oldTreeNode)
        parent.withNewChildren(children)
      }
      withNewParents(parents)
    }
  }

  def relateChildren(oldTreeNode: TreeType): Unit = {
    Option(oldTreeNode.getChildren).foreach { children =>
      children.foreach { child =>
        val parents = addOrNewArray(child.getParents, oldTreeNode)
        child.withNewParents(parents)
      }
      withNewChildren(children)
    }
  }

  private def addOrNewArray(array: Array[TreeType], oldTreeNode: TreeType): Array[TreeType] = {
    if (array == null || array.isEmpty) {
      val newArr = newArray[TreeType](1, array.getClass)
      newArr(0) = this
      newArr
    } else {
      val filteredArr = array.filter(_.getId != oldTreeNode.getId)
      val newArr = copyArray[TreeType](filteredArr, filteredArr.length + 1)
      newArr(filteredArr.length) = this
      newArr
    }
  }

  final def copy(): TreeType = {
    val newTreeNode = newNode()
    newTreeNode.relateToTree(this)
    newTreeNode
  }

}
