/*
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

package com.webank.wedatasphere.linkis.engine.flink.client.result;

import org.apache.flink.core.execution.JobClient;
import org.apache.flink.table.api.TableSchema;


public class ResultDescriptor {

	private final Result<?, ?> result;

	private boolean isChangelogResult;

	private final TableSchema resultSchema;

	private final JobClient jobClient;

	public ResultDescriptor(Result<?, ?> result, boolean isChangelogResult, TableSchema resultSchema, JobClient jobClient) {
		this.result = result;
		this.isChangelogResult = isChangelogResult;
		this.resultSchema = resultSchema;
		this.jobClient = jobClient;
	}

	public Result<?, ?> getResult() {
		return result;
	}

	public boolean isChangelogResult() {
		return isChangelogResult;
	}

	public TableSchema getResultSchema() {
		return resultSchema;
	}

	public JobClient getJobClient() {
		return jobClient;
	}

}
