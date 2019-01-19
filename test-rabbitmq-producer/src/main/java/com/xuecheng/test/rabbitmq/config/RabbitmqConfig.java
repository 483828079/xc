package com.xuecheng.test.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
	public static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
	public static final String QUEUE_INFORM_SMS = "queue_inform_sms";
	public static final String ROUTEKEY_EMAIL = "inform.#.email.#";
	public static final String ROUTEKEY_SMS = "inform.#.sms.#";
	public static final String EXCHANGE_TOPICS_INFORM="exchange_topics_inform";

	// 声明Exchange
	// 在容器中叫exchange_topics_inform，这里还可以写变量。。。
	// 其实不用这样写，因为类型就是bean在容器中的名称。
	@Bean(EXCHANGE_TOPICS_INFORM)
	public Exchange EXCHANGE_TOPICS_INFORM() {
		// topic类型的Exchange。用的exchange_topics_inform名称。
		// durable(true) 持久化Exchange，重启服务后Exchange还会存在。
		return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_INFORM).durable(true).build();
	}

	// 声明Queue
	// bean在容器中的名称为queue_inform_email
	@Bean(QUEUE_INFORM_EMAIL)
	public Queue QUEUE_INFORM_EMAIL() {
		// 声明队列，队列的名称为queue_inform_email
		return new Queue(QUEUE_INFORM_EMAIL);
	}

	// bean在容器中的名称为queue_inform_sms
	@Bean(QUEUE_INFORM_SMS)
	public Queue QUEUE_INFORM_SMS() {
		// 声明队列，队列的名称为queue_inform_sms
		return new Queue(QUEUE_INFORM_SMS);
	}

	// 绑定Queue
	@Bean
	public Binding BINDING_QUEUE_INFORM_EMAIL(@Qualifier(QUEUE_INFORM_EMAIL) Queue queue,
											  @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange) {
		// 绑定Queue，使用inform.#.email.#的routeKey
		return BindingBuilder.bind(queue).to(exchange).with(ROUTEKEY_EMAIL).noargs();
	}

	// 绑定Queue
	@Bean
	public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_INFORM_SMS) Queue queue,
											  @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange) {
		// 绑定Queue，使用inform.#.sms.#的routeKey
		return BindingBuilder.bind(queue).to(exchange).with(ROUTEKEY_SMS).noargs();
	}
}
