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

package org.apache.linkis.engineplugin.spark.utils;

import org.apache.linkis.engineplugin.spark.common.MultiTreeNode;

import org.apache.spark.sql.catalyst.analysis.UnresolvedAlias;
import org.apache.spark.sql.catalyst.expressions.*;
import org.apache.spark.sql.catalyst.plans.QueryPlan;
import org.apache.spark.sql.catalyst.plans.logical.*;

import java.util.ArrayList;
import java.util.List;

import scala.collection.Seq;

/** 解析Spark sql执行计划工具类 */
public class PlanParseUtil {

  /**
   * 将执行计划转换为多叉树
   *
   * @param logicalPlan 逻辑计划
   * @param level 多叉树层级
   * @return 转换后的多叉树
   */
  private static MultiTreeNode convert(LogicalPlan logicalPlan, int level) {
    if (logicalPlan == null) {
      return null;
    }
    MultiTreeNode multiRoot = new MultiTreeNode(logicalPlan);
    multiRoot.setLevel(level);
    LogicalPlan sub = null;

    if (logicalPlan instanceof Project) {
      sub = ((Project) logicalPlan).child();
    }
    if (logicalPlan instanceof SubqueryAlias) {
      sub = ((SubqueryAlias) logicalPlan).child();
    }

    if (logicalPlan instanceof Filter) {
      sub = ((Filter) logicalPlan).child();
    }

    if (logicalPlan instanceof Aggregate) {
      sub = ((Aggregate) logicalPlan).child();
    }

    if (sub == null) {
      return multiRoot;
    }
    List<LogicalPlan> children = new ArrayList<>();
    children.add(sub);

    Seq<QueryPlan<?>> seq = logicalPlan.innerChildren();
    if (seq != null && seq.size() > 0) {
      scala.collection.Iterator<QueryPlan<?>> it = seq.iterator();
      while (it.hasNext()) {
        children.add((LogicalPlan) it.next());
      }
    }

    for (LogicalPlan childItem : children) {
      sub = childItem;
      if (sub instanceof Join) {
        Join join = (Join) sub;
        LogicalPlan right = join.right();
        if (right != null) {
          MultiTreeNode rightTree = convert(right, level + 1);
          rightTree.setParent(multiRoot);
          multiRoot.getChildren().add(rightTree);
        }
        LogicalPlan left = join.left();
        while (left != null) {
          // 处理left的right为project,subQuery的情况
          MultiTreeNode childNode = new MultiTreeNode(left);
          childNode.setParent(multiRoot);
          childNode.setLevel(level + 1);
          multiRoot.getChildren().add(childNode);
          if (left instanceof Join) {
            Join leftJoin = (Join) left;
            left = leftJoin.left();
            LogicalPlan subRight = leftJoin.right();
            if (subRight != null
                && (subRight instanceof Project || subRight instanceof SubqueryAlias)) {
              MultiTreeNode subRightNode = convert(subRight, level + 2);
              subRightNode.setParent(childNode);
              childNode.getChildren().add(subRightNode);
            }
          } else if (left instanceof SubqueryAlias) {
            MultiTreeNode subNode = convert(((SubqueryAlias) left).child(), level + 2);
            subNode.setParent(childNode);
            childNode.getChildren().add(subNode);
            left = null;
          } else {
            left = null;
          }
        }
      }

      // 处理子查询中有limit的场景
      if (sub instanceof GlobalLimit) {
        GlobalLimit gl = (GlobalLimit) sub;
        sub = gl.child();
        if (sub instanceof LocalLimit) {
          LocalLimit ll = (LocalLimit) sub;
          sub = ll.child();
        }
      }

      if (sub instanceof Project
          || sub instanceof SubqueryAlias
          || sub instanceof Aggregate
          || sub instanceof Filter) {
        MultiTreeNode childNode = convert(sub, level + 1);
        childNode.setParent(multiRoot);
        multiRoot.getChildren().add(childNode);
      }
    }
    return multiRoot;
  }

  /**
   * 检测执行计划查询字段中是否使用了给定udf中的一个
   *
   * @param logicalPlan 逻辑计划
   * @param udfNames 待检查的udf函数名
   * @return 检查结果，包含一个则为true
   */
  public static boolean checkUdf(LogicalPlan logicalPlan, String[] udfNames) {
    if (udfNames == null || udfNames.length == 0) {
      return false;
    }

    // 处理 limit
    if (logicalPlan instanceof GlobalLimit) {
      GlobalLimit gl = (GlobalLimit) logicalPlan;
      logicalPlan = gl.child();
      if (logicalPlan instanceof LocalLimit) {
        LocalLimit ll = (LocalLimit) logicalPlan;
        logicalPlan = ll.child();
      }
    }
    // 处理 order by
    if (logicalPlan instanceof Sort) {
      Sort sort = (Sort) logicalPlan;
      logicalPlan = sort.child();
    }

    MultiTreeNode root = convert(logicalPlan, 0);
    for (String udfName : udfNames) {
      if (containsUdf(root, udfName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检测执行计划中是否使用给定的udf
   *
   * @param multiTreeNode 逻辑计划转换后的多叉树
   * @param udfName 待检查udf名
   * @return 检查结果
   */
  public static boolean containsUdf(MultiTreeNode multiTreeNode, String udfName) {
    if (multiTreeNode == null) {
      return false;
    }
    LogicalPlan logicalPlan = multiTreeNode.getLogicalPlan();
    if (logicalPlan == null) {
      return false;
    }

    if (logicalPlan instanceof Filter) {
      Filter filter = (Filter) logicalPlan;
      logicalPlan = filter.child();
    }

    // SubqueryAlias Filter Aggregate
    Seq<NamedExpression> seq = null;
    if (logicalPlan instanceof Aggregate) {
      seq = ((Aggregate) logicalPlan).aggregateExpressions();
    }
    if (logicalPlan instanceof Project) {
      seq = ((Project) logicalPlan).projectList();
    }

    if (seq != null && !seq.isEmpty()) {
      scala.collection.Iterator<NamedExpression> it = seq.iterator();
      while (it.hasNext()) {
        NamedExpression next = it.next();
        if (next instanceof Alias) {
          Alias alias = (Alias) next;
          if (alias.name().contains(udfName)) {
            return true;
          }
          Expression child = alias.child();
          if (child instanceof ScalaUDF) {
            ScalaUDF su = (ScalaUDF) child;
            String useUdfName = su.udfName().get();
            if (udfName.equals(useUdfName)) {
              return true;
            }
          }
          if (child instanceof PythonUDF) {
            PythonUDF pu = (PythonUDF) child;
            String useUdfName = pu.name();
            if (udfName.equals(useUdfName)) {
              return true;
            }
          }
        }
        if (next instanceof UnresolvedAlias) {
          UnresolvedAlias alias = (UnresolvedAlias) next;
          Expression child = alias.child();
          if (child instanceof ScalaUDF) {
            ScalaUDF su = (ScalaUDF) child;
            String useUdfName = su.udfName().get();
            if (udfName.equals(useUdfName)) {
              return true;
            }
          }
          if (child instanceof PythonUDF) {
            PythonUDF pu = (PythonUDF) child;
            String useUdfName = pu.name();
            if (udfName.equals(useUdfName)) {
              return true;
            }
          }
        }
      }
    }

    if (multiTreeNode.getChildren() == null) {
      return false;
    }
    for (MultiTreeNode node : multiTreeNode.getChildren()) {
      boolean subRes = containsUdf(node, udfName);
      if (subRes) {
        return true;
      }
    }
    return false;
  }
}
