package com.xuecheng.manage_course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Administrator
 * @version 1.0
 **/
/*
开启feign客户端,开启之后能生成接口代理对象。
 */
@EnableFeignClients
/*
声明该类是eureka的客户端，能从eurekaService上发现服务。
 */
@EnableDiscoveryClient
@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.course")//扫描实体类
@ComponentScan(basePackages={"com.xuecheng.api"})//扫描接口
@ComponentScan(basePackages={"com.xuecheng.manage_course"})
@ComponentScan(basePackages={"com.xuecheng.framework"})//扫描common下的所有类
public class ManageCourseApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ManageCourseApplication.class, args);
    }


    @Bean
    /*
        开启客户端负载均衡。ribbon的作用就是客户端的负载均衡。
        在客户端拿到要调用服务的列表然后负载均衡算法算出要调用哪个。

        feign的本质还是从Eureka拿到服务列表的ip+端口，
        通过requestMapping拿到请求地址和请求方式最后生成代理对象，可以根据代理对象调用服务。

        如果Eureka拿到的是多个服务的ip，可以给restTemplate加上@LoadBalanced。
        让其允许服务端的负载均衡，每次调用服务使用负载均衡算法计算要调用哪个服务。
     */
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }
}
