package com.xuecheng.manage_cms_client.client.service;

import com.xuecheng.manage_cms_client.client.dao.CmsSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SiteService {
	@Autowired
	CmsSiteRepository cmsSiteRepository;

}
