package com.pmcaff.nework.manager.common;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommonInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(CommonInterceptor.class);

    private List<String> excludedUrls;

    public List<String> getExcludedUrls() {
        return excludedUrls;
    }

    public void setExcludedUrls(List<String> excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    /**
     * @param request
     * @param response
     * @Description: 在业务处理器处理请求之前被调用 如果返回false
     * 从当前的拦截器往回执行所有拦截器的afterCompletion(),
     * 再退出拦截器链, 如果返回true 执行下一个拦截器,
     * 直到所有的拦截器都执行完毕 再执行被拦截的Controller
     * 然后进入拦截器链,
     * 从最后一个拦截器往回执行所有的postHandle()
     * 接着再从最后一个拦截器往回执行所有的afterCompletion()
     * @return: boolean
     * @author: SongJia
     * @date: 2016-6-27 下午4:17:51
     */
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        logger.debug("response is [{}]", response);

        //判断session是否过期
        String path = request.getServletPath();
        try {
            Subject subject = SecurityUtils.getSubject();
            if (logger.isDebugEnabled()) logger.debug("interceptor subject is [{}]", subject);
            if (subject == null) {
                if (logger.isDebugEnabled()) logger.debug("interceptor subject is null");
                response.setHeader("token", "timeout");//在响应头设置token状态
            } else {
                if (!ManagerConstants.LOGIN_PATH.equals(path) && !subject.isAuthenticated()) {
                    if (logger.isDebugEnabled()) logger.debug("interceptor session is timeout");
                    response.setHeader("token", "timeout");//在响应头设置token状态
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) logger.debug("interceptor error happens");
            response.setHeader("token", "timeout");//在响应头设置token状态
        }

        //统一处理接口Get、Post、Options请求
        if (judgeMethod(request.getMethod())) {

            setVaryResponseHeader(request, response);
            //option在此结束
            if (request.getMethod().equals(RequestMethod.OPTIONS.toString())) {
                return false;
            }
        }
        return true;
    }

    // 在业务处理器处理请求执行完成后,生成视图之前执行的动作
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        logger.debug("response is [{}]", response);
    }

    /**
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @Description: 在DispatcherServlet完全处理完请求后被调用
     * 当有拦截器抛出异常时,
     * 会从当前拦截器往回执行所有的拦截器的afterCompletion()
     * @author: SongJia
     * @date: 2016-6-27 下午4:27:51
     */
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    public void setVaryResponseHeader(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getHeader("Origin");
        logger.debug("url is [{}]", url);

        List<String> domains = new ArrayList<>();
        //domains.add("http://upload-sec-cdn.static.net");

        if (url != null && (domains.contains(url) || url.startsWith("http://localhost:") || url.startsWith("http://10."))) {
            logger.debug("response add heard [{}]", url);
            response.setHeader("Access-Control-Allow-Origin", url);
        } else {
            logger.debug("response add heard [{}] ", "*");
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE"); //支持的http动作
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader("Access-Control-Expose-Headers", "token");
    }

    public boolean judgeMethod(String method) {
        logger.debug("method is [{}]", method);

        if (method.equals(RequestMethod.OPTIONS.toString())) {
            return true;
        }

        if (method.equals(RequestMethod.GET.toString())) {
            return true;
        }

        if (method.equals(RequestMethod.POST.toString())) {
            return true;
        }

        return false;
    }
}