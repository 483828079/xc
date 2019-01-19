package com.xuecheng.test.rabbitmq;

import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_topics_springboot {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /***
     * 只需要在application.xml配置连接的信息。
     * 然后把Exchange声明，Queue声明，绑定。再放入容器。
     * 就可以使用rabbitTemplate发送消息。
     * 指定发送消息的Exchange和routeKey。
     */
    @Test
    public void testSendByTopics(){
        for (int i=0;i<5;i++){
            String message = "sms email inform to user"+i;
            rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM,"inform.sms.email",message);
            System.out.println("Send Message is:'" + message + "'");
        }
    }
}
