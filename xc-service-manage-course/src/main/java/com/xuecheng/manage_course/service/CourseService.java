package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CourseService {
	@Autowired
	TeachplanMapper teachplanMapper;
	@Autowired
	CourseBaseRepository courseBaseRepository;
	@Autowired
	CourseMapper courseMapper;
	@Autowired
	TeachplanRepository teachplanRepository;
	@Autowired
	CourseMarketRepository courseMarketRepository;

	//查询课程计划
	public TeachplanNode findTeachplanList(String courseId){
		return teachplanMapper.selectList(courseId);
	}

	/**
	 * 添加课程计划
	 * @param teachplan 课程计划实例
	 * @return
	 */
	@Transactional
	public ResponseResult addTeachplan(Teachplan teachplan){
		// 在页面上添加课程计划。
		// 下拉框不选择表名要添加的是二级节点
		// 下拉框选择表明要添加的是三级节点。
		// 下拉框选中的值代表的是parentId

		String parentid = teachplan.getParentid();
		if (Objects.isNull(parentid)) {
			// 要添加的是二级节点
			// 添加二级节点到根节点，查询根节点。

			// 根据课程id查询根节点id
			String courseid = teachplan.getCourseid();
			// 获取根节点id
			parentid = this.getTeachplanRoot(courseid);

			// 没有父节点抛出异常。
			if (StringUtils.isEmpty(parentid)) {
				ExceptionCast.cast(CommonCode.FAIL);
			}

			// 创建二级节点，实例化。
			// 设置父节点
			teachplan.setParentid(parentid);
			// 二级节点
			teachplan.setGrade("2");
			// 未发布
			teachplan.setStatus("0");
			teachplanRepository.save(teachplan);

			// 添加成功。
			return new ResponseResult(CommonCode.SUCCESS);
		} else { // 要添加的是三级节点。
			// 直接设置父节点,父节点就是二级节点。
			teachplan.setParentid(parentid);
			teachplan.setGrade("3");
			teachplan.setStatus("0");
			teachplanRepository.save(teachplan);

			// 添加成功。
			return new ResponseResult(CommonCode.SUCCESS);
		}
	}

	/**
	 * 根据课程id获取课程计划根节点。
	 * @param courseid 课程id
	 * @return
	 */
	private String getTeachplanRoot(String courseid) {
		// 课程是否存在
		Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseid);
		// 课程不存在返回null。
		if (! optionalCourseBase.isPresent()) {
			return null;
		}

		// 查询课程的根节点.
		List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseid, "0");
		// 课程的根节点是一个，课程目录。
		// 如果没有查询到根节点说明是新课还没有添加根节点。
		// 添加根节点.
		if (Objects.isNull(teachplanList) || teachplanList.size() == 0) {
			// 新增一个根结点，根节点只有一个代表课程名称。
			// 根节点的名称就是课程的名称
			Teachplan teachplanRoot = new Teachplan();
			// 设置课程id给根节点。
			teachplanRoot.setCourseid(courseid);
			// 根节点名称就是课程名称
			teachplanRoot.setPname(optionalCourseBase.get().getName());
			// 根节点的父节点为0
			teachplanRoot.setParentid("0");
			// 根节点是一级节点
			teachplanRoot.setGrade("1");//1级
			// 默认课程状态为0，新添加的都是未发布
			teachplanRoot.setStatus("0");//未发布
			// 实例化的数据库。
			teachplanRepository.save(teachplanRoot);
			return teachplanRoot.getId();
		}

		// 如果课程有根节点，返回根节点。
		return teachplanList.get(0).getId();
	}

	/**
	 * 分页查询课程列表
	 * @param page 当前页
	 * @param size 每页显示个数
	 * @param courseListRequest 查询条件，以便后期拓展。
	 * @return 响应(响应状态+分页信息)
	 * CourseInfo 拓展字段，能够除了能够保存基本课程信息还能保存图片。
	 */
	public QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest) {
		// 在执行第一条sql语句之前执行。
		PageHelper.startPage(page, size);
		Page<CourseInfo> courseInfoPage = courseMapper.findCourseListPage(courseListRequest);
		QueryResult<CourseInfo> queryResult = new QueryResult<>();
		queryResult.setTotal(courseInfoPage.getTotal());
		queryResult.setList(courseInfoPage.getResult());
		return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
	}

	/**
	 * 新增课程
	 * @param courseBase 课程信息
	 * @return ResultCode resultCode,String courseid 状态信息和课程id
	 */
	@Transactional
	public AddCourseResult addCourseBase(CourseBase courseBase) {
		//课程状态默认为未发布
		courseBase.setStatus("0");
		courseBaseRepository.save(courseBase);
		return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
	}

	/**
	 * 根据课程id查询课程基本信息
	 * @param courseId
	 * @return
	 */
	public CourseBase getCourseBaseById(String courseId) {
		Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
		if (courseBaseOptional.isPresent()) {
			return courseBaseOptional.get();
		}
		return null;
	}

	/**
	 * 根据课程id和课程信息修改课程。
	 * @param id
	 * @param courseBase
	 * @return
	 */
	@Transactional
	public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
		// 校验表单
		if (StringUtils.isEmpty(courseBase.getName()) ||
			StringUtils.isEmpty(courseBase.getMt()) ||
			StringUtils.isEmpty(courseBase.getSt())) {
			ExceptionCast.cast(CourseCode.COURSE_FROM_IMPERFECT);
		}

		// 查询课程
		CourseBase courseBaseInfo = this.getCourseBaseById(id);

		// 课程不存在
		if (Objects.isNull(courseBaseInfo)) {
			ExceptionCast.cast(CourseCode.COURSE_NOTEXIST);
		}

		courseBaseInfo.setName(courseBase.getName());
		courseBaseInfo.setMt(courseBase.getMt());
		courseBaseInfo.setSt(courseBase.getSt());
		courseBaseInfo.setGrade(courseBase.getGrade());
		courseBaseInfo.setStudymodel(courseBase.getStudymodel());
		courseBaseInfo.setUsers(courseBase.getUsers());
		courseBaseInfo.setDescription(courseBase.getDescription());

		CourseBase saveCourseBase = courseBaseRepository.save(courseBaseInfo);

		if (Objects.isNull(saveCourseBase)) {
			return new ResponseResult(CommonCode.FAIL);
		}
		return new ResponseResult(CommonCode.SUCCESS);
	}

	/**
	 * 根据课程id查询营销信息
	 * @param courseId
	 * @return
	 */
	public CourseMarket getCourseMarketById(String courseId) {
		Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(courseId);
		if (optionalCourseMarket.isPresent()) {
			return optionalCourseMarket.get();
		}
		return null;
	}

	/**
	 * 修改课程营销信息
	 * @param id
	 * @param courseMarket
	 * @return
	 */
	@Transactional
	public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
		Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
		if (courseMarketOptional.isPresent()) {
			CourseMarket courseMarketInfo = courseMarketOptional.get();
			courseMarketInfo.setCharge(courseMarket.getCharge());
			courseMarketInfo.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
			courseMarketInfo.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
			courseMarketInfo.setPrice(courseMarket.getPrice());
			courseMarketInfo.setQq(courseMarket.getQq());
			courseMarketInfo.setValid(courseMarket.getValid());
			CourseMarket saveCourseMarket = courseMarketRepository.save(courseMarketInfo);
			if (Objects.isNull(saveCourseMarket)) {
				return new ResponseResult(CommonCode.FAIL);
			}
			return new ResponseResult(CommonCode.SUCCESS);
		} else {
			courseMarket.setId(id);
			CourseMarket saveCourseMarket = courseMarketRepository.save(courseMarket);
			if (Objects.isNull(saveCourseMarket)) {
				return new ResponseResult(CommonCode.FAIL);
			}
			return new ResponseResult(CommonCode.SUCCESS);
		}
	}
}