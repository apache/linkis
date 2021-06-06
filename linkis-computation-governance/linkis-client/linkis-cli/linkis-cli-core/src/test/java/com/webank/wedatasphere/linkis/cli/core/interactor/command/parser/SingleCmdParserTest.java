/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.core.interactor.command.parser;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.TestCmdType;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter.SingleTplFitter;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.result.ParseResult;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.template.TestParamMapper;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.template.TestSparkCmdTemplate;
import com.webank.wedatasphere.linkis.cli.core.utils.CommonUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class SingleCmdParserTest {
    String[] cmdStr;
    Map<String, CmdTemplate> templateMap;

    @Before
    public void before() throws Exception {
        cmdStr = new String[]{"-u", "hadoop",
                "-pwd", "1234",
                "-c", "/path/to/user/config",
                "--cmd", "show tables",
                "--split", "\',\'",
                "--queue", "q05",
                "--name", "testApp",
//      "--hiveconf", "/path/...",
                "--num-executors", "4",
                "--executor-cores", "4",
                "--executor-memory", "4G",
                "--shuffle-partitions", "200",
                "--other", "--other-spark-config=none",
        };

        TestSparkCmdTemplate template = new TestSparkCmdTemplate();
        templateMap = new HashMap<>();
        templateMap.put(template.getCmdType().getName(), template);

    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: parse(String[] input)
     */
    @Test
    public void testParse() throws Exception {

        Parser parser = new SingleCmdParser()
                .setMapper(null)
                .setTemplate(templateMap.get(TestCmdType.SPARK.getName()))
                .setFitter(new SingleTplFitter())
                .setMapper(new TestParamMapper());

        ParseResult result = parser.parse(cmdStr);
        System.out.println(result.getParams().getCmdType());
        System.out.println(CommonUtils.GSON.toJson(result.getParams()));
        System.out.println(CommonUtils.GSON.toJson(result.getRemains()));
    }


    /**
     * Method: parsePrimary(String[] input)
     */
    @Test
    public void testParsePrimary() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = SingleCmdParser.getClass().getMethod("parsePrimary", String[].class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: parseSingleSub(String[] remains)
     */
    @Test
    public void testParseSingleSub() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = SingleCmdParser.getClass().getMethod("parseSingleSub", String[].class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: standardParse(String identifier, String[] args, CmdTemplate templateOri)
     */
    @Test
    public void testStandardParse() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = SingleCmdParser.getClass().getMethod("standardParse", String.class, String[].class, CmdTemplate.class);
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 
