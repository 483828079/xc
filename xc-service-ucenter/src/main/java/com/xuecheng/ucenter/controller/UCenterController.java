package com.xuecheng.ucenter.controller;

import com.xuecheng.api.ucenter.UcenterControllerApi;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ucenter")
public class UCenterController implements UcenterControllerApi {
    @Autowired
    UserService userService;

    /**
     * 根据用户名获取用户信息，用户所属公司,用户对应权限
     * 公司: 用户表有所有用户，讲师和普通用户。 只有讲师有对应公司。
     *       第三张表记录用户和公司id，用户id拥有唯一索引，可以通过用户id去查询对应公司id。
     *       也用来表示一个用户只有一家公司。
     * @param username 用户名
     * @return 用户信息、所属公司、拥有权限信息。
     */
    @GetMapping("/getuserext")
    public XcUserExt getUserext(@RequestParam("username") String username) {
        XcUserExt xcUser = userService.getUserExt(username);
        return xcUser;
    }
  }