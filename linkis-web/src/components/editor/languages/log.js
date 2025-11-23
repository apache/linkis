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
 

export default {
  register(monaco) {
    monaco.languages.register({ id: 'log' });

    monaco.languages.setMonarchTokensProvider('log', {
      tokenizer: {
        // The earlier a rule is placed, the higher its priority. If a new rule does not take effect, try moving it to the top.(规则越靠前，优先级越高，如果新配置的规则不生效，尝试提到最前面去)
        root: [
          [/^Caused by:\s.*/, 'log-caused'],
          [/(^[=a-zA-Z].*|\d\s.*)/, 'log-normal'],
          [/\sERROR\s.*/, 'log-error'],
          [/\sWARN\s.*/, 'log-warn'],
          [/\sINFO\s.*/, 'log-info'],
          [/^([0-9]{4}||[0-9]{2})-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}(.[0-9]{3})?/, 'log-date'],
          [/^[0-9]{2}\/[0-9]{2}\/[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}(.[0-9]{3})?/, 'log-date'],
          [/(^\*\*Waiting queue:.*)/, 'log-info'],
          [/(^\*\*result tips:.*)/, 'log-info'],
        ],
      },
    });
  },
};
