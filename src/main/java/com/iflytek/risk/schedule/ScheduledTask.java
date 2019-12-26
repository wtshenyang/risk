package com.iflytek.risk.schedule;

import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

/**
 * @program: law-risk->ScheduledTask
 * @description: 定时任务控制类
 * @author: 黄智强
 * @create: 2019-11-27 12:11
 **/
@Component
public final class ScheduledTask {

    public volatile ScheduledFuture<?> future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }
}
