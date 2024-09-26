
### 1. Submitting java tasks with Restful API
```text
POST /api/rest_j/v1/entrance/submit
```

#### 1.1. Contains the class name and method

```java
package com.linkis.javassist;

import org.apache.commons.lang3.StringUtils;

public class Test {
    public void sayHello() {
        System.out.println("hello");
        System.out.println(StringUtils.isEmpty("hello"));
    }
}

```

```json
{
  "executionContent":{
    "code":"package com.linkis.javassist;\n\nimport org.apache.commons.lang3.StringUtils;\n\npublic class Test {\n    public void sayHello() {\n        System.out.println(\"hello\");\n        System.out.println(StringUtils.isEmpty(\"hello\"));\n    }\n}\n",
    "runType":"repl"
  },
  "params":{
    "configuration":{
      "runtime":{
        "linkis.repl.type":"java"
      }
    }
  },
  "labels":{
    "engineType":"repl-1",
    "userCreator":"linkis-IDE"
  }
}
```

#### 1.2. Include method only

```text
import org.apache.commons.lang3.StringUtils;

    public void sayHello() {
        System.out.println("hello");
        System.out.println(StringUtils.isEmpty("hello"));
    }
```

```json
{
  "executionContent":{
    "code":"import org.apache.commons.lang3.StringUtils;\n\n    public void sayHello() {\n        System.out.println(\"hello\");\n        System.out.println(StringUtils.isEmpty(\"hello\"));\n    }",
    "runType":"repl"
  },
  "params":{
    "configuration":{
      "runtime":{
        "linkis.repl.type":"java"
      }
    }
  },
  "labels":{
    "engineType":"repl-1",
    "userCreator":"linkis-IDE"
  }
}
```

#### 1.3. Multiple methods

```text
import org.apache.commons.lang3.StringUtils;

    public void sayHello() {
        System.out.println("hello");
        System.out.println(StringUtils.isEmpty("hello"));
    }
    public void sayHi() {
        System.out.println("hi");
        System.out.println(StringUtils.isEmpty("hi"));
    }
```

```json
{
  "executionContent":{
    "code":"import org.apache.commons.lang3.StringUtils;\n\n    public void sayHello() {\n        System.out.println(\"hello\");\n        System.out.println(StringUtils.isEmpty(\"hello\"));\n    }\n     public void sayHi() {\n        System.out.println(\"hi\");\n        System.out.println(StringUtils.isEmpty(\"hi\"));\n    }",
    "runType":"repl"
  },
  "params":{
    "configuration":{
      "runtime":{
        "linkis.repl.type":"java",
        "linkis.repl.method.name":"sayHi"
      }
    }
  },
  "labels":{
    "engineType":"repl-1",
    "userCreator":"linkis-IDE"
  }
}
```


### 2. Submitting scala tasks with Restful API

```text
import org.apache.commons.io.FileUtils
import java.io.File

val x = 2 + 3;
println(x);
FileUtils.forceMkdir(new File("/tmp/linkis_repl_scala_test"));
```

```json
{
  "executionContent":{
    "code":"import org.apache.commons.io.FileUtils\nimport java.io.File\n\nval x = 2 + 3;\nprintln(x);\nFileUtils.forceMkdir(new File(\"/tmp/linkis_repl_scala_test\"));\n",
    "runType":"repl"
  },
  "params":{
    "configuration":{
      "runtime":{
        "linkis.repl.type":"scala"
      }
    }
  },
  "labels":{
    "engineType":"repl-1",
    "userCreator":"linkis-IDE"
  }
}
```

### 3. Reference document
```text
http://www.javassist.org/tutorial/tutorial.html

https://github.com/jboss-javassist/javassist

https://github.com/apache/dubbo

https://docs.scala-lang.org/overviews/scala-book/scala-repl.html
```

