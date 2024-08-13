/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.contextcache.test.keyword;

public class TestContextKeyValueParser {

  /* @Test
  public void testParser() {

      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.apache.linkis.cs");
      System.out.println("ioc容器加载完成");
      ContextKeyValueParser contextKeyValueParser = context.getBean(ContextKeyValueParser.class);
      ContextKey contextKey1 = new TestContextKey();
      contextKey1.setKey("key1");
      contextKey1.setKeywords("keyword1,keyword2,keyword3");

      ContextValue contextValue1 = new TestContextValue();
      contextValue1.setKeywords("keyword4-keyword5-keyword6");
      contextValue1.setValue("hello,hello2");
      ContextKeyValue contextKeyValue1 = new TestContextKeyValue();
      contextKeyValue1.setContextKey(contextKey1);
      contextKeyValue1.setContextValue(contextValue1);
      List<ContextKeyValue> contextKeyValueList = new ArrayList<>();
      contextKeyValueList.add(contextKeyValue1);
      Set<String> parse = contextKeyValueParser.parse(contextKeyValue1);
      parse.stream().forEach(System.out::println);
  }

  public List<ContextKeyValue> testGetKeyValues(){
      List<ContextKeyValue> contextKeyValueList = new ArrayList<>();
      ContextKey contextKey1 = new TestContextKey();
      contextKey1.setKey("key1");
      contextKey1.setKeywords("keyword1,keyword2,keyword3");

      ContextValue contextValue1 = new TestContextValue();
      contextValue1.setKeywords("keyword4-keyword5-keyword6");
      contextValue1.setValue("hello,hello2");
      ContextKeyValue contextKeyValue1 = new TestContextKeyValue();
      contextKeyValue1.setContextKey(contextKey1);
      contextKeyValue1.setContextValue(contextValue1);

      contextKeyValueList.add(contextKeyValue1);

      ContextKey contextKey2 = new TestContextKey();
      contextKey2.setKey("key2");
      contextKey2.setKeywords("keywordd1,keywordd2,keywordd3");

      ContextValue contextValue2 = new TestContextValue();
      contextValue1.setKeywords("keywordd4-keywordd5-keywordd6");
      contextValue1.setValue("hello,hello2");
      ContextKeyValue contextKeyValue2 = new TestContextKeyValue();
      contextKeyValue1.setContextKey(contextKey2);
      contextKeyValue1.setContextValue(contextValue2);
      contextKeyValueList.add(contextKeyValue2);
      return contextKeyValueList;
  }*/
}
