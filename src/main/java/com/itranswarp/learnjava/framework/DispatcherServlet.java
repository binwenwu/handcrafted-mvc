package com.itranswarp.learnjava.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itranswarp.learnjava.controller.IndexController;
import com.itranswarp.learnjava.controller.UserController;

/**
 * MVC框架的核心分发器，负责处理所有HTTP请求并分发到对应的Controller方法
 */
@WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	// 存储GET请求URL到对应处理方法的映射
	private Map<String, GetDispatcher> getMappings = new HashMap<>();

	// 存储POST请求URL到对应处理方法的映射
	private Map<String, PostDispatcher> postMappings = new HashMap<>();

	// TODO: 可指定package并自动扫描:
	private List<Class<?>> controllers = List.of(IndexController.class, UserController.class);

	// 视图引擎，用于渲染页面
	private ViewEngine viewEngine;

	/**
	 * 当Servlet容器创建当前Servlet实例后，会自动调用init(ServletConfig)方法
	 * 初始化过程包括扫描所有Controller，解析@GetMapping和@PostMapping注解，建立URL到处理方法的映射关系
	 */
	@Override
	public void init() throws ServletException {
		logger.info("init {}...", getClass().getSimpleName());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// 依次处理每个Controller:
		for (Class<?> controllerClass : controllers) {
			try {
				Object controllerInstance = controllerClass.getConstructor().newInstance();
				// 依次处理每个Method:
				for (Method method : controllerClass.getMethods()) {
					if (method.getAnnotation(GetMapping.class) != null) {
						// 处理@Get注解的方法:
						if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
							throw new UnsupportedOperationException(
									"Unsupported return type: " + method.getReturnType() + " for method: " + method);
						}
						for (Class<?> parameterClass : method.getParameterTypes()) {
							if (!supportedGetParameterTypes.contains(parameterClass)) {
								throw new UnsupportedOperationException(
										"Unsupported parameter type: " + parameterClass + " for method: " + method);
							}
						}
						String[] parameterNames = Arrays.stream(method.getParameters()).map(p -> p.getName())
								.toArray(String[]::new);
						String path = method.getAnnotation(GetMapping.class).value();
						logger.info("Found GET: {} => {}", path, method);
						this.getMappings.put(path, new GetDispatcher(controllerInstance, method, parameterNames,
								method.getParameterTypes()));
					} else if (method.getAnnotation(PostMapping.class) != null) {
						// 处理@Post注解的方法:
						if (method.getReturnType() != ModelAndView.class && method.getReturnType() != void.class) {
							throw new UnsupportedOperationException(
									"Unsupported return type: " + method.getReturnType() + " for method: " + method);
						}
						Class<?> requestBodyClass = null;
						for (Class<?> parameterClass : method.getParameterTypes()) {
							if (!supportedPostParameterTypes.contains(parameterClass)) {
								if (requestBodyClass == null) {
									requestBodyClass = parameterClass;
								} else {
									throw new UnsupportedOperationException("Unsupported duplicate request body type: "
											+ parameterClass + " for method: " + method);
								}
							}
						}
						String path = method.getAnnotation(PostMapping.class).value();
						logger.info("Found POST: {} => {}", path, method);
						this.postMappings.put(path, new PostDispatcher(controllerInstance, method,
								method.getParameterTypes(), objectMapper));
					}
				}
			} catch (ReflectiveOperationException e) {
				throw new ServletException(e);
			}
		}
		// 创建ViewEngine:
		this.viewEngine = new ViewEngine(getServletContext());
	}

	/**
	 * 处理所有GET请求
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp, this.getMappings);
	}

	/**
	 * 处理所有POST请求
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp, this.postMappings);
	}

	/**
	 * 处理HTTP请求的核心方法
	 * 
	 * @param req 请求对象
	 * @param resp 响应对象
	 * @param dispatcherMap URL到处理器的映射
	 */
	private void process(HttpServletRequest req, HttpServletResponse resp,
			Map<String, ? extends AbstractDispatcher> dispatcherMap) throws ServletException, IOException {
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		// 获取请求路径:
		String path = req.getRequestURI().substring(req.getContextPath().length());
		// 根据路径查找处理器:
		AbstractDispatcher dispatcher = dispatcherMap.get(path);
		if (dispatcher == null) {
			// 未找到处理器，返回404错误:
			resp.sendError(404);
			return;
		}
		ModelAndView mv = null;
		try {
			// 调用Controller方法获取处理结果:
			mv = dispatcher.invoke(req, resp);
		} catch (ReflectiveOperationException e) {
			throw new ServletException(e);
		}
		if (mv == null) {
			// 处理器返回null，不需要进一步处理:
			return;
		}
		if (mv.view.startsWith("redirect:")) {
			// 处理重定向:
			resp.sendRedirect(mv.view.substring(9));
			return;
		}
		// 使用视图引擎渲染页面:
		PrintWriter pw = resp.getWriter();
		this.viewEngine.render(mv, pw);
		pw.flush();
	}

	// GET请求支持的参数类型列表
	private static final Set<Class<?>> supportedGetParameterTypes = Set.of(int.class, long.class, boolean.class,
			String.class, HttpServletRequest.class, HttpServletResponse.class, HttpSession.class);

	// POST请求支持的参数类型列表
	private static final Set<Class<?>> supportedPostParameterTypes = Set.of(HttpServletRequest.class,
			HttpServletResponse.class, HttpSession.class);
}

/**
 * 抽象分发器，定义了调用Controller方法的抽象接口
 */
abstract class AbstractDispatcher {

	public abstract ModelAndView invoke(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ReflectiveOperationException;
}

/**
 * GET请求分发器，负责解析请求参数并调用Controller方法
 */
class GetDispatcher extends AbstractDispatcher {

	final Object instance; // Controller实例
	final Method method; // Controller方法
	final String[] parameterNames; // 方法参数名称
	final Class<?>[] parameterClasses; // 方法参数类型

	public GetDispatcher(Object instance, Method method, String[] parameterNames, Class<?>[] parameterClasses) {
		super();
		this.instance = instance;
		this.method = method;
		this.parameterNames = parameterNames;
		this.parameterClasses = parameterClasses;
	}

	/**
	 * 处理GET请求，解析请求参数并调用Controller方法
	 */
	@Override
	public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ReflectiveOperationException {
		// 准备调用参数:
		Object[] arguments = new Object[parameterClasses.length];
		for (int i = 0; i < parameterClasses.length; i++) {
			String parameterName = parameterNames[i];
			Class<?> parameterClass = parameterClasses[i];
			// 根据参数类型解析参数值:
			if (parameterClass == HttpServletRequest.class) {
				arguments[i] = request;
			} else if (parameterClass == HttpServletResponse.class) {
				arguments[i] = response;
			} else if (parameterClass == HttpSession.class) {
				arguments[i] = request.getSession();
			} else if (parameterClass == int.class) {
				arguments[i] = Integer.valueOf(getOrDefault(request, parameterName, "0"));
			} else if (parameterClass == long.class) {
				arguments[i] = Long.valueOf(getOrDefault(request, parameterName, "0"));
			} else if (parameterClass == boolean.class) {
				arguments[i] = Boolean.valueOf(getOrDefault(request, parameterName, "false"));
			} else if (parameterClass == boolean.class) {
				arguments[i] = Boolean.valueOf(getOrDefault(request, parameterName, "false"));
			} else if (parameterClass == String.class) {
				arguments[i] = getOrDefault(request, parameterName, "");
			} else {
				throw new RuntimeException("Missing handler for type: " + parameterClass);
			}
		}
		// 调用Controller方法:
		return (ModelAndView) this.method.invoke(this.instance, arguments);
	}

	private String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
		String s = request.getParameter(name);
		return s == null ? defaultValue : s;
	}
}

class PostDispatcher extends AbstractDispatcher {

	final Object instance;
	final Method method;
	final Class<?>[] parameterClasses;
	final ObjectMapper objectMapper;

	public PostDispatcher(Object instance, Method method, Class<?>[] parameterClasses, ObjectMapper objectMapper) {
		this.instance = instance;
		this.method = method;
		this.parameterClasses = parameterClasses;
		this.objectMapper = objectMapper;
	}

	@Override
	public ModelAndView invoke(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ReflectiveOperationException {
		Object[] arguments = new Object[parameterClasses.length];
		for (int i = 0; i < parameterClasses.length; i++) {
			Class<?> parameterClass = parameterClasses[i];
			if (parameterClass == HttpServletRequest.class) {
				arguments[i] = request;
			} else if (parameterClass == HttpServletResponse.class) {
				arguments[i] = response;
			} else if (parameterClass == HttpSession.class) {
				arguments[i] = request.getSession();
			} else {
				BufferedReader reader = request.getReader();
				arguments[i] = this.objectMapper.readValue(reader, parameterClass);
			}
		}
		return (ModelAndView) this.method.invoke(instance, arguments);
	}
}
