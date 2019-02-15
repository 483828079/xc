package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaFileService;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media/file")
public class MediaFileController implements MediaFileControllerApi {
	@Autowired
	MediaFileService mediaFileService;

	@Autowired
	MediaUploadService mediaUploadService;

	/**
	 * 根据查询条件进行分页查询
	 * @param page 第几页
	 * @param size 每页显示多少条
	 * @param queryMediaFileRequest 查询条件
	 * @return 查询结果
	 */
	@GetMapping("/list/{page}/{size}")
	public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryMediaFileRequest queryMediaFileRequest) {
		return mediaFileService.findList(page,size,queryMediaFileRequest);
	}

	@GetMapping("/process/{id}")
	public ResponseResult mediaProcess(@PathVariable("id") String mediaId) {
		return mediaUploadService.sendProcessVideoMsg(mediaId);
	}
}