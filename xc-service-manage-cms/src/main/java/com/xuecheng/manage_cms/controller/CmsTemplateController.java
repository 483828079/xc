package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsTemplateControllerApi;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/template")
public class CmsTemplateController implements CmsTemplateControllerApi {
	@Autowired
	TemplateService templateService;

	@Override // 查询所有的template
	@GetMapping("/all")
	public QueryResponseResult findAll() {
		return templateService.findAll();
	}

	@GetMapping("/list/{page}/{size}")
	public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryTemplateRequest queryTemplateRequest) {
		return templateService.findList(page, size, queryTemplateRequest);
	}

	@PostMapping("/add")
	public CmsTemplateResult add(@RequestBody CmsTemplate cmsTemplate) {
		return templateService.add(cmsTemplate);
	}

	@DeleteMapping("/del/{id}")
	public ResponseResult delete(@PathVariable("id") String id) {
		return templateService.delete(id);
	}
}
