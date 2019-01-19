package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *Routing
 *  声明Queue，声明Exchange(名称，类型(BuiltinExchangeType.DIRECT))
 *  Exchange绑定Queue，绑定Queue的时候设置routeKey。
 *  Consumer发送消息到Exchange，需要设置Exchange的名称和routeKey。
 *  Exchange会将消息转发到对应routeKey的Queue上。
 *
 *  Work queues
 *      有一个Queue使用默认的Exchange和Queue名称相同的routeKey。
 *      发送消息到默认的Exchange，Exchange转发到绑定的一个Queue。
 *      监听这个Queue就能接收到消息，多个Product监听轮询接收。
 * Publish/subscribe
 *      声明Exchange，绑定多个Queue，routeKey值为""。
 *      发送消息到Exchange所有绑定的Queue都能接收到。
 *      监听Queue的Product就能接收到消息，监听同一个Queue的Product轮询。
 * route
 *      声明Exchange，绑定多个Queue，绑定Queue加上routeKey。
 *      每个Queue都有对应的routeKey。
 *      生产消息，需要Exchange和routeKey。
 *      Exchange会转发消息到对应routeKey的Queue。
 *      监听的时候：
 *          声明Queue，声明Exchange。使用routeKey绑定Queue和Exchange。
 *          监听Queue，能够接收到对应routeKey的Queue中的消息。
 *          多个Product监听同一个Queue轮询。
 *
 *     比起Publish/subscribe多了routeKey。
 *     Publish/subscribe发送消息到Exchange，Exchange转发消息到所有的Queue。
 *     发送消息要带上routeKey
 *     route的Exchange转发消息到对应routeKey的Queue。
 *
 *     Work queues  Exchange转发消息到唯一的Queue。
 *     Publish/subscribe Exchange转发消息到所有绑定的Queue。
 *     route Exchange转发消息到所有routeKey对应的Queue。
 *
 */
public class Producer03_routing {
    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_ROUTING_INFORM="exchange_routing_inform";
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
            channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.DIRECT);
            //声明队列
//            channel.queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
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
            //交换机和队列绑定String queue, String exchange, String routingKey
            /**
             * 参数明细
             * 1、队列名称
             * 2、交换机名称
             * 3、路由key
             */
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_ROUTING_INFORM,QUEUE_INFORM_EMAIL);
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_ROUTING_INFORM,QUEUE_INFORM_SMS);
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
                channel.basicPublish(EXCHANGE_ROUTING_INFORM, QUEUE_INFORM_EMAIL, null, message.getBytes());
                System.out.println("Send Message is:'" + message + "'");
            }
            //发送短信消息
            for (int i=0;i<10;i++){
                String message = "sms inform to user"+i;
                //向交换机发送消息 String exchange, String routingKey, BasicProperties props, byte[] body
                channel.basicPublish(EXCHANGE_ROUTING_INFORM, QUEUE_INFORM_SMS, null, message.getBytes());
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
