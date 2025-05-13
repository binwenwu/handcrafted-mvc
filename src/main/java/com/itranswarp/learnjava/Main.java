package com.itranswarp.learnjava;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * 应用程序入口类，负责启动内嵌的Tomcat服务器
 */
public class Main {

   
    public static void main(String[] args) throws Exception {
        // 创建Tomcat服务器实例
        Tomcat tomcat = new Tomcat();
        // 设置Tomcat端口，默认为8080
        tomcat.setPort(Integer.getInteger("port", 8080));
        // 初始化连接器
        tomcat.getConnector();
        // 添加Web应用，根路径为空字符串，对应webapp目录
        Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        // 创建Web资源根目录
        WebResourceRoot resources = new StandardRoot(ctx);
        // 添加编译后的class文件目录到Web应用
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);
        // 启动Tomcat服务器
        tomcat.start();
        // 等待服务器接收请求，不退出主线程
        tomcat.getServer().await();
    }
}
