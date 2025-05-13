package com.itranswarp.learnjava.controller;

import jakarta.servlet.http.HttpSession;

import com.itranswarp.learnjava.bean.User;
import com.itranswarp.learnjava.framework.GetMapping;
import com.itranswarp.learnjava.framework.ModelAndView;

/**
 * 处理首页和问候页面的控制器
 */
public class IndexController {

    /**
     * 处理网站首页的请求
     * 
     * @param session HTTP会话，用于获取当前登录用户
     * @return 包含用户信息的首页视图
     */
    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ModelAndView("/index.html", "user", user);
    }

    /**
     * 处理问候页面的请求
     * 
     * @param name 可选的名称参数，默认为"World"
     * @return 包含名称参数的问候页面视图
     */
    @GetMapping("/hello")
    public ModelAndView hello(String name) {
        if (name == null) {
            name = "World";
        }
        return new ModelAndView("/hello.html", "name", name);
    }
}
