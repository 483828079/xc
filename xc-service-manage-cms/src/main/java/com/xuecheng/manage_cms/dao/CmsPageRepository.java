package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
	// 自定义DAO方法

	/**
	 * 根据pageName查询cmsPage
	 * */
	CmsPage findByPageName(String pageName);

	/**
	 * 根据pageName和pageType查询cmsPage
	 * */
	CmsPage findByPageNameAndPageType(String pageName, String pageType);

	/**
	 * 根据siteId和pageType查询cmsPage总数
	 * */
	int countBySiteIdAndPageType(String siteId, String pageType);

	/**
	 * 根据siteId和pageType进行分页查询
	 * @return 分页查询的返回值为Page，Page封装了分页的信息。
	 */
	Page<CmsPage> findBySiteIdAndPageType(String siteId, String pageType, Pageable pageable);
}