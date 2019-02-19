package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

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
        if (Objects.isNull(loginRequest)) {
            loginRequest = new LoginRequest();
        }

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

    /**
     * 退出登录，删除cookie中的认证令牌，删除redis中的令牌
     * @return 返回响应状态
     */
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        // 删除redis中的令牌信息
        // 身份令牌
        String accessToken = this.getTokenFormCookie();
        authService.delToken(accessToken);

        // 删除cookie中的身份令牌
        clearCookie(accessToken);

        // 无论有没有要删除的令牌,只要执行完毕都返回正确状态。
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 通过cookie中记录的身份令牌获取redis中的jwt令牌
     * @return jwt令牌
     */
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        // 获取cookie中的令牌
        String accessToken = getTokenFormCookie();
        // 根据身份令牌获取redis中的令牌信息
        AuthToken authToken = authService.getUserToken(accessToken);
        if (Objects.isNull(authToken)) {
            return new JwtResult(CommonCode.FAIL, null);
        }

        // 返回页面jwt令牌
        return new JwtResult(CommonCode.SUCCESS, authToken.getJwt_token());
    }

    /**
     * 将身份令牌添加到cookie
     * @param token
     */
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


    /**
     *  删除cookie中的身份令牌(实际是设置存活时间为0)
     * @param token
     */
    private void clearCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
    }

    /**
     * 获取cookie中存储的认证令牌
     * @return 如果获取不到认证令牌返回null
     */
    private String getTokenFormCookie() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // readCookie 获取多个cookieName对应的cookieName和cookieValue
        Map<String, String> cookieMap = CookieUtil.readCookie(req, "uid");
        if (!Objects.isNull(cookieMap)) {
            return cookieMap.get("uid");
        }
        return null;
    }
}