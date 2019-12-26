package com.iflytek.risk.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 接口地址枚举类
 */
@Getter
public enum InterfaceUrlEnum {
    // uap
    UAP_GETBYAPPCODE_APP("/app/getByAppCode", "POST", "根据应用编码获取应用信息"),
    UAP_GETBYLOGINNAM("/getByLoginNam", "POST", "根据登录名获取用户基本信息"),
    UAP_GETBYAPPIDANDUSERID_AUTH("/auth/getByAppIdAndUserId", "POST", "根据应用Id和用户Id获取功能权限信息列表"),
    UAP_GETBYAPPIDANDUSERID_RESOURCE("/resource/getByAppIdAndUserId", "POST", "根据应用Id和用户Id获取资源信息列表"),
    UAP_GETBYAPPCODE_ROLE("/role/getByAppCode", "POST", "根据应用编码获取角色信息列表"),
    UAP_GETBYROLEID("/user/getByRoleId", "POST", "根据应用编码获取角色信息列表"),

    // ps
    PS_DEMO("demo", "POST", "调试"),
    ;

    InterfaceUrlEnum(String value, String method, String desc) {
        this.value = value;
        this.method = method;
        this.desc = desc;
    }

    @EnumValue
    private String value;
    @EnumValue
    private String method;
    @EnumValue
    private String desc;
}
