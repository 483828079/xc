package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;

@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {
    /**
     * 通过用户名获取用户信息，权限，企业id
     * @param username
     * @return
     */
    public XcUserExt getUserext(String username);
}