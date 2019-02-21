package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;

/**
 * feign拦截器，在feign调用服务之前执行。
 */
public class FeignClientInterceptor implements RequestInterceptor {
	@Override
	public void apply(RequestTemplate requestTemplate) {
		// 使用RequestContextHolder工具获取request相关变量
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (! Objects.isNull(attributes)) {
			// 获取请求头中的jwt令牌
			HttpServletRequest request = attributes.getRequest();
			// 获取所有请求头名称
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headName = headerNames.nextElement();
				String headValue = request.getHeader(headName);
				// 取出authorization对应的jwt令牌
				if ("authorization".equals(headName)) {
					// 放入请求中,传递给下一个服务。
					requestTemplate.header(headName, headValue);
				}
			}
		}
	}
}