package com.xuecheng.auth.service;

import com.xuecheng.auth.client.UserClient;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;
    @Autowired
    UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if(authentication==null){
            // 认证客户端id，客户端密码
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if(clientDetails!=null){
                //密码
                String clientSecret = clientDetails.getClientSecret();
                return new User(username,clientSecret,AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }

        if (StringUtils.isEmpty(username)) {
            return null;
        }

        // 认证用户名密码
        // 通过用户名查询用户对应信息
        XcUserExt userext = userClient.getUserext(username);
        if(userext == null){
            return null;
        }

        //取出正确密码（hash值）
        String password = userext.getPassword();
        // 防止遍历权限的时候空指针异常，如果权限集合为null先初始化集合。
        if (Objects.isNull(userext.getPermissions())) {
            userext.setPermissions(new ArrayList<>());
        }
        // 获取权限信息集合
        List<XcMenu> permissions = userext.getPermissions();
        // 取出权限的code属性放在集合中,之后作为用户拥有的权限
        List<String> user_permission = new ArrayList<>();
        permissions.forEach(item-> user_permission.add(item.getCode()));

        // 将数组切割为用,分割的字符串。
        // jdk8中提供了String.join方法效果相同。
        // String.join(",", user_permission);
        String user_permission_string  = StringUtils.join(user_permission.toArray(), ",");


        // 用户名密码权限，初始化User。用来做认证授权。
        UserJwt userDetails = new UserJwt(username,
                password,
                // 将用,分割的permission code转换为List<GrantedAuthority> 权限集合。
                AuthorityUtils.commaSeparatedStringToAuthorityList(user_permission_string));

        // 增加一些用户的其他信息
        userDetails.setId(userext.getId());
        userDetails.setUtype(userext.getUtype());//用户类型
        userDetails.setCompanyId(userext.getCompanyId());//所属企业
        userDetails.setName(userext.getName());//用户名称
        userDetails.setUserpic(userext.getUserpic());//用户头像

       /* UserDetails userDetails = new org.springframework.security.core.userdetails.User(username,
                password,
                AuthorityUtils.commaSeparatedStringToAuthorityList(""));*/
//                AuthorityUtils.createAuthorityList("course_get_baseinfo","course_get_list"));
        return userDetails;
    }
}
