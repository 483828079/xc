package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 过滤器,需要继承ZuulFilter抽象类,并将其注册到容器中。
 */
//@Component
public class LoginFilterTest extends ZuulFilter {
	/**
	 * 返回字符串代表过滤器的类型
	 * pre：请求在被路由之前执行
	 * routing：在路由请求时调用
	 * post：在routing和error过滤器之后调用
	 * error：处理请求时发生错误调用
	 * @return
	 */
	public String filterType() {
		return "pre";
	}

	/**
	 * 此方法返回整型数值，通过此数值来定义过滤器的执行顺序，
	 * 数字越小优先级越高。
	 * @return
	 */
	public int filterOrder() {
		return 2;
	}

	/**
	 * 返回一个Boolean值，判断该过滤器是否需要执行。
	 * 返回true表示要执行此过虑器，否则不执行。
	 * @return
	 */
	public boolean shouldFilter() {
		return true;
	}

	/**
	 * 过滤器的业务逻辑。
	 * @return
	 * @throws ZuulException
	 */
	public Object run() throws ZuulException {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest req = requestContext.getRequest();
		HttpServletResponse resp = requestContext.getResponse();
		// 取出请求头中的信息
		String authorization = req.getHeader("Authorization");
		// 如果请求头中的Authorization不存在
		if (StringUtils.isEmpty(authorization)) {
			// 拒绝访问
			requestContext.setSendZuulResponse(false);
			// 设置响应的状态码
			requestContext.setResponseStatusCode(200);
			// 设置响应体
			ResponseResult unauthenticated = new ResponseResult(CommonCode.UNAUTHENTICATED);
			requestContext.setResponseBody(JSON.toJSONString(unauthenticated));
			// 设置响应mime类型和编码格式
			resp.setContentType("application/json;charset=utf-8");
			return null;
		}
		return null;
	}
}