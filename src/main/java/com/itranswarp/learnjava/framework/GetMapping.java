package com.itranswarp.learnjava.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识Controller方法处理HTTP GET请求的注解
 * 用于将HTTP GET请求映射到特定的Controller方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetMapping {

    /**
     * 请求路径
     * 
     * @return URL路径，如"/"、"/hello"等
     */
    String value();

}
