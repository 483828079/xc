package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {
	@Autowired
	PageService pageService;

	@Override
	/**
	 * @RequestMapping( method = {RequestMethod.GET}) 等同于，限制请求为GET。
	 * @PathVariable 可以将占位符绑定的值绑定到参数上。
	 * */
	@GetMapping("/list/{page}/{size}")
	public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {
		return pageService.findList(page, size, queryPageRequest);
	}

	@Override
	@PostMapping("/add") // 保存使用post，这里接收一个json格式字符串。
	public CmsPageResult add(@RequestBody CmsPage cmsPage) {
		return pageService.add(cmsPage);
	}

	@Override
	@GetMapping("/get/{id}")
	public CmsPage findById(@PathVariable("id") String id) {
		return pageService.findById(id);
	}

	/**
	 *
	 * @param id 从url中获取
	 * @param cmsPage cmsPage从请求体中的json字符串转换
	 * @return
	 */
	@Override
	@PutMapping("/edit/{id}") // 修改需要使用put请求
	public CmsPageResult edit(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
		return pageService.update(id, cmsPage);
	}

	@Override
	@DeleteMapping("/del/{id}") // 删除用delete请求
	public ResponseResult delete(@PathVariable("id") String id) {
		return pageService.delete(id);
	}
}
