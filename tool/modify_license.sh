#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cd "$(dirname $0)"
cd ..
project_path=`pwd`
echo "project path is $project_path"

java_license='''/*\
 * Licensed to the Apache Software Foundation (ASF) under one or more\
 * contributor license agreements.  See the NOTICE file distributed with\
 * this work for additional information regarding copyright ownership.\
 * The ASF licenses this file to You under the Apache License, Version 2.0\
 * (the "License"); you may not use this file except in compliance with\
 * the License.  You may obtain a copy of the License at\
 *\
 *    http://www.apache.org/licenses/LICENSE-2.0\
 *\
 * Unless required by applicable law or agreed to in writing, software\
 * distributed under the License is distributed on an "AS IS" BASIS,\
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\
 * See the License for the specific language governing permissions and\
 * limitations under the License.\
 */\
 
'''

xml_license='''<?xml version="1.0" encoding="UTF-8"?>\
<!--\
  ~ Licensed to the Apache Software Foundation (ASF) under one or more\
  ~ contributor license agreements.  See the NOTICE file distributed with\
  ~ this work for additional information regarding copyright ownership.\
  ~ The ASF licenses this file to You under the Apache License, Version 2.0\
  ~ (the "License"); you may not use this file except in compliance with\
  ~ the License.  You may obtain a copy of the License at\
  ~\
  ~    http://www.apache.org/licenses/LICENSE-2.0\
  ~\
  ~ Unless required by applicable law or agreed to in writing, software\
  ~ distributed under the License is distributed on an "AS IS" BASIS,\
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\
  ~ See the License for the specific language governing permissions and\
  ~ limitations under the License.\
  -->\
  
'''

sh_license='''# \
# Licensed to the Apache Software Foundation (ASF) under one or more\
# contributor license agreements.  See the NOTICE file distributed with\
# this work for additional information regarding copyright ownership.\
# The ASF licenses this file to You under the Apache License, Version 2.0\
# (the "License"); you may not use this file except in compliance with\
# the License.  You may obtain a copy of the License at\
#\
#    http://www.apache.org/licenses/LICENSE-2.0\
#\
# Unless required by applicable law or agreed to in writing, software\
# distributed under the License is distributed on an "AS IS" BASIS,\
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\
# See the License for the specific language governing permissions and\
# limitations under the License.\
#\

'''

#remove license of java files
remove_java_license(){
	files=`find -type f -name *.java`
	for file in $files;do 
		line_cnt=$(cat -n $file | awk '{if($2=="package")print $1}'|head -1)
		if [[ $line_cnt -gt 1 ]];then
			line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
		fi
	done
}

#remove license of scala files
remove_scala_license(){
	files=`find -type f -name *.scala`
	for file in $files;do 
		line_cnt=$(cat -n $file | awk '{if($2=="package")print $1}'|head -1)
		if [[ $line_cnt -gt 1 ]];then
			line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
		fi
	done
}

#remove license of xml files
remove_xml_license(){
	files=`find -type f -name "*.xml" |xargs grep -l "limitations under the License"`
	for file in $files;do 
		line_cnt=$(cat -n $file | grep -v '<?' |grep -v '<!' | grep -e "[0-9]*[[:space:]]*<" |head -1 | awk '{print $1}')
		if [[ $line_cnt -gt 1 ]];then
			line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
		fi
	done
}

#remove license of js files
remove_js_license(){
	files=`find -type f -name "*.js" |xargs grep -l "limitations under the License"`
	for file in $files;do 
		line_cnt=$(cat -n $file | awk '{if($2=="*/")print $1}' | head -1)
		if [[ $line_cnt -gt 1 ]];then
			#line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
			#remove the top empty line
		fi
	done
}

#remove license of scss files
remove_scss_license(){
	files=`find -type f -name "*.scss" | xargs grep -l "limitations under the License"`
	for file in $files;do 
		line_cnt=$(cat -n  $file | awk '{if($2=="*/")print $1}' | head -1)
		if [[ $line_cnt -gt 1 ]];then
			#line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
		fi
	done
}

#remove license of sh files
remove_sh_license(){
	files=`find -type f -name "*.sh" | xargs grep -l "limitations under the License"`
	for file in $files;do 
		line_cnt=$(cat -n $file | grep -A 1 "limitations under the License" |head -1 | sed "s|limitations under the License.*||" |awk '{if($3=="")print $1}' | tail -1)
		if [[ $line_cnt -gt 1 ]] && [[ $line_cnt -lt 20 ]];then
			#line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
		fi
	done
}

#remove license of properties files
remove_properties_license(){
	files=`find -type f -name "*.properties" | xargs grep -l "limitations under the License"`
	for file in $files;do 
		line_cnt=$(cat -n $file | grep -A 1 "limitations under the License" |head -1 | sed "s|limitations under the License.*||" |awk '{if($3=="")print $1}' | tail -1)
		if [[ $line_cnt -gt 1 ]] && [[ $line_cnt -lt 20 ]];then
			#line_cnt=$((line_cnt-1))
			echo "$file line_cnt=$line_cnt"
			sed -i "1,${line_cnt}d" $file
		fi
	done
}


remove_java_license
find  -type f -name "*.java" | xargs sed -i "1i $java_license"

remove_scala_license
find  -type f -name "*.scala" | xargs sed -i "1i $java_license"


remove_xml_license
find -type f -name "*.xml" | xargs sed -i "1i $xml_license"

remove_js_license
find -type f -name "*.js" |xargs sed -i "/^\r/d"
ind -type f -name "*.js" |xargs sed -i "/^$/d"
find -type f -name "*.js" | xargs sed -i "1i $java_license"

remove_scss_license
find -type f -name "*.scss" |xargs sed -i "/^\r/d"
find -type f -name "*.scss" |xargs sed -i "/^$/d"
find -type f -name "*.scss" | xargs sed -i "1i $java_license"

remove_properties_license
find -type f -name "*.properties" |xargs sed -i "/^\r/d"
find -type f -name "*.properties" |xargs sed -i "/^$/d"
find -type f -name "*.properties" |  xargs sed -i "1i $sh_license"

#add license for sql files
find  -type f -name "*.sql" | xargs sed -i "1i $java_license"

#add license for vue files
find  -type f -name "*.vue" | xargs sed -i "1i $xml_license"
find  -type f -name "*.vue" | xargs sed -i '1d'