package com.iflytek.risk.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iflytek.risk.entity.RemindTodo;
import com.iflytek.risk.enums.BusinessEnum;
import com.iflytek.risk.service.IRemindTodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @program: law-risk->DynamicScheduleTaskSecond
 * @description: 动态定时任务配置类
 * @author: 黄智强
 * @create: 2019-11-27 10:50
 **/
@Configuration
public class AutoInitSchedule {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingRunnable.class);
    @Resource
    private IRemindTodoService remindTodoService;
    @Resource
    private CronTaskRegistrar cronTaskRegistrar;

    /**
     * 初始化定时任务
     */
    @PostConstruct
    public void initSystemTask() {
        QueryWrapper<RemindTodo> remindTodoQueryWrapper = new QueryWrapper<RemindTodo>();
        remindTodoQueryWrapper.eq("send_flag", BusinessEnum.MAIL_NOT_SEND.getValue()).isNotNull("remind_date_regular");
        List<RemindTodo> remindTodoList = remindTodoService.list(remindTodoQueryWrapper);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (remindTodoList != null && remindTodoList.size() > 0) {
            for (RemindTodo remindTodo : remindTodoList) {
                String remindId = remindTodo.getId();
                String cron = remindTodo.getRemindDateRegular();
                if (!StringUtils.isEmpty(cron)) {
                    logger.info("初始化定时任务 - 任务名称：{}，任务主键：{}， 执行时间： {}， 目标邮箱：{}",
                            remindTodo.getEventTitle(),
                            remindTodo.getId(),
                            sdf.format(remindTodo.getRemindDate()),
                            remindTodo.getTargetEmail()
                    );
                    SchedulingRunnable task = new SchedulingRunnable("remindTodoServiceImpl", "sendMail", remindId);
                    cronTaskRegistrar.addCronTask(task, cron);
                    // cronTaskRegistrar.addCronTask(task, "0/5 * * * * ? ");
                }
            }
        } else {
            logger.info("无定时任务～");
        }
    }

}
