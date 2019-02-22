package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse, String> {
    /**
     * 用户id和课程id作为唯一索引，可以查询出唯一的选课信息。
     * 判断当前选课是否已经存在。
     * @param userId
     * @param courseId
     * @return
     */
    XcLearningCourse findXcLearningCourseByUserIdAndCourseId(String userId, String courseId);
}