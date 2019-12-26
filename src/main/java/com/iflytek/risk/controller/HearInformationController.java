package com.iflytek.risk.controller;


import com.iflytek.risk.common.BaseController;
import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.service.IHearInformationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 审理信息表  前端控制器
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
@RestController
@RequestMapping("/risk/hear-information")
public class HearInformationController extends BaseController {

    @Resource
    IHearInformationService businessService;

    @Override
    public ResponseBean base(RequestBean requestBean, HttpServletRequest request) {
        return businessService.base(requestBean, request);
    }
}
