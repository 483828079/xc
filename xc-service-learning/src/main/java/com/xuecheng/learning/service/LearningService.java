package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.media.response.GetMediaResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class LearningService {

    @Autowired
    CourseSearchClient courseSearchClient;
    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    /**
     * 校验用户是否拥有该课程权限，根据课程计划查询课程计划对应视频地址
     * @param courseId
     * @param teachPlanId
     * @return
     */
    public GetMediaResult getMedia(String courseId, String teachPlanId) {
        // 校验用户是否拥有该课程权限(是否已经资费)
        // ...

        // 如果拥有该课程权限获取课程对应媒体url
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getMedia(teachPlanId);
        if (Objects.isNull(teachplanMediaPub) || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())) {
            //获取视频播放地址出错
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        return new GetMediaResult(CommonCode.SUCCESS, teachplanMediaPub.getMediaUrl());
    }

    /**
     * 选课已经存在更新选课信息，选课不存在添加选课。
     * 添加消息到历史消息表。添加选课要和添加消息到历史消息表在一个事物中。
     * @param userId 用户id
     * @param courseId courseId
     * @param valid
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param xcTask 消息对象
     * @return
     */
    @Transactional
    public ResponseResult addCourse(String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask){
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        if (StringUtils.isEmpty(userId)) {
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_USERISNULL);
        }
        if(xcTask == null || StringUtils.isEmpty(xcTask.getId())){
            ExceptionCast.cast(LearningCode.CHOOSECOURSE_TASKISNULL);
        }

        // 查询历史任务
        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
        // 如果历史任务存在说明不用选课。
        // 因为历史任务和选课在同一个事物中，说明选课已经存在。
        // 但是消息又发送过来说明之前虽然添加了选课但是完成选课消息发送失败。
        // 只用重新发送选课成功消息。
        if(optional.isPresent()){
            return new ResponseResult(CommonCode.SUCCESS);
        }

        // 根据userId和courseId取得唯一的选课对象
        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findXcLearningCourseByUserIdAndCourseId(userId, courseId);
        // 如果选课信息不存在，添加选课。
        if (Objects.isNull(xcLearningCourse)) {
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        } else { // 如果选课信息已经存在，更新选课开始时间和结束时间(以为是收费课程)
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        }

        // 向消息历史表中插入已经处理过选课的消息。
        // 如果消息在历史表中不存在。
        if(!optional.isPresent()){
            //添加历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
        }

        // 添加选课，添加消息到历史表都成功后发送成功状态。
        return new ResponseResult(CommonCode.SUCCESS);
    }
}