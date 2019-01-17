package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

	public QueryResponseResult findList(int page, int size, QueryTemplateRequest queryTemplateRequest) {
		if (Objects.isNull(queryTemplateRequest)) {
			queryTemplateRequest = new QueryTemplateRequest();
		}

		CmsTemplate cmsTemplate = new CmsTemplate();
		if (StringUtils.isNotEmpty(queryTemplateRequest.getSiteId())) {
			cmsTemplate.setSiteId(queryTemplateRequest.getSiteId());
		}

		if (StringUtils.isNotEmpty(queryTemplateRequest.getTemplateName())) {
			cmsTemplate.setTemplateName(queryTemplateRequest.getTemplateName());
		}

		if (StringUtils.isNotEmpty(queryTemplateRequest.getTemplateParameter())) {
			cmsTemplate.setTemplateParameter(queryTemplateRequest.getTemplateParameter());
		}

		// 因为mongodb接口的分页是从0开始(0代表页面上的1)
		page = page - 1;

		// 默认每页显示20条数据
		if (size <= 0) {
			size = 20;
		}

		// 分页信息
		Pageable pageable = PageRequest.of(page, size);

		// 按条件查询
		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
				// 模板名称模糊查询
				.withMatcher("templateName", ExampleMatcher.GenericPropertyMatchers.contains());
		Example example = Example.of(cmsTemplate, exampleMatcher);
		Page pageInfo = cmsTemplateRepository.findAll(example, pageable);
		List<CmsTemplate> cmsTemplateList = pageInfo.getContent();
		long totalElements = pageInfo.getTotalElements();

		// 封装查询结果
		QueryResult<CmsTemplate> queryResult = new QueryResult();
		queryResult.setList(cmsTemplateList);
		queryResult.setTotal(totalElements);

		return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
	}

	public CmsTemplateResult add(CmsTemplate cmsTemplate) {

		// 表单校验
		if (StringUtils.isEmpty(cmsTemplate.getTemplateName()) ||
		StringUtils.isEmpty(cmsTemplate.getSiteId()) ||
		StringUtils.isEmpty(cmsTemplate.getTemplateParameter()) ||
		StringUtils.isEmpty(cmsTemplate.getTemplateFileId())) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}

		// 安全起见设置id为null
		cmsTemplate.setTemplateId(null);

		CmsTemplate saveCmsTemplate = cmsTemplateRepository.save(cmsTemplate);
		return new CmsTemplateResult(CommonCode.SUCCESS, saveCmsTemplate);
	}

	/**
	 * 通过id删除template
	 * @param id
	 * @return
	 */
	public ResponseResult delete(String id) {
		// 判断id对应的template是否存在，如果存在才能够删除
		Optional<CmsTemplate> cmsTemplateOptional = cmsTemplateRepository.findById(id);
		if (! cmsTemplateOptional.isPresent()) {
			ExceptionCast.cast(CmsCode.CMS_TEMPLATE_NOTEXISTS);
		}

		CmsTemplate cmsTemplateInfo = cmsTemplateOptional.get();

		// 删除
		cmsTemplateRepository.delete(cmsTemplateInfo);

		return new ResponseResult(CommonCode.SUCCESS);
	}


	public CmsTemplate findById(String id) {
		Optional<CmsTemplate> optionalCmsTemplate = cmsTemplateRepository.findById(id);
		if (! optionalCmsTemplate.isPresent()) {
			ExceptionCast.cast(CmsCode.CMS_TEMPLATE_NOTEXISTS);
		}

		CmsTemplate cmsTemplate = optionalCmsTemplate.get();
		return cmsTemplate;
	}

	public CmsTemplateResult update(String id, CmsTemplate cmsTemplate) {
		// 表单校验
		if (StringUtils.isEmpty(cmsTemplate.getTemplateName()) ||
				StringUtils.isEmpty(cmsTemplate.getSiteId()) ||
				StringUtils.isEmpty(cmsTemplate.getTemplateParameter()) ||
				StringUtils.isEmpty(cmsTemplate.getTemplateFileId())) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}

		Optional<CmsTemplate> optionalCmsTemplate = cmsTemplateRepository.findById(id);
		if (optionalCmsTemplate.isPresent()) {
			CmsTemplate cmsTemplateInfo = optionalCmsTemplate.get();
			if (StringUtils.isNotEmpty(cmsTemplate.getSiteId())) {
				cmsTemplateInfo.setSiteId(cmsTemplate.getSiteId());
			}

			if (StringUtils.isNotEmpty(cmsTemplate.getTemplateName())) {
				cmsTemplateInfo.setTemplateName(cmsTemplate.getTemplateName());
			}

			if (StringUtils.isNotEmpty(cmsTemplate.getTemplateParameter())) {
				cmsTemplateInfo.setTemplateParameter(cmsTemplate.getTemplateParameter());
			}

			if (StringUtils.isNotEmpty(cmsTemplate.getTemplateFileId())) {
				cmsTemplateInfo.setTemplateFileId(cmsTemplate.getTemplateFileId());
			}

			// 更新
			CmsTemplate saveCmsTemplate = cmsTemplateRepository.save(cmsTemplateInfo);
			if (Objects.isNull(saveCmsTemplate)) {
				ExceptionCast.cast(CommonCode.UPDATE_FAIL);
			}

			// 更新成功
			return new CmsTemplateResult(CommonCode.SUCCESS, saveCmsTemplate);
		}
		return new CmsTemplateResult(CommonCode.SAVE_FAIL);
	}
}
