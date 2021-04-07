// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

namespace java org.apache.impala.thrift

include "ErrorCodes.thrift"

// NOTE: The definitions in this file are part of the binary format of the Impala query
// profiles. They should preserve backwards compatibility and as such some rules apply
// when making changes. Please see RuntimeProfile.thrift for more details.

struct TStatus {
  1: required ErrorCodes.TErrorCode status_code
  2: list<string> error_msgs
}
