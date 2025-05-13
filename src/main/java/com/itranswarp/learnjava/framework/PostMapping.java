package com.itranswarp.learnjava.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识Controller方法处理HTTP POST请求的注解
 * 用于将HTTP POST请求映射到特定的Controller方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {

    /**
     * 请求路径
     * 
     * @return URL路径，如"/signin"、"/register"等
     */
    String value();

}
