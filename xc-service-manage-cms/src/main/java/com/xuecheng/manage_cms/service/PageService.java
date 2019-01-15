package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class PageService {
	@Autowired
	CmsPageRepository cmsPageRepository;

	/**
	 * 页面列表分页查询
	 *
	 * @param page             当前页码
	 * @param size             页面显示个数
	 * @param queryPageRequest 查询条件
	 * @return 页面列表
	 */
	public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
		// 接口中规定 page size不能为null。

		if (queryPageRequest == null) { // 如果没有查询条件初始化查询对象
			queryPageRequest = new QueryPageRequest();
		}

		// 设置查询条件
		CmsPage cmsPage = new CmsPage();

		// siteId
		if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
			cmsPage.setSiteId(queryPageRequest.getSiteId());
		}

		// templateId
		if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
			cmsPage.setTemplateId(queryPageRequest.getTemplateId());
		}

		// pageAliase
		if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
			cmsPage.setPageAliase(queryPageRequest.getPageAliase());
		}

		// 默认从第一页开始。
		if (page <= 0) {
			page = 1;
		}

		// 因为mongodb接口的分页是从0开始(0代表页面上的1)
		page = page - 1;

		// 默认每页显示20条数据
		if (size <= 0) {
			size = 20;
		}

		// 按条件查询
		// 匹配方式
		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
				//pageAliase模糊查询。
				.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
		// 将cms对象中不为null的属性作为条件进行查询
		// 属性名等同于mongodb中的field。
		Example example = Example.of(cmsPage, exampleMatcher);

		// 分页查询
		Pageable pageable = PageRequest.of(page, size);
		Page<CmsPage> pageInfo = cmsPageRepository.findAll(example, pageable);


		// 封装查询信息
		QueryResult<CmsPage> queryResult = new QueryResult<>();
		queryResult.setList(pageInfo.getContent());
		queryResult.setTotal(pageInfo.getTotalElements());

		// 查询成功
		return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
	}

	/**
	 * 添加页面
	 *
	 * @param cmsPage
	 * @return
	 */
	public CmsPageResult add(CmsPage cmsPage) {
		// cms_page集中上创建页面名称、站点Id、页面webpath为唯一索引
		// 通过这三个条件可以确认一个唯一的页面。
		// 所以添加页面之前需要判断要添加的页面是否已经存在。
		CmsPage cmsPageInfo = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
				cmsPage.getPageName(),
				cmsPage.getSiteId(),
				cmsPage.getPageWebPath());

		// 如果页面已经存在抛出自定义异常
		if (!Objects.isNull(cmsPageInfo)) {
			// 抛出异常，设置异常状态信息。
			ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
		}

		// 为了避免传入的id有值，设置id为null让mongodb生成id。
		cmsPage.setPageId(null);
		// 保存后会将生成的id封装在返回的对象中。
		CmsPage saveCmsPageInfo = cmsPageRepository.save(cmsPage);
		// 返回正确信息和保存成功的cmsPage
		return new CmsPageResult(CommonCode.SUCCESS, saveCmsPageInfo);

	}

	/**
	 * 根据id查询cmsPage
	 * @param id
	 * @return
	 */
	public CmsPage findById(String id) {
		Optional<CmsPage> cmsPageOptional = cmsPageRepository.findById(id);
		// 如果查询到的cmsPage不为null
		if (cmsPageOptional.isPresent()) {
			return cmsPageOptional.get();
		}
		// 如果没有查询到就返回null
		return null;
	}

	/**
	 * 修改id对应的cmsPage
	 * @param id
	 * @param cmsPage
	 * @return
	 */
	public CmsPageResult update(String id, CmsPage cmsPage) {
		// 判断要修改的cmsPage是否已经存在，存在才可以修改。
		CmsPage cmsPageInfo = this.findById(id);
		if (! Objects.isNull(cmsPageInfo)) { // 存在允许修改。
			// 后端对表单的校验
			if (StringUtils.isEmpty(cmsPage.getTemplateId()) ||
			StringUtils.isEmpty(cmsPage.getSiteId()) ||
			StringUtils.isEmpty(cmsPage.getPageAliase()) ||
			StringUtils.isEmpty(cmsPage.getPageName()) ||
			StringUtils.isEmpty(cmsPage.getPageWebPath()) ||
			StringUtils.isEmpty(cmsPage.getPagePhysicalPath())) {
				return new CmsPageResult(CommonCode.FAIL);
			}

			// 因为是修改不是添加，所以id要存在
			// 前端传来的不一定是所有属性，所以要根据传来id查询所有属性。
			// 然后对当前id对应实体部分属性进行修改

			//更新模板id
			cmsPageInfo.setTemplateId(cmsPage.getTemplateId());
			//更新所属站点
			cmsPageInfo.setSiteId(cmsPage.getSiteId());
			//更新页面别名
			cmsPageInfo.setPageAliase(cmsPage.getPageAliase());
			//更新页面名称
			cmsPageInfo.setPageName(cmsPage.getPageName());
			//更新访问路径
			cmsPageInfo.setPageWebPath(cmsPage.getPageWebPath());
			//更新物理路径
			cmsPageInfo.setPagePhysicalPath(cmsPage.getPagePhysicalPath());

			//执行更新,save执行成功会返回保存的对象和生成的id。
			CmsPage saveCmsPageInfo = cmsPageRepository.save(cmsPageInfo);
			if (! Objects.isNull(saveCmsPageInfo)) {
				return new CmsPageResult(CommonCode.SUCCESS, saveCmsPageInfo);
			}

			// 保存失败
			return new CmsPageResult(CommonCode.FAIL);
		}

		// 不存在，所以不能修改
		return new CmsPageResult(CommonCode.FAIL);
	}

	/**
	 * 根据id删除cmsPage
	 * @param id
	 * @return
	 */
	public ResponseResult delete(String id) {
		CmsPage cmsPageInfo = this.findById(id);
		if (! Objects.isNull(cmsPageInfo)) {
			cmsPageRepository.deleteById(id);
			return new ResponseResult(CommonCode.SUCCESS); // 删除成功
		}

		// 删除失败
		return new ResponseResult(CommonCode.FAIL);
	}
}