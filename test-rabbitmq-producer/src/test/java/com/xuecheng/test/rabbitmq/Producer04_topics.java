package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Topic
 *  声明的routeKey可以和发送消息时使用的routeKey不一样。
 *  可以声明通配符的RouteKey，在发送的时候可以进行通配确认要发送到的Queue。
 *
 *  中间以“.”分隔。
 *  符号#可以匹配多个词，符号*可以匹配一个词语。
 *  比如
 *     声明队列的时候使用该routeKey对Queue进行绑定。
 *     private static final String ROUTEKEY_EMAIL = "inform.#.email.#";
 *     private static final String ROUTEKEY_SMS = "inform.#.sms.#";
 *
 *      发送消息的时候使用inform.email 能够匹配到第一个。
 *      发送消息的时候使用inform.sms 能够匹配到第二个。
 *      发送消息的时候使用inform.sms.email 两个两个都能匹配到，也就是两个Queue都能接收到。
 *
 *  比起Route方式更加灵活，因为routeKey可以使用通配的方式。
 *  原来要设置多个Queue绑定不同的routeKey才能够实现的功能可以直接使用Topic方式实现。
 *
 *  就比Route多改了一个Exchange的类型。多了绑定的时候可以设置通配的RouteKey。
 */
public class Producer04_topics {
    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String ROUTEKEY_EMAIL = "inform.#.email.#";
    private static final String ROUTEKEY_SMS = "inform.#.sms.#";
    private static final String EXCHANGE_TOPICS_INFORM="exchange_topics_inform";
    public static void main(String[] args) {
        Connection connection = null;
        Channel channel = null;
        try {
            //创建一个与MQ的连接
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setVirtualHost("/");//rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务器

            //创建一个连接
            connection = factory.newConnection();
            //创建与交换机的通道，每个通道代表一个会话
            channel = connection.createChannel();
            //声明交换机 String exchange, BuiltinExchangeType type
            /**
             * 参数明细
             * 1、交换机名称
             * 2、交换机类型，fanout、topic、direct、headers
             */
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            //声明队列
            /**
             * 参数明细：
             * 1、队列名称
             * 2、是否持久化
             * 3、是否独占此队列
             * 4、队列不用是否自动删除
             * 5、参数
             */
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);

            /***
             * 1.Queue名称
             * 2.Exchange名称
             * 3.routeKey
             */
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_TOPICS_INFORM,ROUTEKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_TOPICS_INFORM, ROUTEKEY_SMS);

            //发送邮件消息
            for (int i=0;i<10;i++){
                String message = "email inform to user"+i;
                //向交换机发送消息 String exchange, String routingKey, BasicProperties props, byte[] body
                /**
                 * 参数明细
                 * 1、交换机名称，不指令使用默认交换机名称 Default Exchange
                 * 2、routingKey（路由key），根据key名称将消息转发到具体的队列，这里填写队列名称表示消息将发到此队列
                 * 3、消息属性
                 * 4、消息内容
                 */
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.email", null, message.getBytes());
                System.out.println("Send Message is:'" + message + "'");
            }
            //发送短信消息
            for (int i=0;i<10;i++){
                String message = "sms inform to user"+i;
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms", null, message.getBytes());
                System.out.println("Send Message is:'" + message + "'");
            }
            //发送短信和邮件消息
            for (int i=0;i<10;i++){
                String message = "sms and email inform to user"+i;
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms.email", null, message.getBytes());
                System.out.println("Send Message is:'" + message + "'");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally{
            if(channel!=null){
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}