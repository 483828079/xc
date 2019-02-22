package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 取出前n条任务,取出指定时间之前处理的任务
     */
    public List<XcTask> findTaskList(Date updateTime, int n) {
        //设置分页参数，取出前n 条记录
        Pageable pageable = PageRequest.of(0, n);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getContent();
    }

    /**
     * 根据消息表中的数据发送消息到Exchange并指定routeKey。
     * 修改updateTime为当前时间
     * @param xcTask
     * @param ex
     * @param routingKey
     */
    @Transactional
    @Rollback(false)
    public void publish(XcTask xcTask, String ex, String routingKey) {
        String taskId = xcTask.getId();
        Optional<XcTask> xcTaskOptional = xcTaskRepository.findById(taskId);
        if (xcTaskOptional.isPresent()) {
            // 发送消息到Exchange并指定routingKey
            // 这里的xcTask为对象，所以会被序列化存放到消息队列中
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            // 发送消息后更新updateTime为当前时间
            xcTaskRepository.updateTaskTime(taskId, new Date());
        }
    }


    /**
     * 根据版本号和taskId修改task。
     * @param taskId
     * @param version
     * @return 影响行数
     */
    @Transactional
    public int getTask(String taskId,int version){
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }
}