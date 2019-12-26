package com.iflytek.risk.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CommonConstants {
    // 成功
    SUCCESS("0", "成功"),
    // 失败
    FAIL("1", "失败"),
    // 无权操作
    NOAUTH("2", "无权操作~"),
    // 不可重复操作，请刷新页面
    FLUSHPAGE("3", "不可重复操作，请刷新页面~"),
    // 未登陆
    NOT_LOGIN("4", "未登录");


    @EnumValue
    private final String code;
    private final String message;

    private CommonConstants(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
