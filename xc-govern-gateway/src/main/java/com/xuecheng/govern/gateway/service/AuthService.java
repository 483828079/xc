package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 查询cookie中的身份令牌
     * @param request
     * @return 如果身份令牌存在cookie中返回身份令牌，否则返回null。
     */
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    /**
     * 获取请求头中的jwt令牌
     * @param request httpServletRequest
     * @return 如果请求头中有jwt令牌返回jwt令牌，如果没有返回null。
     */
    public String getJwtFromHeader(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒绝访问
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            //拒绝访问
            return null;
        }
        return authorization;
    }

    /**
     * 通过身份令牌查询redis中的令牌信息
     * @param access_token
     * @return 如果令牌过期返回负数，没有过期返回正数。
     */
    public long getExpire(String access_token) {
        //token在redis中的key
        String key = "user_token:" + access_token;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }
}
