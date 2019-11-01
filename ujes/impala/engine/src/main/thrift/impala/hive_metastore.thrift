#!/usr/local/bin/thrift -java

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#
# Thrift Service that the MetaStore is built on
#


namespace java org.apache.hadoop.hive.metastore.api

// schema of the table/query results etc.
struct Schema {
 // column names, types, comments
 1: list<FieldSchema> fieldSchemas,  // delimiters etc
 2: map<string, string> properties
}

struct FieldSchema {
  1: string name, // name of the field
  2: string type, // type of the field. primitive types defined above, specify list<TYPE_NAME>, map<TYPE_NAME, TYPE_NAME> for lists & maps
  3: string comment
}