package com.webank.wedatasphere.linkis.metadata.hive.config;

import java.lang.annotation.*;


@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataSource {
    String name() default "";
}
