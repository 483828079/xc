package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MediaFileService {
	@Autowired
	MediaFileRepository mediaFileRepository;

	/**
	 * 根据查询条件进行分页查询
	 * @param page 第几页
	 * @param size 每页显示多少条
	 * @param queryMediaFileRequest 查询条件
	 * @return 查询结果
	 */
	public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
		if (Objects.isNull(queryMediaFileRequest)) {
			queryMediaFileRequest = new QueryMediaFileRequest();
		}

		// 查询条件
		MediaFile mediaFile = new MediaFile();

		// 原文件名称
		if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
			mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
		}

		// 修改状态
		if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
			mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
		}

		// 标签
		if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
			mediaFile.setTag(queryMediaFileRequest.getTag());
		}

		if (page < 1) {
			page = 1;
		}

		if (size <= 0) {
			size = 20;
		}

		page -= 1;

		// 设置分页条件, 从0开始。
		Pageable pageable = PageRequest.of(page, size);

		// 设置查询条件
		// tag 模糊查询 fileOriginalName 模糊查询
		Example example = Example.of(mediaFile, ExampleMatcher.matching()
						.withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
						.withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains()));


		// 查询
		Page pageResultInfo = mediaFileRepository.findAll(example, pageable);

		// 设置查询结果
		QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<>();
		mediaFileQueryResult.setList(pageResultInfo.getContent());
		mediaFileQueryResult.setTotal(pageResultInfo.getTotalElements());

		// 查询成功,返回查询结果信息
		return new QueryResponseResult(CommonCode.SUCCESS,mediaFileQueryResult);
	}
}