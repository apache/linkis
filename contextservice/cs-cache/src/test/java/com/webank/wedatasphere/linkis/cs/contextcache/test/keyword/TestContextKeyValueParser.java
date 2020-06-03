package com.webank.wedatasphere.linkis.cs.contextcache.test.keyword;

/**
 * @author peacewong
 * @date 2020/2/13 16:26
 */
public class TestContextKeyValueParser {

   /* @Test
    public void testParser() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.webank.wedatasphere.linkis.cs");
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
