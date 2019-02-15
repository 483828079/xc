package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia, String> {
	/**
	 * 查询通过课程id查询所有的TeachPlanMedia
	 * @param courseId
	 * @return
	 */
	List<TeachplanMedia> findByCourseId(String courseId);
}
