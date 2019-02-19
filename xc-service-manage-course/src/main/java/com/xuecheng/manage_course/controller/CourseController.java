package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {
    @Autowired
    CourseService courseService;

    /**
     * 查询课程计划列表
     *  @PreAuthorize 方法之前进行授权
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
     * @PreAuthorize 在方法调用之前进行授权
     * @param page 当前页
     * @param size 每页显示个数
     * @param courseListRequest 查询条件，以便后期拓展。
     * @return 响应(响应状态+分页信息)
     * CourseInfo 拓展字段，能够除了能够保存基本课程信息还能保存图片。
     */
    @GetMapping("coursebase/list/{page}/{size}")
    @PreAuthorize("hasAuthority('course_find_list1')")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page,
                                                          @PathVariable("size") int size,
                                                          CourseListRequest courseListRequest) {
        return courseService.findCourseList(page, size, courseListRequest);
    }

    /**
     * 新增课程
     * @param courseBase 课程基本信息
     * @return
     */
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    @GetMapping("/coursebase/get/{courseId}")
    @PreAuthorize("hasAuthority('course_get_baseinfo')")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) throws RuntimeException {
        return courseService.getCourseBaseById(courseId);
    }

    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(id, courseBase);
    }

    /**
     * 根据课程id查询课程营销信息
     * @param courseId 课程id
     * @return 课程营销信息
     */
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    /**
     * 根据课程id和营销信息修改课程营销信息
     * @param id 课程id
     * @param courseMarket 营销信息
     * @return 响应状态
     */
    @PostMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        return courseService.updateCourseMarket(id, courseMarket);
    }

    /**
     * 关联课程和课程对应的图片
     * @param courseId 课程id
     * @param pic 课程图片
     * @return 响应状态信息
     */
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
        return courseService.saveCoursePic(courseId, pic);
    }

    /**
     * 根据课程id查询课程图片
     * @param courseId
     * @return
     */
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable String courseId) {
        return courseService.findCoursepic(courseId);
    }

    /**
     * 根据课程id删除课程图片
     * @param courseId
     * @return
     */
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    /**
     * 查询课程相关信息
     * @param id 课程id
     * @return
     */
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCourseView(id);
    }

    /**
     * 课程详情页面预览
     * @param id 要进行预览的courseId
     * @return 预览的URL和响应状态信息
     */
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    /**
     *根据课程id发布课程详情页
     * @param id
     * @return
     */
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }

    /**
     * 保存课程计划媒资信息
     * @param teachplanMedia
     * @return
     */
    @PostMapping("/savemedia")
    public ResponseResult saveMedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.saveMedia(teachplanMedia);
    }
}
