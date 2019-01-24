package com.xuecheng.manage_course.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @FeignClient 用来绑定接口对应的服务。
 * feign的客户端接口，对应的EurekaService中的SERVICE_MANAGE_CMS名称的服务。
 */
@FeignClient(value = XcServiceList.XC_SERVICE_MANAGE_CMS)
public interface CmsPageClient {
    /**
     * 支持SpringMVC注解，GetMapping的值相当于要请求的地址，请求方式就是Get。
     * 必须对应调用方Controller的接口。
     * feignClient接口 有参数在参数必须加@PathVariable("XXX")和@RequestParam("XXX")
     * feignClient返回值为复杂对象时其类型必须有无参构造函数。
     * @param id
     * @return
     */
    @GetMapping("/cms/page/get/{id}")
    public CmsPage findById(@PathVariable("id") String id);

    //保存页面
    @PostMapping("/cms/page/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage);
}
