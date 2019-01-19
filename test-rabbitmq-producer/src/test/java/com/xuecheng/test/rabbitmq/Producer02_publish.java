package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *  Publish/subscribe 发布订阅模式。
 *  需要声明Exchange(名称和类型)，发布订阅模式的Exchange的类型是BuiltinExchangeType.FANOUT
 *  声明队列。 声明就是需要使用，如果没有就创建。
 *  然后将Queue和Exchange绑定。 不用设置routingKey("")。
 *  再向Exchange发送消息。
 *
 *  可以让Exchange绑定多个Queue，这样所有的Queue都会受到发送到Exchange的消息。
 *  然后监听Queue的Product也能够收到对应的消息。
 *
 *  相当于Work queues的升级。
 *  Work queues使用默认的Exchange，routingKey使用队列名称一样，一个Queue。
 *  会默认将默认的Exchange和Queue绑定。
 *  Consumer生产消息到Exchange，会将消息放到绑定的一个Queue。
 *  多个Product监听一个Queue。然后轮询拿到Queue中的消息。
 *
 *  Publish/subscribe使用自己声明的Exchange类型为FANOUT。
 *  可以给这个Exchange绑定多个Queue，routingKey为"".
 *  Consumer生产消息到Exchange，多个Queue中都能接收到Exchange转发的消息。
 *  监听这些Queue的Product也能够收到相同的消息。
 *  监听同一个Queue的Product轮询。
 *  相当于使用多个Queue分担了压力。
 *
 */
public class Producer02_publish {
    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_FANOUT_INFORM="exchange_fanout_inform";
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
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //声明队列
//           (String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
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
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_FANOUT_INFORM,"");
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_FANOUT_INFORM,"");
            //发送消息
            for (int i=0;i<10;i++){
                String message = "inform to user"+i;
                //向交换机发送消息 String exchange, String routingKey, BasicProperties props, byte[] body
                /**
                 * 参数明细
                 * 1、交换机名称，不指令使用默认交换机名称 Default Exchange
                 * 2、routingKey（路由key），根据key名称将消息转发到具体的队列，这里填写队列名称表示消息将发到此队列
                 * 3、消息属性
                 * 4、消息内容
                 */
                channel.basicPublish(EXCHANGE_FANOUT_INFORM, "", null, message.getBytes());
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
