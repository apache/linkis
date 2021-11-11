/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.engineplugin.python.exception

import org.apache.linkis.common.exception.ErrorException


class PythonSessionNullException (errCode: Int, desc: String) extends ErrorException(errCode, desc)

class PythonEngineException (errCode: Int, desc: String) extends ErrorException(errCode, desc)

class QueryFailedException (errCode: Int=60001, desc: String) extends ErrorException(errCode, desc)

class SessionStartFailedException (errCode: Int=60002, desc: String) extends ErrorException(errCode, desc)

class ExecuteException(errCode: Int=60003, desc: String) extends ErrorException(errCode, desc)

class EngineException(errCode: Int=60004, desc: String) extends ErrorException(errCode, desc)

class PythonExecuteError (errCode: Int, desc: String) extends ErrorException(errCode, desc){

}