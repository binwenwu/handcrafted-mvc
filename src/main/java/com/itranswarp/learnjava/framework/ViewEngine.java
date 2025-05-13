package com.itranswarp.learnjava.framework;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.ServletContext;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.Servlet5Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * 视图引擎，负责渲染模板，将模型数据填充到视图模板中
 * 使用Pebble作为模板引擎
 */
public class ViewEngine {

	/**
	 * Pebble模板引擎实例
	 */
	private final PebbleEngine engine;

	/**
	 * 创建ViewEngine实例
	 * 
	 * @param servletContext Servlet上下文，用于初始化模板引擎
	 */
	public ViewEngine(ServletContext servletContext) {
		var loader = new Servlet5Loader(servletContext);
		loader.setCharset("UTF-8");
		loader.setPrefix("/WEB-INF/templates");
		loader.setSuffix("");
		this.engine = new PebbleEngine.Builder().autoEscaping(true).cacheActive(false) // no cache for dev
				.loader(loader).build();
	}

	/**
	 * 渲染视图
	 * 
	 * @param mv ModelAndView对象，包含视图路径和模型数据
	 * @param writer 输出Writer
	 * @throws IOException 渲染过程中发生IO错误时抛出
	 */
	public void render(ModelAndView mv, Writer writer) throws IOException {
		PebbleTemplate template = this.engine.getTemplate(mv.view);
		template.evaluate(writer, mv.model);
	}
}
