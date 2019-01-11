package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Test // 查询所有
    public void testFindAll() throws Exception{
        List<CmsPage> cmsPageList = cmsPageRepository.findAll();
        for (CmsPage cmsPage : cmsPageList) {
            System.out.println(cmsPage);
        }
    }

    @Test // 分页查询
    public void testQueryPage() throws Exception{
        int page = 0; // 第几页，从0开始。
        int size = 10; // 显示多少条数据。
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> cmsPageList = cmsPageRepository.findAll(pageable);
        for (CmsPage cmsPage : cmsPageList) {
            System.out.println(cmsPage);
        }
    }

    //添加
    @Test
    public void testInsert(){
        //定义实体类
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("s01");
        cmsPage.setTemplateId("t01");
        cmsPage.setPageName("测试页面");
        cmsPage.setPageCreateTime(new Date());

        List<CmsPageParam> cmsPageParams = new ArrayList<>();

        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");

        cmsPageParams.add(cmsPageParam);
        // 可以看出来如果包含一个对象，也可以同时将该对象存储在mongodb中作为一个field
        // 会自动生成主键
        cmsPage.setPageParams(cmsPageParams);
        cmsPageRepository.save(cmsPage);

        System.out.println(cmsPage);
    }
    
    @Test // 通过id进行删除
    public void testDelete() throws Exception{
        cmsPageRepository.deleteById("5c3781c964732f10c05ad4a1");
    }

    @Test // 修改
    public void testUpdate() throws Exception{
		CmsPage cmsPage = new CmsPage();
		cmsPage.setPageId("5c382f06a97f3a23f8c03263");
		cmsPage.setPageName("测试修改");

		// 如果有id会被认为是修改。
		cmsPageRepository.save(cmsPage);
    }

    @Test // 通过id查询
    public void testQueryById() throws Exception{
        /**
         * Optional是jdk1.8引入的类型，Optional是一个容器对象，
         * 它包括了我们需要的对象，使用isPresent方法判断所包含对象是否为空，
         * isPresent方法返回false则表示Optional包含对象为空，否则可以使用get()取出对象进行操作。
         * Optional的优点是：
         * 1、提醒你非空判断。
         * 2、将对象非空检测标准化。
         * */
        Optional<CmsPage> optional = cmsPageRepository.findById("5c382f06a97f3a23f8c03263");
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            System.out.println(cmsPage);
        }
    }
    
    @Test // 测试自定义DAO方法
    public void testQueryByPageName() throws Exception{
        CmsPage cmsPage = cmsPageRepository.findByPageName("测试修改");
        System.out.println(cmsPage);
    }

    @Test // 按照siteId和pageType分页查询
    public void testfindBySiteIdAndPageType() throws Exception{
        Pageable pageable = PageRequest.of(0, 2, new Sort(Sort.Direction.DESC, "siteId"));
        Page<CmsPage> page = cmsPageRepository.findBySiteIdAndPageType("233", "1", pageable);
        List<CmsPage> cmsPageList = page.getContent();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();
    }
}