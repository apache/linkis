#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

include Java
java_import org.apache.hadoop.hbase.client.ConnectionFactory
java_import org.apache.hadoop.hbase.HBaseConfiguration
java_import org.apache.linkis.manager.engineplugin.hbase.HBaseConnectionManager

require 'hbase/admin'
require 'hbase/table'
require 'hbase/quotas'
require 'hbase/security'
require 'hbase/visibility_labels'

module Hbase
  class Hbase
    attr_accessor :configuration

    def initialize(properties = nil)
      # Create configuration
      @connection = nil
      if properties
        self.configuration = HBaseConnectionManager.getInstance().getConfiguration(properties)
        @connection = HBaseConnectionManager.getInstance().getConnection(self.configuration)
      else
        self.configuration = org.apache.hadoop.hbase.HBaseConfiguration.create
        # Turn off retries in hbase and ipc.  Human doesn't want to wait on N retries.
        configuration.setInt("hbase.client.retries.number", 7)
        configuration.setInt("hbase.ipc.client.connect.max.retries", 3)
        @connection = ConnectionFactory.createConnection(self.configuration)
      end
    end

    def admin(formatter)
      ::Hbase::Admin.new(@connection.getAdmin, formatter)
    end

    # Create new one each time
    def table(table, shell)
      ::Hbase::Table.new(@connection.getTable(table), shell)
    end

    def replication_admin(formatter)
      ::Hbase::RepAdmin.new(configuration, formatter)
    end

    def security_admin(formatter)
      ::Hbase::SecurityAdmin.new(@connection.getAdmin, formatter)
    end

    def visibility_labels_admin(formatter)
      ::Hbase::VisibilityLabelsAdmin.new(@connection.getAdmin, formatter)
    end
    
    def quotas_admin(formatter)
      ::Hbase::QuotasAdmin.new(@connection.getAdmin, formatter)
    end

    def shutdown
      @connection.close
    end
  end
end
