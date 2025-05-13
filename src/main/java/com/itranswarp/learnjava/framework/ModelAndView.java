package com.itranswarp.learnjava.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示MVC模式中的Model和View
 * 包含视图路径和模型数据，用于Controller向视图传递数据
 */
public class ModelAndView {

	/**
	 * 视图的路径，如"/index.html"
	 */
	public final String view;

	/**
	 * 存储模型数据的Map
	 */
	public final Map<String, Object> model;

	/**
	 * 创建一个只包含视图路径的ModelAndView
	 * 
	 * @param view 视图路径
	 */
	public ModelAndView(String view) {
		this.view = view;
		this.model = new HashMap<>();
	}

	/**
	 * 创建一个包含视图路径和一个模型数据的ModelAndView
	 * 
	 * @param view 视图路径
	 * @param name 模型数据的名称
	 * @param value 模型数据的值
	 */
	public ModelAndView(String view, String name, Object value) {
		this.view = view;
		this.model = new HashMap<>();
		this.model.put(name, value);
	}

	public ModelAndView(String view, Map<String, Object> model) {
		this.view = view;
		this.model = new HashMap<>(model);
	}
}
