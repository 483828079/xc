package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.PageService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest {
	@Autowired
	PageService pageService;

	@Test
	public void testGenerateHtml() throws Exception{
		String pageHtml = pageService.getPageHtml("5c3f2d0161079e278cdb5279");
		System.out.println(pageHtml);
	}
}
