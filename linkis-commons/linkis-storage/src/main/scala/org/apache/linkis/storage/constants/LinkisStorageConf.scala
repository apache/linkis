package org.apache.linkis.storage.constants

import org.apache.linkis.common.conf.CommonVars

object LinkisStorageConf {
  val HDFS_FILE_SYSTEM_REST_ERRS: String =
    CommonVars.apply(
      "wds.linkis.hdfs.rest.errs",
      ".*Filesystem closed.*|.*Failed to find any Kerberos tgt.*")
      .getValue
}
