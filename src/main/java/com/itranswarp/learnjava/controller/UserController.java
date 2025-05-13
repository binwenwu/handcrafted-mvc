package com.itranswarp.learnjava.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.itranswarp.learnjava.bean.SignInBean;
import com.itranswarp.learnjava.bean.User;
import com.itranswarp.learnjava.framework.GetMapping;
import com.itranswarp.learnjava.framework.ModelAndView;
import com.itranswarp.learnjava.framework.PostMapping;

/**
 * 用户管理控制器，负责处理用户登录、登出和个人资料等功能
 */
public class UserController {

    /**
     * 模拟用户数据库，存储预设的用户信息
     */
    private Map<String, User> userDatabase = new HashMap<>() {
        {
            List<User> users = List.of( //
                    new User("bob@example.com", "bob123", "Bob", "This is bob."), 
                    new User("tom@example.com", "tomcat", "Tom", "This is tom."));
            users.forEach(user -> {
                put(user.email, user);
            });
        }
    };

    /**
     * 显示登录页面
     * 
     * @return 登录页面视图
     */
    @GetMapping("/signin")
    public ModelAndView signin() {
        return new ModelAndView("/signin.html");
    }

    /**
     * 处理用户登录请求
     * 
     * @param bean 包含用户登录信息的Bean对象
     * @param response HTTP响应对象，用于返回JSON结果
     * @param session HTTP会话，用于存储用户信息
     * @return 登录成功返回null，由响应对象直接输出JSON结果
     * @throws IOException 当写入响应失败时抛出异常
     */
    @PostMapping("/signin")
    public ModelAndView doSignin(SignInBean bean, HttpServletResponse response, HttpSession session) throws IOException {
        User user = userDatabase.get(bean.email);
        if (user == null || !user.password.equals(bean.password)) {
            // 登录失败，返回错误信息
            response.setContentType("application/json");
            PrintWriter pw = response.getWriter();
            pw.write("{\"error\":\"Bad email or password\"}");
            pw.flush();
        } else {
            // 登录成功，将用户信息存入会话
            session.setAttribute("user", user);
            response.setContentType("application/json");
            PrintWriter pw = response.getWriter();
            pw.write("{\"result\":true}");
            pw.flush();
        }
        return null;
    }

    /**
     * 处理用户登出请求
     * 
     * @param session HTTP会话，用于移除用户信息
     * @return 重定向到首页
     */
    @GetMapping("/signout")
    public ModelAndView signout(HttpSession session) {
        session.removeAttribute("user");
        return new ModelAndView("redirect:/");
    }

    /**
     * 显示用户个人资料页面
     * 
     * @param session HTTP会话，用于获取当前登录用户信息
     * @return 用户资料页面视图，若未登录则重定向到登录页
     */
    @GetMapping("/user/profile")
    public ModelAndView profile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ModelAndView("redirect:/signin");
        }
        return new ModelAndView("/profile.html", "user", user);
    }
}
