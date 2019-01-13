package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PageService {
	@Autowired
	CmsPageRepository cmsPageRepository;

	/**
	 * 页面列表分页查询
	 * @param page 当前页码
	 * @param size 页面显示个数
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

		if (Objects.isNull(cmsPageInfo)) { // 如果为空表明保存的页面不存在，可以进行保存。
			// 为了避免传入的id有值，设置id为null让mongodb生成id。
			cmsPage.setPageId(null);
			// 保存后会将生成的id封装在返回的对象中。
			CmsPage saveCmsPageInfo = cmsPageRepository.save(cmsPage);

			// 返回正确信息和保存成功的cmsPage
			return new CmsPageResult(CommonCode.SUCCESS, saveCmsPageInfo);
		}

		// 表示不能保存返回错误信息
		return new CmsPageResult(CommonCode.FAIL, null);
	}
}