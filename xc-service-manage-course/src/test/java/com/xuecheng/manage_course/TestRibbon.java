package com.xuecheng.manage_course;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRibbon {
	@Autowired
	RestTemplate restTemplate;

	@Test
	public void testRibbon() throws Exception{
		//服务id
		String serviceId = "XC-SERVICE-MANAGE-CMS";

		// 使用okHttp请求cms服务。
		// restTemplate上加了@LoadBalanced，可以使用服务名称代替ip+端口。
		// 服务名称spring.application.name: xc-service-manage-course
		// 对应的应该是服务注册到eureka的ip地址+端口号。
		// ribbon会在EurekaService拿到服务名称对应的列表，使用负载均衡算法
		// 计算出本次要使用的地址。
		ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://" + serviceId + "/cms/page/get/5a754adf6abb500ad05688d9", Map.class);
		Map map = forEntity.getBody();
		System.out.println(map);
	}
}
