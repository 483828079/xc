package com.xuecheng.manage_cms.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Product只需要配置Exchange。
 * 让Consumer去声明Exchange，Queue并且让siteId作为routeKey绑定。
 * 因为Product只需要发送消息给Exchange，不知道具体的Queue。
 * 每个站点对应着多个服务器，这些服务器对应着多个Queue使用routeKey绑定。
 * 所以作为cms_client肯定知道自己是哪个routeKey。要使用哪个Queue.
 * 让接收消息的声明queue通过siteId绑定Exchange。
 *
 * cms服务静态化页面，并且将html页面保存到gridFS。
 * 发送静态化页面的pageId到Exchange，routeKey为siteId。
 * cms_client，本来监听的就是cms这个站点对应的Queue。
 *
 * 也就是这些页面可能在不同站点上。每个站点有多个Queue，也就是多个client。
 * client监听的是站点对应的Queue。
 * 生产消息的一端就只有一个，静态化页面之后通知消息给所有站点对应的Queue。
 * 通过siteId将不同站点的消息发送给不同的Queue。
 */
@Configuration
public class RabbitmqConfig {
	//交换机的名称
	public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

	@Bean(EX_ROUTING_CMS_POSTPAGE)
	public Exchange EXCHANGE_TOPICS_INFORM() {
		// route类型的Exchange
		return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
	}
}
