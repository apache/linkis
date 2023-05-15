#!/usr/bin/env bash
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

#The cleaning date must be passed in. And ecm will print the actual effective date log.

end_date=`date -d '-90 days'`
end_date_t=`date -d '-90 days' +%s`
echo "Clean up data before this date:$end_date"
#Directory read ecm engine cache variables
ENGINECONN_ROOT_PATH=
for userdir in `sudo ls $ENGINECONN_ROOT_PATH -F | grep '/$'`
do
#EngineConPublicckDir cannot be cleaned up. Clean it up in the ecm stop script
  if [ "$userdir" != "engineConnPublickDir/" ];then
    for ecdir in `sudo ls $ENGINECONN_ROOT_PATH$userdir -F | grep '/$'`
    do
       ecdirpath=$ENGINECONN_ROOT_PATH$userdir$ecdir
        #Get the last modification time of the folder, and get the time of the latest modified file under the folder
       modify_time=`sudo find $ecdirpath  -printf "%TY-%Tm-%Td\n" | sort -nr | head -n 1`
       modify_time_t=`date -d "$modify_time" +%s`
       if [ $modify_time_t -lt $end_date_t ]; then
         echo "Delete directory：$ecdirpath Last update time$modify_time"
         sudo rm -rf $ecdirpath
       fi
    done
  fi
done
