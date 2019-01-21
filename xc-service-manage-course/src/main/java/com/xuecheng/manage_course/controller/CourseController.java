package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {
    @Autowired
    CourseService courseService;

    /**
     * 查询课程计划列表
     * @param courseId 课程id。
     * @return
     */
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    /**
     * 新增课程计划
     * @param teachplan 新增的课程信息
     * @return
     */
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    /**
     * 分页查询课程列表
     * @param page 当前页
     * @param size 每页显示个数
     * @param courseListRequest 查询条件，以便后期拓展。
     * @return 响应(响应状态+分页信息)
     * CourseInfo 拓展字段，能够除了能够保存基本课程信息还能保存图片。
     */
    @GetMapping("coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page,
                                                          @PathVariable("size") int size,
                                                          CourseListRequest courseListRequest) {
        return courseService.findCourseList(page, size, courseListRequest);
    }
}
