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

/**
 * 任务
 */
class Nav {
  /**
     * 构造器
     * @param {*} option 
     */
  constructor(option) {
    // 侧边栏type
    this.barType = option.barType;
    // 显示nav icon list
    this.navList = option.navList;
  }
  /**
     * @param {*} type
     * @return {boolean}
     * @memberof Nav
     */
  isShowNav(type) {
    return this.navList.indexOf(type) !== -1;
  }
}

export default Nav;
