package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePicRepository extends JpaRepository<CoursePic, String> {
	/**
	 * 根据课程id删除课程图片
	 * @param courseId 课程id
	 * @return 删除成功返回影响行
	 */
	long deleteCoursePicByCourseid(String courseId);
}