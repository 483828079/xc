package com.xuecheng.manage_cms_client.client.service;

import com.xuecheng.manage_cms_client.client.dao.CmsPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService {
	@Autowired
	CmsPageRepository cmsPageRepository;

}
