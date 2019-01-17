package com.xuecheng.manage_cms;

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
public class RestTemplateTest {
	@Autowired
	RestTemplate restTemplate;

	@Test
	public void testRestTemplate() throws Exception{
		ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
		System.out.println(forEntity);

		/*
		* forEntity:
		* 	status 200
		*   headers	响应头
		*   body 响应体，响应回来的数据。
		* */
	}
}
