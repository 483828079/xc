package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {
	@Autowired
	private CmsTemplateRepository cmsTemplateRepository;

	public QueryResponseResult findAll() {
		List<CmsTemplate> cmsTemplateList = cmsTemplateRepository.findAll();

		// 封装查询结果
		QueryResult queryResult = new QueryResult();
		queryResult.setTotal(cmsTemplateList.size());
		queryResult.setList(cmsTemplateList);

		// 查询成功
		return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
	}
}
