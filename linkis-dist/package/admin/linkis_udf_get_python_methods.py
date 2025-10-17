#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import sys
import ast
import json

def extract_method_names(file_path):
    with open(file_path, 'r') as file:
        code = file.read()
    tree = ast.parse(code)
    method_names = set()
    
    for node in ast.walk(tree):
        if isinstance(node, ast.FunctionDef):
            method_names.add(node.name)
    
    return json.dumps(list(method_names), indent=4)

file_path = sys.argv[1]
print(extract_method_names(file_path))
