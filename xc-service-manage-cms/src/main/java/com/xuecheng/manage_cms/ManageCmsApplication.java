package com.xuecheng.manage_cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/*
 * 扫描当前包及其子包下的所有@Component,并放入容器中。
 */
@SpringBootApplication
/*
 * 用来扫描mongodb的实体类。
 * */
@EntityScan(basePackages = "com.xuecheng.framework.domain.cms")//扫描实体类
/*
 * 扫描com.xuecheng.api包下的所有@Component，有配置相关的类(Swagger2)。
 * */
@ComponentScan(basePackages = {"com.xuecheng.api"})//扫描接口
/*
 * 扫描全局异常类
 */
@ComponentScan(basePackages = "com.xuecheng.framework.exception")
/*
 * 扫描本工程，其实没必要，不过为了看上去直观加上去。
 * */
@ComponentScan(basePackages = {"com.xuecheng.manage_cms"})//扫描本项目下的所有类
public class ManageCmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(ManageCmsApplication.class, args);
	}

	/**
	 * 注册resultTemplate到容器。
	 * SpringMVC提供了ResultTemplate请求http接口。
	 * 这里使用的是OkHttpClient的实现。
	 * @return
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
	}
}