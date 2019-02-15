package com.xuecheng.search.controller;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {
    @Autowired
    EsCourseService esCourseService;

    /**
     * 按条件分页查询索引库中CoursePub信息
     * @param page 当前页
     * @param size 每页显示数量
     * @param courseSearchParam 查询条件
     * @return
     * @throws IOException
     */
    @GetMapping(value="/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") int page, @PathVariable("size") int size, CourseSearchParam courseSearchParam) throws IOException {
        return esCourseService.list(page,size,courseSearchParam);
    }

    /**
     * 根据课程id查询课程信息
     * @param id
     * @return map key课程id CoursePub 发布的课程信息
     */
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getAll(@PathVariable("id") String id) {
        return esCourseService.getAll(id);
    }


    @GetMapping(value="/getmedia/{teachplanId}")
    public TeachplanMediaPub getMedia(String teachPlanId) {
        String[] teachPlanIds = new String[]{teachPlanId};
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = esCourseService.getmedia(teachPlanIds);
        QueryResult<TeachplanMediaPub> queryResult = queryResponseResult.getQueryResult();
        if (! Objects.isNull(queryResult) || !Objects.isNull(queryResult.getList()) || queryResult.getList().size() > 0) {
            // 返回第一个媒资信息，因为一个课程计划对应一个媒资信息
            return queryResult.getList().get(0);
        }
        return new TeachplanMediaPub();
    }
}