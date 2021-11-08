/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconnplugin.sqoop.manager;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.metastore.JobData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sqoop.manager.com.webank.wedatasphere.SQLServerManager;


public class ExtraManagerFactory extends com.cloudera.sqoop.manager.ManagerFactory{
    public static final Log LOG = LogFactory.getLog(
            ExtraManagerFactory.class.getName());
    public static final String NET_SOURCEFORGE_JTDS_JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    public ConnManager accept(JobData data) {
        SqoopOptions options = data.getSqoopOptions();

        String scheme = extractScheme(options);
        if (null == scheme) {
            // We don't know if this is a mysql://, hsql://, etc.
            // Can't do anything with this.
            LOG.warn("Null scheme associated with connect string.");
            return null;
        }

        LOG.debug("Trying with scheme: " + scheme);
        options.setConnectString(options.getConnectString().replace("linkis:",""));
        if ("linkis:jdbc:mysql:".equals(scheme)) {
            if (options.isDirect()) {
                return new org.apache.sqoop.manager.com.webank.wedatasphere.DirectMySQLManager(options);
            } else {
                return new org.apache.sqoop.manager.com.webank.wedatasphere.MySQLManager(options);
            }
        } else if ("linkis:jdbc:postgresql:".equals(scheme)) {
            if (options.isDirect()) {
                return new org.apache.sqoop.manager.com.webank.wedatasphere.DirectPostgresqlManager(options);
            } else {
                return new org.apache.sqoop.manager.com.webank.wedatasphere.PostgresqlManager(options);
            }
        } else if ("linkis:jdbc:hsqldb:".equals(scheme)) {
            return new org.apache.sqoop.manager.com.webank.wedatasphere.HsqldbManager(options);
        } else if ("linkis:jdbc:oracle:".equals(scheme)) {
            return new org.apache.sqoop.manager.com.webank.wedatasphere.OracleManager(options);
        } else if ("linkis:jdbc:sqlserver:".equals(scheme)) {
            return new org.apache.sqoop.manager.com.webank.wedatasphere.SQLServerManager(options);
        } else if ("linkis:jdbc:jtds:sqlserver:".equals(scheme)) {
            return new SQLServerManager(NET_SOURCEFORGE_JTDS_JDBC_DRIVER, options);
        } else if ("linkis:jdbc:db2:".equals(scheme)) {
            return new org.apache.sqoop.manager.com.webank.wedatasphere.Db2Manager(options);
        } else if ("linkis:jdbc:netezza:".equals(scheme)) {
            if (options.isDirect()) {
                return new org.apache.sqoop.manager.com.webank.wedatasphere.DirectNetezzaManager(options);
            } else {
                return new org.apache.sqoop.manager.com.webank.wedatasphere.NetezzaManager(options);
            }
        } else if ("linkis:jdbc:cubrid:".equals(scheme)) {
            return new org.apache.sqoop.manager.com.webank.wedatasphere.CubridManager(options);
        } else {
            return null;
        }
    }

    protected String extractScheme(SqoopOptions options) {
        String connectStr = options.getConnectString();
        int schemeStopIdx = connectStr.indexOf("//");
        if (-1 == schemeStopIdx) {
            schemeStopIdx = connectStr.lastIndexOf(':');
            if (-1 == schemeStopIdx) {
                LOG.warn("Could not determine scheme component of connect string");
                schemeStopIdx = connectStr.length();
            }
        }
        return connectStr.substring(0, schemeStopIdx);
    }
}
