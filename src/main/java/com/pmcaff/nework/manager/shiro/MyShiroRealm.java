package com.pmcaff.nework.manager.shiro;

import com.pmcaff.nework.manager.common.ManagerConstants;
import com.pmcaff.nework.manager.domain.SysPermission;
import com.pmcaff.nework.manager.domain.SysRole;
import com.pmcaff.nework.manager.domain.SysUser;
import com.pmcaff.nework.manager.service.SysPermissionService;
import com.pmcaff.nework.manager.service.SysRoleService;
import com.pmcaff.nework.manager.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

/**
 * Created by Administrator on 2018/4/30.
 */
public class MyShiroRealm extends AuthorizingRealm {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysPermissionService permissionService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String username = (String) principals.getPrimaryPrincipal();
        SysUser user = sysUserService.findByUsername(username);
        Set<SysRole> roles = sysUserService.getRoles(user);
        for (SysRole role : roles) {
            //超级管理员返回所有权限和角色
            if (ManagerConstants.SUPER_ADMIN.equals(role.getRoleName())) {
                for (SysPermission p : permissionService.listAllUsedPermission()) {
                    if (!StringUtils.isEmpty(p.getPercode())) {
                        authorizationInfo.addStringPermission(p.getPercode());
                    }
                }
                List<String> roleNames = sysRoleService.listAllUsedRoleNames();
                if (roleNames != null && roleNames.size() > 0) {
                    authorizationInfo.addStringPermissions(roleNames);
                }
                return authorizationInfo;
            }
            authorizationInfo.addRole(role.getRoleName());
        }
        Set<String> codes = sysUserService.getUserPermsCode(user);
        if (codes != null && codes.size() > 0) {
            authorizationInfo.addStringPermissions(codes);
        }
        return authorizationInfo;
    }

    /*主要是用来进行身份认证的，也就是说验证用户输入的账号和密码是否正确。*/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        //获取用户的输入的账号.
        String username = (String) token.getPrincipal();
//        System.out.println(token.getCredentials());
        //通过username从数据库中查找 User对象，如果找到，没找到.
        //实际项目中，这里可以根据实际情况做缓存，如果不做，Shiro自己也是有时间间隔机制，2分钟内不会重复执行该方法
        SysUser user = sysUserService.findByUsername(username);
        if (user == null) {
            return null;
        }
        if ("1".equals(user.getLocked())) { //账户冻结
            throw new LockedAccountException();
        }
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                username, //用户名
                user.getPassword(), //密码
                null,//salt
                getName()  //realm name
        );
        return authenticationInfo;
    }
}
