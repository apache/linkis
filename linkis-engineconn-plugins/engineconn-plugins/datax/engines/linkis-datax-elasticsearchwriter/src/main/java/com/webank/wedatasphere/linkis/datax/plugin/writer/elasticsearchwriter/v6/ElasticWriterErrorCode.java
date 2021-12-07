/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datax.plugin.writer.elasticsearchwriter.v6;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * @author davidhua
 * 2019/8/12
 */
public enum ElasticWriterErrorCode implements ErrorCode {
    /**
     * bad connection
     */
    BAD_CONNECT("ESWriter-01", "Cannot connect to Elastic server"),
    CLOSE_EXCEPTION("ESWriter-02", "Cannot close the Elastic client"),
    REQUIRE_VALUE("ESWriter-03", "Necessary value"),
    REQUEST_ERROR("ESWriter-04", "Send request error"),
    CREATE_INDEX_ERROR("ESWriter-05", "Create index error"),
    DELETE_INDEX_ERROR("ESWriter-06", "Delete index error"),
    PUT_MAPPINGS_ERROR("ESWriter-07", "Put mappings error"),
    MAPPING_TYPE_UNSUPPORTED("ESWriter-08", "Unsupported mapping type"),
    BULK_REQ_ERROR("ESWriter-09", "Bulk request error"),
    INDEX_NOT_EXIST("ESWriter-10", "Index not exist"),
    CONFIG_ERROR("ESWriter-11", "Config error");

    private final String code;
    private final String description;

    ElasticWriterErrorCode(String code, String description){
        this.code = code;
        this.description = description;
    }
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
