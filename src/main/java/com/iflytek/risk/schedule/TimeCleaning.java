package com.iflytek.risk.schedule;

import com.iflytek.risk.service.ISystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @program: law-risk->TimeCleaning
 * @description: 定时清理系统日志表数据定时器
 * @author: 陈花梅
 * @create: 2019-12-26 15:12
 **/
@Component
public class TimeCleaning {
    private static final Logger logger = LoggerFactory.getLogger(TimeCleaning.class);

    @Value("${systemlog.deleteLog.month}")
    private Integer deleteLogMonth;

    @Autowired
    private ISystemLogService systemLogService;

//    @Scheduled(cron = "0 */1 * * * ?")
    public void timeCleaning() {
        try {
            logger.error("[开始删除系统日志Log  " + deleteLogMonth + "  月前的数据]------");
            systemLogService.deleteLog(deleteLogMonth);
        } catch (Exception e) {
            logger.error("[删除系统日志Log  " + deleteLogMonth + "  月前的数据失败]");
        }
    }
}
