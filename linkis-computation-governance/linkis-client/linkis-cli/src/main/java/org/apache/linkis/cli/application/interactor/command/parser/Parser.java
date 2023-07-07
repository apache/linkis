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

package org.apache.linkis.cli.application.interactor.command.parser;

import org.apache.linkis.cli.application.entity.command.Params;
import org.apache.linkis.cli.application.interactor.command.parser.result.ParseResult;

/**
 * 1. parse cmd arguments and fill into template 2. generate unique identifier for sub command 3.
 * transform parsed template into instance of {@link Params} 4. return parsed copy of template for
 * further validation, and commandParam for submitting to backend
 */
public interface Parser {
  ParseResult parse(String[] input);
}
