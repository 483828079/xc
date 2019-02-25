package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    LearningService learningService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 监听选课消息队列
     * 选课，添加消息到历史表。如果选课成功发送选课完成消息。
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE})
    public void receiveChooseCourseTask(XcTask xcTask, Message message, Channel channel) throws IOException {
        LOGGER.info("receive choose course task,taskId:{}",xcTask.getId());
        // 接收到的消息对象id
        String id = xcTask.getId();
        try {
            // 拿到消息信息中的requestBody字段值(json格式的userId和courseId)
            String requestBody = xcTask.getRequestBody();
            Map<String, String> reqBodyMap = JSON.parseObject(requestBody, Map.class);
            String userId = reqBodyMap.get("userId");
            String courseId = reqBodyMap.get("courseId");
            String valid = reqBodyMap.get("valid");
            Date startTime = null;
            Date endTime = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            if(reqBodyMap.get("startTime") != null){
                startTime = dateFormat.parse(reqBodyMap.get("startTime"));
            }
            if(reqBodyMap.get("endTime") != null){
                endTime = dateFormat.parse(reqBodyMap.get("endTime"));
            }

            // 添加选课
            ResponseResult addCourse = learningService.addCourse(userId, courseId, valid,startTime, endTime,xcTask);

            // 选课成功发送响应消息
            if(addCourse.isSuccess()){
                // 发送响应消息
                rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE, RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY, xcTask );
                LOGGER.info("send finish choose course taskId:{}",id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("send finish choose course taskId:{}", id);
        }
    }
}