package com.iflytek.risk.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UapUrlEnum {

    GET_USER_INFO_BY_LOGIN_NAME("/user/getByLoginNam", "POST", "提供根据登录名获取用户基本信息服务"),
    GET_APP_INFO_BY_APP_CODE("/app/getByAppCode", "POST", "提供根据应用编码获取应用信息服务"),
    GET_AUTH_BY_APP_ID_USER_ID("/auth/getByAppIdAndUserId", "POST", "提供根据应用Id和用户Id获取功能权限信息列表服务"),
    GET_SOURCE_BY_APP_ID_USER_ID("/resource/getByAppIdAndUserId", "POST", "提供根据应用Id和用户Id获取资源信息列表服务"),
    GET_PS_PERSONS("/LegalManageSystem_20191121/getPersonData_rest_7720", "POST", "获取ps人员数据"),
    GET_USER_INFO_BY_ROLE_ID("/user/getByRoleId", "POST", "提供根据角色id获取用户信息列表服务"),

    ;

    UapUrlEnum(String url, String methodType, String desc) {
        this.url = url;
        this.methodType = methodType;
        this.desc = desc;
    }

    @EnumValue
    private String url;
    @EnumValue
    private String methodType;
    @EnumValue
    private String desc;
}
