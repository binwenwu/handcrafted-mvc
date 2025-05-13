# Simple MVC Framework

A lightweight MVC framework implemented from scratch to understand the core concepts of web frameworks.

## Overview

This project is a simplified MVC framework implementation that demonstrates the fundamental concepts of web frameworks. It includes:

- Request dispatching
- Annotation-based routing
- View rendering with Pebble template engine
- Session management
- JSON processing

## Features

- `@GetMapping` and `@PostMapping` annotations for route mapping
- Automatic parameter binding
- Template-based view rendering
- Session management
- JSON request/response handling

## Tech Stack

- Java 8+
- Servlet API
- Embedded Tomcat
- Pebble Template Engine
- Jackson
- Maven

## Quick Start

### Option 1: Run in IDE
1. Clone the repository
2. Open the project in your IDE (e.g., IntelliJ IDEA, Eclipse)
3. Run `Main.java` directly

### Option 2: Deploy to Web Server
1. Clone the repository
2. Build with Maven: `mvn clean package`
3. Deploy the generated WAR file to your web server (e.g., Tomcat)
4. Access the application at `http://localhost:8080`

## Documentation

For detailed documentation and implementation details, please visit my blog post:
[从零开始手搓一个MVC框架](https://www.tankenqi.cn/posts/56)

## License

MIT License
