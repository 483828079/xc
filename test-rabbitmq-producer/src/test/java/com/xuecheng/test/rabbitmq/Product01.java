package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Work queues
 * 	Product声明队列，队列不存在创建队列。
 * 	生产消息到默认的Exchange((AMQP default) type:direct), 使用和队列名称相同的routingKey。
 * 	Consumer监听队列获取生产的消息。
 *
 * 1、一条消息只会被一个消费者接收；
 * 2、rabbit采用轮询的方式将消息平均发送给消费者；
 * 3、消费者在处理完某条消息后，才会收到下一条消息。
 */
public class Product01 {
	private static final String QUEUE = "HelloWorld";
	public static void main(String[] args) {
		// 通过连接工厂获取连接对象
		ConnectionFactory connectionFactory = new ConnectionFactory();
		// 设置连接信息

		// 地址
		connectionFactory.setHost("127.0.0.1");
		// 端口(5672)
		connectionFactory.setPort(5672);
		// 用户名密码
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");

		// 设置虚拟机("/"默认的虚拟机)
		// 可以有多个虚拟机，每一个虚拟机相当于一个rabbitmq。
		connectionFactory.setVirtualHost("/");
		Connection connection = null;
		Channel channel = null;
		try {
			// 获取连接(用于client和broker连接)
			connection = connectionFactory.newConnection();
			// channel 网络信道。几乎所有的操作都在channel中进行。
			// 是消息读写的通道，客户端可以建立多个channel，每个channel
			// 代表一个会话任务。
			channel = connection.createChannel();

			/**
			 * 声明队列，如果Rabbit中没有此队列将自动创建
			 * param1:队列名称
			 * param2:是否持久化, 如果设置会被持久化到内存，mq重启队列还在。
			 * param3:队列是否独占此连接
			 * param4:队列不再使用时是否自动删除此队列
			 * param5:队列参数,可以设置
			 */
			channel.queueDeclare(QUEUE, true, false, false, null);
			String message = "你好我是世界"+System.currentTimeMillis();
			/**
			 * 消息发布方法
			 * param1：Exchange的名称，如果没有指定，则使用Default Exchange
			 * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指定的消息队列
			 * 			如果要使用默认Exchange就使用queue名称。
			 * 		Message:
			 * 				由properties和body组成
			 * 				properties可以对消息进行修饰，比如延时、优先级等。
			 * 				body就是消息体的内容。
			 * param3:消息包含的属性
			 * param4：消息体
			 */
			/**
			 * 这里没有指定交换机，消息将发送给默认交换机，每个队列也会绑定那个默认的交换机，但是不能显示绑定或解除绑定
			 *　默认的交换机，routingKey等于队列名称
			 */
			channel.basicPublish("", QUEUE, null, message.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			if(channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
			if(connection != null) {
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
