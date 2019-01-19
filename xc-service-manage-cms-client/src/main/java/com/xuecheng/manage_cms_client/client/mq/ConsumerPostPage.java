package com.xuecheng.manage_cms_client.client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ConsumerPostPage {
	@Autowired
	PageService pageService;
	@Autowired
	CmsPageRepository cmsPageRepository;
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);

	@RabbitListener(queues={"${xuecheng.mq.queue}"})
	public void postPage(String msg){

		// 消息为{"pageId":""}  使用json字符串便于以后的扩充。
		// 解析消息
		Map<String, String> map = JSON.parseObject(msg, Map.class);
		String pageId = map.get("pageId");
		Optional<CmsPage> cmsPage = cmsPageRepository.findById(pageId);
		if (!cmsPage.isPresent()) {
			LOGGER.error("receive cms post page,cmsPage is null:{}",msg.toString());
			return ;
		}

		// 根据pageId去mongodb中取html页面，然后保存到page所在物理路径。
		pageService.savePageToServerPath(pageId);
	}
}