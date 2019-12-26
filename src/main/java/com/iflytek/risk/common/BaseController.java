package com.iflytek.risk.common;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: law-risk->BaseController
 * @description: 公共控制层方法
 * @author: 黄智强
 * @create: 2019-11-12 12:11
 **/
public abstract class BaseController {

    @ApiOperation(value = "基础处理接口", notes = "基础处理接口")
    @PostMapping("/base")
    @ResponseBody
    public ResponseBean baseOrigin(@RequestBody RequestBean requestBean, HttpServletRequest request) {
        return this.base(requestBean, request);
    }

    public abstract ResponseBean base(RequestBean requestBean, HttpServletRequest request);
}
