package com.iflytek.risk.sec;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.entity.SystemLog;
import com.iflytek.risk.enums.CommonConstants;
import com.iflytek.risk.service.ISystemLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Base64Utils;
import org.springframework.util.PathMatcher;
import org.w3c.dom.Document;

import javax.annotation.Resource;
import javax.servlet.FilterConfig;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @program: law-risk->SSOFilter
 * @description: 过滤器
 * @author: 黄智强
 * @create: 2019-11-14 21:03
 **/
@Component
public class SSOFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSOFilter.class);
    @Value("${sso.enable}")
    private boolean ssoEnable;
    @Value("${sso.ssoUrl}")
    private String ssoUrl;
    @Value("${sso.clientUrl}")
    private String clientUrl;
    @Value("${sso.keystorePath}")
    private String keystorePath;
    @Value("${sso.keystorePwd}")
    private String keystorePwd;
    @Value("${sso.excludeUrl}")
    private String excludeUrl;
    @Value("${sso.refreshInterval}")
    private long refreshInterval = 10;
    @Value("${sso.loginUrl}")
    private String loginUrl;
    @Value("${sso.isClassPath}")
    private boolean isClassPath;
    @Value("${systemlog.noLogurl}")
    private String noLogurl;
    private PathMatcher pathMatcher;
    private String[] excludeUrls;
    private String[] noLogurls;
    @Resource
    ISystemLogService systemLogService;

    public SSOFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.pathMatcher = new AntPathMatcher();
        if (StringUtils.isNotEmpty(excludeUrl)) {
            this.excludeUrls = excludeUrl.split(",");
        }

        if (StringUtils.isNotEmpty(noLogurl)) {
            this.noLogurls = noLogurl.split(",");
        }

        System.clearProperty("javax.net.ssl.trustStore");
        String trustStore;
        if (isClassPath) {
            trustStore = Thread.currentThread().getContextClassLoader().getResource("").getPath() + this.keystorePath;
        } else {
            trustStore = this.keystorePath;
        }

        LOGGER.info("----初始化sso-------------------------------------------" + trustStore);
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", this.keystorePwd);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (ssoEnable) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            SystemLog systemLog = new SystemLog();
            systemLog.setRequestUrl(request.getRequestURI());
            systemLog.setRequestType(request.getMethod());
            boolean isSysLogFlag = getSystemLogFlag(request);
            if (null != this.excludeUrls) {
                String[] var6 = this.excludeUrls;
                int var7 = var6.length;
                for (int var8 = 0; var8 < var7; ++var8) {
                    String item = var6[var8];
                    if (this.pathMatcher.match(item, request.getServletPath())) {
                        systemLog.setOperationData("admin");
                        doMyFilter(filterChain, response, request, systemLog, isSysLogFlag);
                        return;
                    }
                }
            }

            SSOUser ssoUser = (SSOUser) request.getSession().getAttribute("sso_user_session");
            if (null != ssoUser && StringUtils.isNotEmpty(ssoUser.getAccountName())) {
                long exitTimes = (System.currentTimeMillis() - ssoUser.getTimestamp()) / 1000L;
                if (exitTimes < this.refreshInterval || this.isLogin(request)) {
                    systemLog.setOperationData(JSONObject.toJSONString(ssoUser));
                    doMyFilter(filterChain, response, request, systemLog, isSysLogFlag);
                    return;
                } else {//登陆超时且未登陆，将session失效
                    request.getSession().removeAttribute("sso_user_session");
                }
            }

            String ticket = request.getHeader("ticket");
            if (this.pathMatcher.match(loginUrl, request.getServletPath()) && StringUtils.isEmpty(ticket)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (StringUtils.isNotEmpty(ticket)) {
                try {
                    SSOUser user = new SSOUser();
                    if (this.validateST(ticket, user, request)) {
                        request.getSession().setAttribute("sso_user_session", user);
                        systemLog.setOperationData(JSONObject.toJSONString(ssoUser));
                        doMyFilter(filterChain, response, request, systemLog, isSysLogFlag);
                        return;
                    }
                } catch (Exception var10) {
                    LOGGER.error("验证ticket失败", var10);
                    response.sendError(500);
                }
            } else {
                if (this.isAjax(request)) {
                    response.setHeader("sessionstatus", "timeout");
                    filterChain.doFilter(servletRequest, servletResponse);
                    return;
                }
            }

            //重新登陆
            redirectLogin(response);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void doMyFilter(FilterChain filterChain, HttpServletResponse response, HttpServletRequest request, SystemLog systemLog, boolean isSysLogFlag) throws IOException, ServletException {
        if (filterChain == null || response == null || request == null) {
            LOGGER.error("filterChain或response或request参数为空");
            return;
        }

        if (!isSysLogFlag) {
            filterChain.doFilter(request, response);
        } else {
            RequestWrapper requestWrapper = new RequestWrapper(request);
            String body = requestWrapper.getBody();
            systemLog.setRequestData(body);
            try {
                systemLogService.save(systemLog);
            } catch (Exception e) {
                LOGGER.error("新增系统日志--报错---" + e.getMessage());
            }

            ResponseWrapper responseWrapper = new ResponseWrapper(response);
            filterChain.doFilter(requestWrapper, responseWrapper);
            updateSystemLog(response, responseWrapper, systemLog);
        }
    }

    /**
     * 更新系统日志
     */
    private void updateSystemLog(HttpServletResponse response, ResponseWrapper responseWrapper, SystemLog systemLog) throws IOException {
        if (response == null || systemLog == null || responseWrapper == null) {
            return;
        }

        try {
            String content = responseWrapper.getContent();
            if (StringUtils.isNotEmpty(content)) {
                // 重置响应输出的内容长度
                response.setContentLength(-1);
                // 输出最终的结果
                PrintWriter out = response.getWriter();
                out.write(content);
                out.flush();
                out.close();
                systemLog.setResponseData(content);
                systemLogService.updateById(systemLog);
            }
        } catch (Exception e) {
            LOGGER.error("更新系统日志--报错---" + e.getMessage());
        }
    }

    /**
     * 获取是否记录系统日志标志
     */
    private boolean getSystemLogFlag(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        //过滤不记录系统日志请求
        if (null != this.noLogurls) {
            String[] var6 = this.noLogurls;
            int var7 = var6.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                String item = var6[var8];
                if (this.pathMatcher.match(item, request.getServletPath())) {
                    return false;
                }
            }
        }

        return true;
    }

    private void redirectLogin(HttpServletResponse response) throws IOException {
        ResponseBean result = new ResponseBean(
                CommonConstants.NOT_LOGIN.getCode(),
                CommonConstants.NOT_LOGIN.getMessage(),
                String.format("%s/login?service=%s", ssoUrl, clientUrl)
        );
        PrintWriter out = response.getWriter();
        out.append(JSONObject.toJSONString(result));
    }

    private boolean validateST(String ticket, SSOUser ssoUser, HttpServletRequest request) throws Exception {
        String url = String.format("%s/p3/serviceValidate?service=%s&ticket=%s", this.ssoUrl, this.getSSOServiceUrl(request), ticket);
        LOGGER.info("ticket验证：" + url);
        HttpGet httpGet = new HttpGet(url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(httpGet);
        String content = EntityUtils.toString(response.getEntity(), "utf-8");
        LOGGER.info("ticket验证结果：" + content);
        if (response.getStatusLine().getStatusCode() == 200) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            Document document = documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(content.getBytes("utf-8")));
            if (document.getElementsByTagName("cas:authenticationSuccess").item(0) != null) {
                ssoUser.setAccountName(document.getElementsByTagName("cas:userAccount").item(0).getTextContent());
                ssoUser.setName(document.getElementsByTagName("cas:userName").item(0).getTextContent());
                ssoUser.setUserSource(Integer.valueOf(document.getElementsByTagName("cas:userSource").item(0).getTextContent()));
                ssoUser.setUserId(document.getElementsByTagName("cas:userId").item(0).getTextContent());
                ssoUser.setTimestamp(System.currentTimeMillis());
                return true;
            }
        }

        return false;
    }

    private boolean isLogin(HttpServletRequest request) throws IOException {
//        String ip = StringUtils.isEmpty(request.getHeader("x-forwarded-for")) ? request.getRemoteAddr() : request.getHeader("x-forwarded-for");
        String ip = getIpAddr(request);
        LOGGER.info("isLogin ip：" + ip);
        String browser = request.getHeader("User-Agent");
        LOGGER.info("browser：" + browser);
        String userSignId = Base64Utils.encodeToString(String.format("%s%s", ip, browser).getBytes());
        LOGGER.info("userSignId：" + userSignId);
        SSOUser tempUser = (SSOUser) request.getSession().getAttribute("sso_user_session");
        String url = String.format("%s/loginState/check?userSignId=%s&userAccount=%s", this.ssoUrl, userSignId, tempUser.getAccountName());
        LOGGER.info("登陆状态验证：" + url);
        HttpGet httpGet = new HttpGet(url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(httpGet);
        String content = EntityUtils.toString(response.getEntity(), "utf-8");
        LOGGER.info("登陆状态验证结果：" + content);
        if (response.getStatusLine().getStatusCode() == 200 && Boolean.valueOf(content)) {
            tempUser.setTimestamp(System.currentTimeMillis());
            return true;
        } else {
            return false;
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forward-for");//负载均衡下为小写
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (StringUtils.isNotEmpty(ip) && ip.contains(", ")) {
            return ip.split(", ")[0];
        }

        return ip;
    }

    private String getSSOServiceUrl(HttpServletRequest request) {
        String url;
        if (null == request.getQueryString()) {
            url = request.getRequestURL().toString();
        } else {
            url = String.format("%s?%s", request.getRequestURL().toString(), request.getQueryString());
        }

        return StringUtils.isEmpty(this.clientUrl) ? url : this.clientUrl;
    }

    private boolean isAjax(HttpServletRequest request) {
        return request.getHeader("X-Requested-With") != null && "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    @Override
    public void destroy() {
    }
}
