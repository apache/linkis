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

package com.webank.wedatasphere.linkis.udf.service;

import com.webank.wedatasphere.linkis.udf.entity.UDFTree;
import com.webank.wedatasphere.linkis.udf.excepiton.UDFException;


public interface UDFTreeService {

    UDFTree initTree(String userName, String category) throws UDFException;

    UDFTree addTree(UDFTree udfTree, String userName) throws UDFException;

    UDFTree updateTree(UDFTree udfTree, String userName) throws UDFException;

    Boolean deleteTree(Long id,  String userName) throws UDFException;

    UDFTree getTreeById(Long id, String userName, String type, String category) throws UDFException;

    UDFTree getSharedTree(String category) throws UDFException;
}
