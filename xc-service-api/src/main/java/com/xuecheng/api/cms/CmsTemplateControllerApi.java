package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="cms站点管理接口",description = "cms模板管理接口，提供页面的增、删、改、查")
public interface CmsTemplateControllerApi {
	@ApiOperation("查询所有的Site信息")
	QueryResponseResult findAll() ;

	@ApiOperation("分页查询页面列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
			@ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
	})
	QueryResponseResult findList(int page, int size, QueryTemplateRequest queryTemplateRequest) ;

	@ApiOperation("添加模板")
	public CmsTemplateResult add(CmsTemplate cmsTemplate);

	@ApiOperation("通过ID查询模板")
	public CmsTemplate findById(String id);

	@ApiOperation("修改模板")
	public CmsTemplateResult edit(String id, CmsTemplate cmsTemplate);

	@ApiOperation("通过ID删除模板")
	public ResponseResult delete(String id);
}
