package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController implements AuthControllerApi {
    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    AuthService authService;


    /**
     * 对登录用户进行认证
     * @param loginRequest 用户名密码验证码
     * @return 返回登录状态和访问token
     */
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        loginRequest = new LoginRequest();
        // 是否录入用户名
        if (StringUtils.isEmpty(loginRequest.getUsername())) {
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }

        // 是否录入密码
        if (StringUtils.isEmpty(loginRequest.getPassword())) {
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        // 通过用户名密码，客户端id客户端密码获取token信息
        AuthToken authToken = authService.login(loginRequest.getUsername(),
                                                loginRequest.getPassword(),
                                                clientId, clientSecret);
        // 将访问token写入token
        String accessToken = authToken.getAccess_token();
        addCookie(accessToken);

        // 返回状态和访问token
        return new LoginResult(CommonCode.SUCCESS,accessToken);
    }

    private void addCookie(String token) {
        HttpServletResponse resp = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        // cookieDomain 绑定的域名
        // path 路径 域名+路径 cookie绑定的范围。 这里是 *.xuecheng.com/都可以获取到。
        // name cookieName
        // token cookieValue
        // cookieMaxAge 有效时间
        // httpOnly false 允许浏览器获取
        CookieUtil.addCookie(resp, cookieDomain, "/", "uid", token, cookieMaxAge, false);
    }


    public ResponseResult logout() {
        return null;
    }
}