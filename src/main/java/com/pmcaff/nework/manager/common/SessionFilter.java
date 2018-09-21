package com.pmcaff.nework.manager.common;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * created on 2017年5月29日
 *
 * session过滤器，修改response对象，设置session超时标识
 *
 * @author megagao
 * @version 0.0.1
 */
public class SessionFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();
        Subject currentUser = SecurityUtils.getSubject();
        if (!ManagerConstants.LOGIN_PATH.equals(path) && !SecurityUtils.getSubject().isAuthenticated()) {
            response.setHeader("session-status", "timeout");//在响应头设置session状态
        }
        filterChain.doFilter(request, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
