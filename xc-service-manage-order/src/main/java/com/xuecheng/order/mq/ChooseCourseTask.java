package com.xuecheng.order.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    /**
     * 每隔一分钟扫描一次消息表。获取消息表前1000的并且updateTime在一分钟之前的数据。
     */
    @Scheduled(cron = "0/3 * * * * *")
    public void sendChoosecourseTask(){
        //取出当前时间1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        // updateTime为当前时间一分钟前的消息表的行。(取前1000条)
        List<XcTask> taskList = taskService.findTaskList(time, 1000);
        // 发送消息到消息队列，并且更新updateTime为当前时间。
        for (XcTask xcTask : taskList) {
            // 根据taskId和version查询task，修改version版本 版本+1。
            // 如果影响行>0 说明taskId和version对应的行存在。
            // 发送消息，修改update。
            // mysql事物级别为可重复读，在一个事物修改表的时候另个事物是不能修改表的。
            // 等提交事物后，根据版本不能查询到对应行了。可防止不同服务发送多条消息。
            if (taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0) {
                // 拿出消息表中的消息Exchange和routeKey发送消息。
                taskService.publish(xcTask, xcTask.getMqExchange(), xcTask.getMqRoutingkey());
            }
            LOGGER.info("send choose course task id:{}",xcTask.getId());
        }
    }


    /**
     * 接收选课响应结果
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChoosecourseTask(XcTask task, Message message, Channel channel) throws IOException {
        LOGGER.info("receiveChoosecourseTask...{}",task.getId());
        //接收到 的消息id
        String id = task.getId();
        //删除任务，添加历史任务
        taskService.finishTask(id);
    }


    //    @Scheduled(fixedRate = 5000) //上次执行开始时间后5秒执行
//    @Scheduled(fixedDelay = 5000)  //上次执行完毕后5秒执行
//    @Scheduled(initialDelay=3000, fixedRate=5000) //第一次延迟3秒，以后每隔5秒执行一次

    /**
     * 秒（0~59）
     * 分钟（0~59）
     * 小时（0~23）
     * 月中的天（1~31）
     * 月（1~12）
     * 周中的天（填写MON，TUE，WED，THU，FRI，SAT,SUN，或数字1~7 1表示MON，依次类推）
     *
     * “/”字符表示指定数值的增量
     * “*”字符表示所有可能的值
     * “-”字符表示区间范围
     * "," 字符表示列举
     * “？”字符仅被用于月中的天和周中的天两个子表达式，表示不指定值
     */
    //@Scheduled(cron = "0/3 * * * * *")//每隔3秒执行一次
    public void task1() {
        LOGGER.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务1结束===============");
    }



    //@Scheduled(fixedRate = 3000) //上次执行开始时间后5秒执行
    public void task2(){
        LOGGER.info("===============测试定时任务2开始===============");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务2结束===============");
    }
}