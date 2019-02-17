package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    @Autowired
    private XcUserRepository xcUserRepository;
    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;

    /**
     * 根据用户名获取用户信息，用户所属公司,用户对应权限
     * 公司: 用户表有所有用户，讲师和普通用户。 只有讲师有对应公司。
     *       第三张表记录用户和公司id，用户id拥有唯一索引，可以通过用户id去查询对应公司id。
     *       也用来表示一个用户只有一家公司。
     * @param username 用户名
     * @return 用户信息、所属公司、拥有权限信息。
     */
    public XcUserExt getUserExt(String username) {
        XcUserExt xcUserExt = new XcUserExt();

        // 通过用户名查询用户信息
        // 参数不存在
        if (StringUtils.isEmpty(username)) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }

        // 用户信息
        XcUser xcUser = this.findXcUserByUsername(username);
        // 如果用户名对应用户不存在返回null
        if (Objects.isNull(xcUser)) {
            return null;
        }
        // 保存用户信息
        BeanUtils.copyProperties(xcUser, xcUserExt);
        // 用户id
        String userId = xcUser.getId();

        // 根据userId查询companyId
        XcCompanyUser companyUser = xcCompanyUserRepository.findByUserId(userId);
        if (! Objects.isNull(companyUser)) {
            xcUserExt.setCompanyId(companyUser.getCompanyId());
        }

        // 返回用户、用户公司、用户对应权限信息
        return xcUserExt;
    }

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return  用户信息
     */
    public XcUser findXcUserByUsername(String username){
        return xcUserRepository.findXcUserByUsername(username);
    }
}