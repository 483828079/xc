package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 *
 * 本次定义页面查询接口，本接口供前端请求查询页面列表，
 * 支持分页及自定义条件查询方式。
 *
 * 具体需求如下：
 * 1、分页查询CmsPage 集合下的数据
 * 2、根据站点Id、模板Id、页面别名查询页面信息
 * 3、接口基于Http Get请求，响应Json数据
 * */

@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {
	/**
	 * 查询所有的CmsPage，分页并且按条件。
	 * */

	@ApiOperation("分页查询页面列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
			@ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
	})
	QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) ;
}
