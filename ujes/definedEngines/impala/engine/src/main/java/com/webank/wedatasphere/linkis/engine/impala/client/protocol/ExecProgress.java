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
package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ExecProgress {
	private long totalScanRanges;
	private long completedScanRanges;

	/**
	 * @param totalScanRanges
	 * @param completedScanRanges
	 */
	public ExecProgress(long totalScanRanges, long completedScanRanges) {
		super();
		this.totalScanRanges = totalScanRanges;
		this.completedScanRanges = completedScanRanges;
	}


	public long getTotalScanRanges() {
		return totalScanRanges;
	}

	public long getCompletedScanRanges() {
		return completedScanRanges;
	}
}
