package com.iflytek.risk.common;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public class RiskUtils {
    //集合根据连接符号 转成字符串
    public static String listToString(List list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static List stringToList(String str, String regex) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        String[] strArray = str.split(regex);
        return CollectionUtils.arrayToList(strArray);
    }

    public static String getMapValueToString(Object obj) {
        return !StringUtils.isEmpty(obj) ? (String) obj : "";
    }
}
