package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequestMapping
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
	@Autowired
	CoursePicRepository coursePicRepository;
	@Autowired
	CoursePubRepository coursePubRepository;
	@Autowired
	CmsPageClient cmsPageClient;
	@Autowired
	TeachplanMediaRepository teachplanMediaRepository;
	@Autowired
	TeachplanMediaPubRepository teachplanMediaPubRepository;

	@Value("${course-publish.dataUrlPre}")
	private String publish_dataUrlPre;
	@Value("${course-publish.pagePhysicalPath}")
	private String publish_page_physicalpath;
	@Value("${course-publish.pageWebPath}")
	private String publish_page_webpath;
	@Value("${course-publish.siteId}")
	private String publish_siteId;
	@Value("${course-publish.templateId}")
	private String publish_templateId;
	@Value("${course-publish.previewUrl}")
	private String previewUrl;



	//查询课程计划
	public TeachplanNode findTeachplanList(String courseId){
		TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
		return teachplanNode;
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
	public QueryResponseResult<CourseInfo> findCourseList(String companyId, int page, int size, CourseListRequest courseListRequest) {
		if (Objects.isNull(courseListRequest)) {
			courseListRequest = new CourseListRequest();
		}

		if (StringUtils.isNotEmpty(companyId)) {
			courseListRequest.setCompanyId(companyId);
		}

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

	/**
	 * 保存课程图片
	 * @param courseId
	 * @param pic
	 * @return
	 */
	@Transactional
	public ResponseResult saveCoursePic(String courseId, String pic) {
		// 图片如果存在替换掉，如果不存在添加新的图片
		Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);

		CoursePic coursePic = null;
		// 课程存在。
		if (coursePicOptional.isPresent()) {
			coursePic = coursePicOptional.get();
		}

		// 课程不存在
		if (Objects.isNull(coursePic)) {
			coursePic = new CoursePic();
		}

		coursePic.setPic(pic);
		coursePic.setCourseid(courseId);
		coursePicRepository.save(coursePic);

		return new ResponseResult(CommonCode.SUCCESS);
	}

	/**
	 * 根据课程id查询课程图片
	 * @param courseId
	 * @return
	 */
	public CoursePic findCoursepic(String courseId) {
		Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
		if (coursePicOptional.isPresent()) {
			return coursePicOptional.get();
		}
		return null;
	}

	/**
	 * 根据课程id删除对应课程图片
	 * @param courseId 课程id
	 * @return 响应信息
	 */
	@Transactional
	public ResponseResult deleteCoursePic(String courseId) {
		long resultCount = coursePicRepository.deleteCoursePicByCourseid(courseId);
		if (resultCount > 0) {
			return new ResponseResult(CommonCode.SUCCESS);
		}

		return new ResponseResult(CommonCode.FAIL);
	}

	/**
	 * 查询课程相关信息
	 * @param id
	 * @return
	 */
	public CourseView getCourseView(String id) {
		CourseView courseView = new CourseView();
		// 课程基础信息
		Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
		if (courseBaseOptional.isPresent()) {
			courseView.setCourseBase(courseBaseOptional.get());
		}

		// 课程营销信息
		Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
		if (marketOptional.isPresent()) {
			courseView.setCourseMarket(marketOptional.get());
		}

		// 课程图片
		Optional<CoursePic> picOptional = coursePicRepository.findById(id);
		if (picOptional.isPresent()) {
			courseView.setCoursePic(picOptional.get());
		}

		// 课程计划
		TeachplanNode teachplanNode = teachplanMapper.selectList(id);
		courseView.setTeachplanNode(teachplanNode);
		return courseView;
	}

	/**
	 * 获取课程详情页面预览URL
	 * @param id 课程id
	 * @return 状态信息和URL
	 */
	public CoursePublishResult preview(String id) {
		// 获取课程基本信息
		CourseBase courseBaseInfo = findCourseBaseById(id);

		// 封装课程页面信息，调用CMS服务对课程页面信息实例化。
		CmsPage coursePage = new CmsPage();
		// 站点(固定的)
		coursePage.setSiteId(publish_siteId);//课程预览站点
		// 模板(固定的)
		coursePage.setTemplateId(publish_templateId);
		// 页面名称, 约定就是课程id+html(一般都会这样，以便访问不同课程的详情页面)
		coursePage.setPageName(id+".html");
		// 页面别名
		coursePage.setPageAliase(courseBaseInfo.getName());
		// 页面访问路径
		coursePage.setPageWebPath(publish_page_webpath);
		// 页面存储路径
		coursePage.setPagePhysicalPath(publish_page_physicalpath);
		// 数据url,和课程id拼接用来获取课程数据。
		coursePage.setDataUrl(publish_dataUrlPre+id);

		CmsPageResult saveCmsPageResult = cmsPageClient.save(coursePage);
		if(!saveCmsPageResult.isSuccess()){
			return new CoursePublishResult(CommonCode.FAIL,null);
		}
		// 页面id
		String pageId = saveCmsPageResult.getCmsPage().getPageId();
		// 页面url,通过页面id可以对页面进预览
		String pageUrl = previewUrl+pageId;
		// 响应给前端
		return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
	}

	/**
	 * 根据课程id查询课程基本信息
	 * @param courseId
	 * @return
	 */
	public CourseBase findCourseBaseById(String courseId){
		Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
		if(! baseOptional.isPresent()){
			ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
		}
		CourseBase courseBase = baseOptional.get();
		return courseBase;
	}

	/**
	 * 发布课程
	 * @param courseId
	 * @return
	 */
	public CoursePublishResult publish(String courseId) {
		// 查询对应课程
		CourseBase courseBase = this.findCourseBaseById(courseId);

		// 发布课程详情页面
		CmsPostPageResult cmsPostPageResult = publish_page(courseId);
		if(!cmsPostPageResult.isSuccess()){
			ExceptionCast.cast(CommonCode.FAIL);
		}

		// 更新课程状态
		CourseBase courseBaseInfo = saveCoursePubState(courseId);

		// 发布页面成功后一些别的实现。。。

		// 实例化索引需要的数据到coursePub
		// 发布课程后向course_pub添加所有的课程信息(course_pub用来作为索引库数据)
		// course_pub表只是在发布页面后添加数据，用来对索引库进行同步。
		// 创建coursePub对象
		CoursePub coursePub = createCoursePub(courseId);
		// 实例化coursePub
		CoursePub saveCoursePub = this.saveCoursePub(courseId, coursePub);
		if (Objects.isNull(saveCoursePub)) {
			// 索引创建失败
			ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
		}

		// 发布页面成功后根据courseId对TeachPlanMedia进行实例化
		this.saveTeachPlanMediaPub(courseId);

		//...

		// 获取课程详情页的URL
		String pageUrl = cmsPostPageResult.getPageUrl();

		// 响应状态信息和课程详情页URL到页面。
		return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
	}

	/**
	 * 实例化courseId对应的TeachPlanMediaPub
	 * @param courseId
	 */
	private void saveTeachPlanMediaPub(String courseId) {
		// 页面发布后，实例化该页面的所有媒资信息，用来同步到索引库。
		// 如果courseId对应的媒资信息存在就先删除
		teachplanMediaPubRepository.deleteByCourseId(courseId);

		// 获取courseId对应的所有媒资信息
		List<TeachplanMedia> mediaList = teachplanMediaRepository.findByCourseId(courseId);

		// 将所有的媒资信息同步到TeachPlanMedia中
		List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
		for (TeachplanMedia teachplanMedia : mediaList) {
			TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
			BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
			teachplanMediaPub.setTimestamp(new Date());
			teachplanMediaPubList.add(teachplanMediaPub);
		}
		teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
	}

	/**
	 * 如果coursePub已经存在进行修改，不存在添加。
	 * @param courseId 课程id
	 * @param coursePub coursePub更新的数据
	 * @return
	 */
	private CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
		if (StringUtils.isEmpty(courseId)) {
			ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
		}

		CoursePub coursePubInfo = this.findCoursePubByCourseId(courseId);

		if (Objects.isNull(coursePubInfo)) {
			coursePubInfo = new CoursePub();
		}

		BeanUtils.copyProperties(coursePub, coursePubInfo);

		// 设置主键
		coursePubInfo.setId(courseId);
		// 更新时间戳为最新时间
		coursePub.setTimestamp(new Date());
		//发布时间
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String date = simpleDateFormat.format(new Date());
		coursePub.setPubTime(date);

		// 实例化并返回。
		return coursePubRepository.save(coursePub);
	}

	/**
	 * 根据courseId查询coursePub
	 * @param courseId 课程id
	 * @return coursePub
	 */
	private CoursePub findCoursePubByCourseId(String courseId) {
		if (StringUtils.isEmpty(courseId)) {
			ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
		}

		Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
		if (coursePubOptional.isPresent()) {
			return coursePubOptional.get();
		}

		return null;
	}

	/**
	 * 用来构建coursePub对象
	 * coursePub由courseBase,courseMarket,coursePic,teachplan组成。
	 * 它们主键都是courseId
	 * @param courseId
	 * @return
	 */
	private CoursePub createCoursePub(String courseId) {
		if (StringUtils.isEmpty(courseId)) {
			ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
		}

		CoursePub coursePub = new CoursePub();

		// 查询courseBase
		CourseBase courseBase = this.findCourseBaseById(courseId);
		if (! Objects.isNull(courseBase)) {
			BeanUtils.copyProperties(courseBase, coursePub);
		}

		// 查询coursePic
		CoursePic coursepic = this.findCoursepic(courseId);
		if (! Objects.isNull(courseBase)) {
			BeanUtils.copyProperties(coursepic, coursePub);
		}

		// 查询courseMarket
		CourseMarket courseMarket = this.getCourseMarketById(courseId);
		if (! Objects.isNull(courseMarket)) {
			BeanUtils.copyProperties(courseMarket, coursePub);
		}

		// 查询teachPlan
		TeachplanNode  teachplanNode  = this.findTeachplanList(courseId);
		if (! Objects.isNull(teachplanNode)) {
			coursePub.setTeachplan(JSON.toJSONString(teachplanNode));
		}

		// 因为copyProperties所以可能覆盖掉courseId。
		coursePub.setId(courseId);

		return coursePub;
	}

	/**
	 * 更新课程状态
	 * @param courseId
	 * @return
	 */
	private CourseBase saveCoursePubState(String courseId) {
		CourseBase courseBase = this.findCourseBaseById(courseId);
		//更新发布状态
		courseBase.setStatus("202002");
		CourseBase save = courseBaseRepository.save(courseBase);
		return save;
	}

	//发布课程正式页面
	public CmsPostPageResult publish_page(String courseId){
		CourseBase one = this.findCourseBaseById(courseId);
		//发布课程预览页面
		CmsPage cmsPage = new CmsPage();
		//站点
		cmsPage.setSiteId(publish_siteId);//课程预览站点
		//模板
		cmsPage.setTemplateId(publish_templateId);
		//页面名称
		cmsPage.setPageName(courseId+".html");
		//页面别名
		cmsPage.setPageAliase(one.getName());
		//页面访问路径
		cmsPage.setPageWebPath(publish_page_webpath);
		//页面存储路径
		cmsPage.setPagePhysicalPath(publish_page_physicalpath);
		//数据url
		cmsPage.setDataUrl(publish_dataUrlPre+courseId);
		//发布页面
		CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
		return cmsPostPageResult;
	}

	/**
	 * 保存课程计划媒资信息
	 * @param teachplanMedia
	 * @return
	 */
	public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
		if (Objects.isNull(teachplanMedia) || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
			ExceptionCast.cast(CommonCode.INVALIDPARAM);
		}
		// 只有三级的课程计划才能关联对应视频

		// 要添加视频的课程计划id
		String teachPlanId = teachplanMedia.getTeachplanId();
		Optional<Teachplan> teachPlanOptional = teachplanRepository.findById(teachPlanId);
		// 如果课程计划id对应的课程计划不存在，抛出异常。
		if (! teachPlanOptional.isPresent()) {
			ExceptionCast.cast(CommonCode.INVALIDPARAM);
		}

		// 添加视频的课程计划对象
		Teachplan teachPlanInfo = teachPlanOptional.get();
		String teachPlanGrade = teachPlanInfo.getGrade();
		// 如果课程计划不是三级抛出异常
		if (StringUtils.isEmpty(teachPlanGrade) || ! "3".equals(teachPlanGrade)) {
			ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
		}

		// 如果当前课程计划媒资信息存在就修改，不存在添加
		TeachplanMedia teachPlanMediaParam = null;
		// 课程计划id同样是课程计划媒资的id
		Optional<TeachplanMedia> teachPlanMediaOptional = teachplanMediaRepository.findById(teachPlanId);
		// 如果有就使用已经存在的信息，没有就创建新的对象。
		if (teachPlanMediaOptional.isPresent()) {
			teachPlanMediaParam = teachPlanMediaOptional.get();
		} else {
			teachPlanMediaParam = new TeachplanMedia();
		}

		// 设置需要保存的信息
		teachPlanMediaParam.setTeachplanId(teachPlanId);
		teachPlanMediaParam.setCourseId(teachplanMedia.getCourseId());
		teachPlanMediaParam.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
		teachPlanMediaParam.setMediaId(teachplanMedia.getMediaId());
		teachPlanMediaParam.setMediaUrl(teachplanMedia.getMediaUrl());
		teachplanMediaRepository.save(teachPlanMediaParam);
		return new ResponseResult(CommonCode.SUCCESS);
	}
}
