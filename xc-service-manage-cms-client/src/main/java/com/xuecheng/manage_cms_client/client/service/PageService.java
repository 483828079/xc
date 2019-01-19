package com.xuecheng.manage_cms_client.client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;
import java.util.Optional;

@Service
public class PageService {
	@Autowired
	CmsPageRepository cmsPageRepository;
	@Autowired
	CmsSiteRepository cmsSiteRepository;
	@Autowired
	GridFsTemplate gridFsTemplate;
	@Autowired
	GridFSBucket gridFSBucket;

	/**
	 * 将页面保存到物理路径。
	 * @param pageId 页面的id从消息中获取。
	 */
	public void savePageToServerPath(String pageId){
		// 页面静态化保存到gridFS之后,将fileId存在page表中。
		// 发送页面对应的pageId。
		// 监听Queue获取需要静态化发布的pageId。
		// 可以查询pageId对应的fileId从gridFS中获取html页面替换掉物理路径中的页面。

		// 根据pageId查询对应的cmsPage
		CmsPage cmsPage = this.findCmsPageByPageId(pageId);

		// 页面不存在
		if (Objects.isNull(cmsPage)) {
			ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
		}

		// 根据pageId获取htmlFileId
		String htmlFileId = cmsPage.getHtmlFileId();
		if (StringUtils.isEmpty(htmlFileId)) {
			ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
		}

		// 文件对应的输入流。可以用来读取文件。
		InputStream inputStream = this.getFileById(htmlFileId);
		// 获取要存储html的物理路径
		// 当前page对应的物理路径：站点的物理路径+页面在站点的物理路径+页面名称
		String siteId = cmsPage.getSiteId();
		if (StringUtils.isEmpty(siteId)) {
			ExceptionCast.cast(CmsCode.CMS_SIT_NOTEXISTS);
		}

		// page对应的site
		CmsSite cmsSite= getCmsSiteById(siteId);
		if (Objects.isNull(cmsSite)) {
			ExceptionCast.cast(CmsCode.CMS_SIT_NOTEXISTS);
		}

		// 站点的物理路径
		String sitePhysicalPath = cmsSite.getSitePhysicalPath();
		if (StringUtils.isEmpty(sitePhysicalPath)) {
			ExceptionCast.cast(CmsCode.CMS_SITE_PHYSICAL_PATH_NOTEXISTS);
		}

		// 页面在站点中的路径
		String pagePhysicalPath = cmsPage.getPagePhysicalPath();
		// 页面名称
		String pageName = cmsPage.getPageName();
		if (StringUtils.isEmpty(pagePhysicalPath) || StringUtils.isEmpty(pageName)) {
			ExceptionCast.cast(CmsCode.CMS_PAGE_PHYSICAL_PATH_NOTEXISTS);
		}

		// 写入html的路径
		String path = sitePhysicalPath + pagePhysicalPath + pageName;

		// 将html文件替换
		try {
			OutputStream outputStream = new FileOutputStream(path);
			IOUtils.copy(inputStream, outputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据htmlFileId获取文件的输入流
	 * @param htmlFileId
	 * @return
	 */
	private InputStream getFileById(String htmlFileId) {
		GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
		GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
		GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
		try {
			return gridFsResource.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据pageId获取CmsPage
	 * @param pageId
	 * @return
	 */
	private CmsPage findCmsPageByPageId(String pageId) {
		Optional<CmsPage> cmsPageOptional = cmsPageRepository.findById(pageId);
		if (cmsPageOptional.isPresent()) {
			return cmsPageOptional.get();
		}
		return null;
	}

	//根据站点id得到站点
	public CmsSite getCmsSiteById(String siteId){
		Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
		if(optional.isPresent()){
			CmsSite cmsSite = optional.get();
			return cmsSite;
		}
		return null;
	}
}
