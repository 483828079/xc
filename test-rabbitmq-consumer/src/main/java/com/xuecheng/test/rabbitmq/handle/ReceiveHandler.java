package com.xuecheng.test.rabbitmq.handle;

import com.rabbitmq.client.Channel;
import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 *  配置连接信息。
 *  声明Exchange，声明Queue。绑定。注册到容器。
 * 然后，只要使用@RabbitListener注解表名要监听的Queue名称。
 *  就会调用标注@RabbitListener的方法传入参数。
 *  注入类到容器。启动服务。Queue中有消息就能够获取到。
 */
@Component
public class ReceiveHandler {

    /**
     * 监听email队列
     * @param msg 转换为String的消息体。
     * @param message Message，消息体和参数。
     * @param channel channel 通道，代表当前会话。
     */
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})
    public void receive_email(String msg,Message message,Channel channel){
        System.out.println(msg);
    }

    //监听sms队列
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_SMS})
    public void receive_sms(String msg,Message message,Channel channel){
        System.out.println(msg);
    }
}