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
 *
 */

const hql = [
  /Truncate\s*Table/i,
  /Drop\s*Table/i,
  /Drop\s*Function/i,
  /Drop\s*Database/i,
  /ALTER\s*DATABASE/i,
];

const python = [
  /sys/i,
  /os/i,
  /sc\.stop/i,
  /spark\.stop/i,
];

const scala = [
  /sc\.stop/i,
  /spark\.stop/i,
];

export default {
  hql,
  python,
  scala,
};
