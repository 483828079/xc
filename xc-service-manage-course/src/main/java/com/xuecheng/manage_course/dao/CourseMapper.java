package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper {
	Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}
