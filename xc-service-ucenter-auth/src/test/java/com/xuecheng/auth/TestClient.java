package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {
    /**
     * 用来获取注册中心的服务地址
     */
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 申请令牌测试
     */
    @Test
    public void testClient(){
        // 采用客户端负载均衡，从eureka获取认证服务的ip 和端口
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        // 服务对应的ip+端口
        URI uri = serviceInstance.getUri();
        // 拼接申请令牌接口rul
        String authUrl = uri+"/auth/oauth/token";


        // 远程调用请求申请令牌
        // 请求体内容(授权码模式，用户名，密码)
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

        // 请求头内容(Authorization:客户端id:客户端密码base64编码)
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(); ;
        String httpBase = httpbasic("XcWebApp", "XcWebApp");;
        headers.add("Authorization", httpBase);
        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity =  new HttpEntity<>(body, headers);
        // 指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        // 返回的值就是错误信息，如果抛出异常就中断操作了。
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        //url 申请令牌的url /oauth/token, method http的方法类型,requestEntity请求内容,responseType,将响应的结果生成的类型
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        Map<String, String> token = exchange.getBody();
        System.out.println(token);
    }

    /**
     * base64加密 客户端id:客户端密码
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String httpbasic(String clientId,String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String userInfo = clientId+":"+clientSecret;
        byte[] encodeUserInfo = Base64.encode(userInfo.getBytes());
        return "Basic " + new String(encodeUserInfo);
    }
}