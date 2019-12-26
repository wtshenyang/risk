package com.iflytek.risk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.entity.RemindTodo;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 提醒记录表  服务类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
public interface IRemindTodoService extends IService<RemindTodo> {

    /**
     * 公共基础方法
     *
     * @param requestBean
     * @return
     */
    public ResponseBean base(RequestBean requestBean, HttpServletRequest request);

    /**
     * @param remindId 正则
     */
    public void sendMail(String remindId);
}
