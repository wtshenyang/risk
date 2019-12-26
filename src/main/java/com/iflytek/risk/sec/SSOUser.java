package com.iflytek.risk.sec;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @program: law-risk->SSOUser
 * @description: 用户
 * @author: 黄智强
 * @create: 2019-11-14 21:35
 **/
public class SSOUser implements Serializable {
    private static final long serialVersionUID = 2498245261087924075L;
    @Getter
    private String userId;
    @Getter
    private String accountName;
    @Getter
    private String name;
    @Getter
    @Setter
    private Integer userSource;
    @Getter
    @Setter
    private Long timestamp;

    public SSOUser() {
    }

    public SSOUser(SSOUser ssoUser) {
        this.userId = ssoUser.getUserId();
        this.accountName = ssoUser.getAccountName();
        this.name = ssoUser.getName();
        this.userSource = ssoUser.getUserSource();
        this.timestamp = ssoUser.getTimestamp();
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName == null ? null : accountName.trim();
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }
}
