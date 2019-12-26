package com.iflytek.risk.sec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @program: law-risk->FilterConfig
 * @description: 过滤器配置类
 * @author: 黄智强
 * @create: 2019-11-14 20:58
 **/
@Configuration
public class FilterConfig {

    @Resource
    SSOFilter ssoFilter;

    @Bean
    public FilterRegistrationBean registerAuthFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean<>();
        registration.setFilter(ssoFilter);
        //过滤规则
        registration.addUrlPatterns("/*");
        //过滤器名称
        registration.setName("ssoFilter");
        //过滤器顺序
        registration.setOrder(1);
        return registration;
    }
}
