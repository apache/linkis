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

import i18n from '@/common/i18n'
const DATELANG = {
  "MIN": i18n.t('message.common.time.MIN'),
  "HOUR": i18n.t('message.common.time.HOUR'),
  "DAY": i18n.t('message.common.time.DAY'),
  "WEEK": i18n.t('message.common.time.WEEK'),
  "MONTH": i18n.t('message.common.time.MONTH'),
  "YEAR": i18n.t('message.common.time.YEAR'),
  "Mon": i18n.t('message.common.time.Mon'),
  "Tue": i18n.t('message.common.time.Tue'),
  "Wed": i18n.t('message.common.time.Wed'),
  "Thu": i18n.t('message.common.time.Thu'),
  "Fri": i18n.t('message.common.time.Fri'),
  "Sat": i18n.t('message.common.time.Sat'),
  "Sun": i18n.t('message.common.time.Sun')
}
const TIMEDATA = {
  zeroList: [],
  minuesList: [],
  hourList: [],
  weekList: [
    {value: '2', label: DATELANG.Mon},
    {value: '3', label: DATELANG.Tue},
    {value: '4', label: DATELANG.Wed},
    {value: '5', label: DATELANG.Thu},
    {value: '6', label: DATELANG.Fri},
    {value: '7', label: DATELANG.Sat},
    {value: '1', label: DATELANG.Sun}
  ],
  monthList: [],
  dayList: [],
  dayNum: {},
  selectType: [
    {value: 'MIN', label: DATELANG.MIN},
    {value: 'HOUR', label: DATELANG.HOUR},
    {value: 'DAY', label: DATELANG.DAY},
    {value: 'WEEK', label: DATELANG.WEEK},
    {value: 'MONTH', label: DATELANG.MONTH},
    {value: 'YEAR', label: DATELANG.YEAR}]
}
const baseMonthsDay = [31,29,31,30,31,30,31,31,30,31,30,31];//各月天数
for(let i = 0; i < 60; i++) {
  TIMEDATA.zeroList.push({value: i, label: i < 10 ? '0' + i : String(i)})
}
for(let i = 0; i < 60; i++) {
  TIMEDATA.minuesList.push({value: i, label: i < 10 ? '0' + i + DATELANG.MIN : String(i) + DATELANG.MIN})
}
for(let i = 0; i < 24; i++) {
  TIMEDATA.hourList.push({value: i, label: i < 10 ? '0' + i + DATELANG.HOUR : String(i) + DATELANG.HOUR})
}
for(let i = 1; i <= 12; i++) {
  TIMEDATA.monthList.push({value: i, label: i < 10 ? '0' + i + DATELANG.MONTH: String(i) + DATELANG.MONTH})
}
for(let i = 1; i <= 31; i++) {
  TIMEDATA.dayList.push({value: i, label: i < 10 ? '0' + i + DATELANG.DAY: String(i) + DATELANG.DAY})
}
for (let i = 1; i <= 12; i++) {
  let dayNum = [];
  for (let j = 1; j <= baseMonthsDay[i-1]; j++) {
    dayNum.push({value: j, label: j < 10 ? '0' + j + DATELANG.DAY : String(j) + DATELANG.DAY})
  }
  TIMEDATA.dayNum[i] = dayNum

}
import { debounce } from 'lodash';
export default {
  data() {
    return {
      zeroSelect: 0,
      oneSelect: 'MIN',
      towSelect: DATELANG.MIN,
      threeSelect: '',
      fourSelect: '',
      fiveSelect: '',
      zeroList: TIMEDATA.zeroList,
      oneList: TIMEDATA.selectType,
      towList: [
      ],
      threeList: [],
      fourList: TIMEDATA.hourList,
      fiveList: TIMEDATA.minuesList,
      selectHourAndMinues: false,
    }
  },
  computed: {
    timeToCron() {
      // Convert selected time to cron format(将所选时间转换成cron格式)
      // Still have to determine what is the first choice to decide how to write the expression(还是得先确定第一个选择的是什么来决定表达是怎么写)
      if (this.oneSelect === 'MIN') {
        return `0 0/${this.zeroSelect} * * * ?`;
      } else if (this.oneSelect === 'HOUR') {
        return `0 ${this.towSelect} 0/1 * * ?`;
      } else if (this.oneSelect === 'DAY') {
        return `0 ${this.fiveSelect} ${this.fourSelect} 1/1 * ?`;
      } else if (this.oneSelect === 'WEEK') {
        return `0 ${this.fiveSelect} ${this.fourSelect} ? * ${this.towSelect}`;
      } else if (this.oneSelect === 'MONTH') {
        return `0 ${this.fiveSelect} ${this.fourSelect} ${this.threeSelect} * ?`
      } else if (this.oneSelect === 'YEAR') {
        return `0 ${this.fiveSelect} ${this.fourSelect} ${this.threeSelect} ${this.towSelect} ? *`
      } else {
        return ''
      }
    }
  },
  watch: {
    timeToCron(val) {
      this.scheduleParams.scheduleTime = val;
      const emitFun =  debounce(() => {
        this.$emit('change-schedule', this.scheduleParams);
      }, 1000);
      emitFun(this);
    },
    towSelect(val) {
      if(this.isSelectYear) {
        this.threeList = TIMEDATA.dayNum[val];
      }
    }
  },
  methods: {
    selectOneChange(value) {
      if (value === 'MIN') {
        this.zeroSelect = this.zeroList[0].value;
        this.towList = [];
        this.threeList = [];
        this.selectHourAndMinues = false;
        this.isSelectYear = false;
      } else if (value === 'HOUR') {
        this.towList = TIMEDATA.minuesList;
        this.isSelectYear = false;
        this.towSelect = this.towList[0].value;
        this.selectHourAndMinues = false;
      } else if (value === 'DAY') {// If you choose the day two and three selection boxes not to be displayed, display the fourth and fifth selection boxes(如果选择天二三选择框不用展示，展示四五选择框)
        this.selectHourAndMinues = true;
        this.isSelectYear = false;
        this.towList = [];
        this.threeList = [];
        this.fourSelect = this.fourList[0].value;
        this.fiveSelect = this.fiveList[0].value;
      } else if (value === 'WEEK') {
        this.selectHourAndMinues = true;
        this.isSelectYear = false;
        this.towList = TIMEDATA.weekList;
        this.threeList = [];
        this.towSelect = this.towList[0].value;
        this.fourSelect = this.fourList[0].value;
        this.fiveSelect = this.fiveList[0].value;
      } else if (value === 'MONTH') {
        this.selectHourAndMinues = true;
        this.isSelectYear = false;
        this.towList = [];
        this.threeList = TIMEDATA.dayList;
        this.threeSelect = this.threeList[0].value;
        this.fourSelect = this.fourList[0].value;
        this.fiveSelect = this.fiveList[0].value;
      } else if (value === 'YEAR') {
        this.selectHourAndMinues = true;
        this.isSelectYear = true;
        this.towList = TIMEDATA.monthList;
        this.threeList = TIMEDATA.dayNum[1];
        this.towSelect = this.towList[0].value;
        this.threeSelect = this.threeList[0].value;
        this.fourSelect = this.fourList[0].value;
        this.fiveSelect = this.fiveList[0].value;
      }
    },
    alarmUserChange() {
      if (/^[a-zA-Z0-9_@]+$/.test(this.scheduleParams.alarmUserEmails)) {
        const emitFun =  debounce(() => {
          this.$emit('change-schedule', this.scheduleParams);
        }, 500);
        emitFun(this);
      }
    },
    selectLevelChange() {
      this.$emit('change-schedule', this.scheduleParams);
    },
    cronToTime() {
      if (Object.keys(this.scheduleParams).length > 0 && this.scheduleParams.scheduleTime) {
        // Convert corn format to time format 0 2 2 3 * * ?(将corn格式转换成时间格式0 2 2 3 * * ?)
        const tepArr = this.scheduleParams.scheduleTime.split(' ');
        // First determine whether to choose the year(先判断是否选的年)
        if (tepArr.length >= 7) {
          this.oneSelect = 'YEAR';
          this.selectHourAndMinues = true;
          this.isSelectYear = true;
          this.towList = TIMEDATA.monthList;
          this.threeList = TIMEDATA.dayNum[Number(tepArr[4])];
          this.towSelect = Number(tepArr[4]);
          this.threeSelect = Number(tepArr[3]);
          this.fourSelect = Number(tepArr[2]);
          this.fiveSelect = Number(tepArr[1]);
        } else if (tepArr[4] === '*' && tepArr[3] !== "*" && tepArr[3] !== "1/1" && tepArr[5] === '?') {
          this.selectHourAndMinues = true;
          this.oneSelect = 'MONTH';
          this.towList = [];
          this.threeList = TIMEDATA.dayList;
          this.threeSelect = Number(tepArr[3]);
          this.fourSelect = Number(tepArr[2]);
          this.fiveSelect = Number(tepArr[1]);
        } else if (tepArr[5] !== '?') {
          this.oneSelect = 'WEEK';
          this.selectHourAndMinues = true;
          this.towList = TIMEDATA.weekList;
          this.threeList = [];
          this.towSelect = tepArr[5];
          this.fourSelect = Number(tepArr[2]);
          this.fiveSelect = Number(tepArr[1]);
        } else if (tepArr[4] === '*' && tepArr[3] === '1/1' && tepArr[2] !== "*" && tepArr[2] !== '0/1') {
          this.oneSelect = 'DAY';
          this.selectHourAndMinues = true;
          this.towList = [];
          this.threeList = [];
          this.fourSelect = Number(tepArr[2]);
          this.fiveSelect = Number(tepArr[1]);
        } else if (tepArr[4] === '*' && tepArr[3] === '*' && tepArr[2] === '0/1') {
          this.oneSelect = 'HOUR';
          this.towList = TIMEDATA.minuesList;
          this.towSelect = Number(tepArr[1]);
        } else if (tepArr[4] === '*' && tepArr[3] === '*' && tepArr[2] === '*') {
          this.zeroSelect = Number(tepArr[1].substring(2));
          this.oneSelect = 'MIN';
          this.threeList = [];
          this.selectHourAndMinues = false;
        }
      }
    }
  }
}
