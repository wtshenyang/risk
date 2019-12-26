package com.iflytek.risk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.entity.LitigationCases;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 诉讼类案件表  服务类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
public interface ILitigationCasesService extends IService<LitigationCases> {

    /**
     * 公共基础方法
     *
     * @param requestBean
     * @return
     */
    public ResponseBean base(RequestBean requestBean, HttpServletRequest request);

    /**
     * 接口数据处理
     *
     * @param requestBean 请求参数
     * @return ResponseBean
     */
    public ResponseBean interfaceHandle(RequestBean requestBean);
}
