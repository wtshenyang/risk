package com.iflytek.risk.controller;

import com.alibaba.fastjson.JSONObject;
import com.iflytek.risk.common.Hzq;
import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.enums.CommonConstants;
import com.iflytek.risk.enums.HandleEnum;
import com.iflytek.risk.enums.UapUrlEnum;
import com.iflytek.risk.sec.SSOUser;
import com.iflytek.risk.service.IDictionaryService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: law-risk->IndexController
 * @description: 系统类
 * @author: 黄智强
 * @create: 2019-11-18 20:39
 **/
@RestController
@RequestMapping("/risk/index")
public class IndexController {
    protected Log log = LogFactory.getLog(this.getClass());

    @Value("${sso.ssoUrl}")
    private String ssoUrl;
    @Value("${sso.clientUrl}")
    private String clientUrl;
    @Value("${appCode}")
    private String appCode;
    @Value("${interfaceUrl.uap}")
    private String uapRootUrl;
    @Value("${adminRoleId}")
    private String adminRoleId;
    @Resource
    IDictionaryService dictionaryService;

    /**
     * 登陆校验
     *
     * @param request
     */
    @GetMapping("/login")
    public ResponseBean loginValidate(HttpServletRequest request) {
        SSOUser ssoUser = (SSOUser) request.getSession().getAttribute("sso_user_session");
        if (ssoUser == null) {
            return new ResponseBean(
                    CommonConstants.NOT_LOGIN.getCode(),
                    CommonConstants.NOT_LOGIN.getMessage(),
                    String.format("%s/login?service=%s", ssoUrl, clientUrl)
            );
        }
        log.debug("已登陆～");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userInfo", ssoUser);
        map.put("dictionaryList", dictionaryService.base(new RequestBean("getAll", HandleEnum.GET_ALL, null, null, null), request).getData());
        // 获取用户信息
        String userId = "";
        Map<String, String> paramForGetUserDetail = new HashMap<String, String>();
        paramForGetUserDetail.put("loginName", ssoUser.getAccountName());
        Object userOriginData = JSONObject.parse(Hzq.doPost(uapRootUrl + UapUrlEnum.GET_USER_INFO_BY_LOGIN_NAME.getUrl(), paramForGetUserDetail));
        boolean resultFlag2 = (boolean) ((JSONObject) userOriginData).get("result");
        if (resultFlag2) {
            JSONObject userDetail = (JSONObject) ((JSONObject) userOriginData).get("content");
            map.put("userDetail", userDetail);
            userId = (String) userDetail.get("id");
        }
        // 获取应用基本信息
        String appId = "";
        Map<String, String> paramForGetApp = new HashMap<String, String>();
        paramForGetApp.put("appCode", appCode);
        Object appOriginData = JSONObject.parse(Hzq.doPost(uapRootUrl + UapUrlEnum.GET_APP_INFO_BY_APP_CODE.getUrl(), paramForGetApp));
        boolean resultFlag = (boolean) ((JSONObject) appOriginData).get("result");
        if (resultFlag) {
            JSONObject appInfo = (JSONObject) ((JSONObject) appOriginData).get("content");
            map.put("appInfo", appInfo);
            appId = (String) appInfo.get("id");
            appCode = (String) appInfo.get("code");
        }
        // 获取权限信息
        if (!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(appId)) {
            Map<String, String> paramForGetAuth = new HashMap<String, String>();
            paramForGetAuth.put("appId", appId);
            paramForGetAuth.put("userId", userId);
            Object authOriginData = JSONObject.parse(Hzq.doPost(uapRootUrl + UapUrlEnum.GET_AUTH_BY_APP_ID_USER_ID.getUrl(), paramForGetAuth));
            boolean authResultFlag = (boolean) ((JSONObject) authOriginData).get("result");
            if (authResultFlag) {
                List authList = (List) ((JSONObject) authOriginData).get("content");
                map.put("authList", authList);
            }
        }

        // 获取资源信息
        if (!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(appId)) {
            Map<String, String> paramForGetSource = new HashMap<String, String>();
            paramForGetSource.put("appId", appId);
            paramForGetSource.put("userId", userId);
            Object sourceOriginData = JSONObject.parse(Hzq.doPost(uapRootUrl + UapUrlEnum.GET_SOURCE_BY_APP_ID_USER_ID.getUrl(), paramForGetSource));
            boolean sourceResultFlag = (boolean) ((JSONObject) sourceOriginData).get("result");
            if (sourceResultFlag) {
                List sourceList = (List) ((JSONObject) sourceOriginData).get("content");
                map.put("sourceList", sourceList);
            }
        }

        //判断当前用户是否是管理员
        boolean isAdmin = false;
        if (!StringUtils.isEmpty(adminRoleId) && !StringUtils.isEmpty(userId)) {
            Map<String, String> paramForGetSource = new HashMap<String, String>();
            paramForGetSource.put("roleId", adminRoleId);
            paramForGetSource.put("pageNum", "1");
            paramForGetSource.put("pageSize", "100");
            Object sourceOriginData = JSONObject.parse(Hzq.doPost(uapRootUrl + UapUrlEnum.GET_USER_INFO_BY_ROLE_ID.getUrl(), paramForGetSource));
            boolean sourceResultFlag = (boolean) ((JSONObject) sourceOriginData).get("result");
            if (sourceResultFlag) {
                List<JSONObject> userList = (List) ((JSONObject) sourceOriginData).get("content");
                if (!CollectionUtils.isEmpty(userList)) {
                    for (JSONObject user : userList) {
                        if (userId.equals(user.get("id"))) {
                            isAdmin = true;
                            break;
                        }
                    }
                }
            }
        }

        request.getSession().setAttribute("isAdmin", isAdmin);
        return new ResponseBean(map);
    }

    /**
     * 退出校验
     *
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public ResponseBean loginOut(HttpServletRequest request) {
        //清除局部session
        request.getSession().invalidate();
        log.debug("退出成功～");
        //跳转sso
        return new ResponseBean(String.format("%s/logout?service=%s", ssoUrl, clientUrl));
    }
}
