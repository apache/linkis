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
package com.webank.wedatasphere.linkis.engine.impala.client.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
	ClosedError("Session is closed."),
	ExecutionError("Server report an error."),
	CommunicateError("Could not communicate with target host."),
	StillRunningError("Target is still running."),
	InvalidHandleError("Current handle is invalid."),
	ParallelLimitError("Reach the parallel limit."),
	LoginError("Failed to login to target server.");
	
	private String message;
	ExceptionCode(String message) {
		this.message = message;
	}
}

