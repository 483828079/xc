package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CmsPagePreviewController extends BaseController {
    @Autowired
    PageService pageService;

    //接收到页面id
    @GetMapping(value="/cms/preview/{pageId}")
    public String preview(@PathVariable("pageId")String pageId){
        return pageService.getPageHtml(pageId);
    }
}