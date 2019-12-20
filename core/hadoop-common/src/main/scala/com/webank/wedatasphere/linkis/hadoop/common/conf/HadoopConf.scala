package com.webank.wedatasphere.linkis.hadoop.common.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * Created by johnnwang on 2019/12/11.
  */
object HadoopConf {

  val HADOOP_ROOT_USER = CommonVars("wds.linkis.hadoop.root.user", "hadoop")

  val KERBEROS_ENABLE = CommonVars("wds.linkis.keytab.enable", false)

  val KEYTAB_FILE = CommonVars("wds.linkis.keytab.file", "/appcom/keytab/")

  val kEYTAB_HOST = CommonVars("wds.linkis.keytab.host", "127.0.0.1")

  val KEYTAB_HOST_ENABLED = CommonVars("wds.linkis.keytab.host.enabled", false)

}
