package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.media.response.GetMediaResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.learning.config.CourseSearchClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LearningService {

    @Autowired
    CourseSearchClient courseSearchClient;

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
}