package org.apache.spark.sql.catalyst.plans.logical

import org.apache.spark.sql.catalyst.expressions.Attribute


/**
 *
 * The InsertIntoTable class is deleted in spark 3, resulting in errors in linkes compilation.
 * However, in order to be compatible with other versions of spark,
 * the following InsertIntoTable related implementations are added
 */
case class InsertIntoTable(
                            table: LogicalPlan,
                            partition: Map[String, Option[String]],
                            query: LogicalPlan,
                            overwrite: Boolean,
                            ifPartitionNotExists: Boolean)
  extends LogicalPlan {
  // IF NOT EXISTS is only valid in INSERT OVERWRITE
  assert(overwrite || !ifPartitionNotExists)
  // IF NOT EXISTS is only valid in static partitions
  assert(partition.values.forall(_.nonEmpty) || !ifPartitionNotExists)

  // We don't want `table` in children as sometimes we don't want to transform it.
  override def children: Seq[LogicalPlan] = query :: Nil
  override def output: Seq[Attribute] = Seq.empty
  override lazy val resolved: Boolean = false
}
