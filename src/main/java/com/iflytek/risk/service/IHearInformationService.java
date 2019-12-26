package com.iflytek.risk.service;

import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.entity.HearInformation;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 审理信息表  服务类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
public interface IHearInformationService extends IService<HearInformation> {

    /**
     * 公共基础方法
     *
     * @param requestBean
     * @return
     */
    public ResponseBean base(RequestBean requestBean, HttpServletRequest request);
}
