package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 过滤器,需要继承ZuulFilter抽象类,并将其注册到容器中。
 */
@Component
public class LoginFilter extends ZuulFilter {
	@Autowired
	AuthService authService;
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
	 * 路由之前执行该方法。
	 * 判断请求头中是否包含jwt令牌
	 * 判断cookie中是否包含身份令牌
	 * 判断redis中的令牌信息是否过期
	 * 			使用cookie中的身份令牌获取
	 * 			(同样能够判断cookie中的身份令牌是否和redis中的令牌信息匹配)
	 * 不符合一项就不允许访问，响应不允许访问的原因回去。
	 * @return
	 * @throws ZuulException
	 */
	public Object run() throws ZuulException {
		// 上下文对象
		RequestContext requestContext = RequestContext.getCurrentContext();
		// 请求对象
		HttpServletRequest req = requestContext.getRequest();
		// 获取cookie中的身份令牌
		String accessToken  = authService.getTokenFromCookie(req);
		// 如果身份令牌不存在
		if (StringUtils.isEmpty(accessToken)) {
			// 不允许访问，并且设置响应信息
			this.access_denied();
			return null;
		}

		// 获取redis中的令牌存活时间
		long tokenExpire = authService.getExpire(accessToken);
		// redis中的令牌已经过期
		if (tokenExpire < 0) {
			// 不允许访问，并且设置响应信息
			this.access_denied();
			return null;
		}

		// 获取请求头中的jwt令牌
		String jwtToken = authService.getJwtFromHeader(req);
		// 如果请求头中不包含jwt令牌
		if (StringUtils.isEmpty(jwtToken)) {
			// 不允许访问，并且设置响应信息
			this.access_denied();
			return null;
		}
		return null;
	}


	/**
	 * 拒绝路由
	 * 设置响应状态为200，设置响应信息，设置响应的mime类型和响应编码。
	 */
	private void access_denied(){
		// 上下文对象
		RequestContext requestContext = RequestContext.getCurrentContext();
		requestContext.setSendZuulResponse(false);//拒绝访问

		// 设置响应内容
		ResponseResult responseResult =new ResponseResult(CommonCode.UNAUTHENTICATED);
		String responseResultString = JSON.toJSONString(responseResult);
		requestContext.setResponseBody(responseResultString);

		// 设置状态码
		requestContext.setResponseStatusCode(200);

		// 设置响应mime类型和编码格式
		HttpServletResponse response = requestContext.getResponse();
		response.setContentType("application/json;charset=utf-8");
	}
}