package com.iflytek.risk.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.catalina.core.ApplicationContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @program: law-risk->Hzq
 * @description: 公共基础方法
 * @author: 黄智强
 * @create: 2019-11-12 21:36
 **/
public abstract class Hzq {

    private static ObjectMapper mapper = new ObjectMapper();
    private static ApplicationContext applicationContext;

    public static <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
        return StringUtils.isEmpty(fromValue) ? null : mapper.convertValue(fromValue, toValueType);
    }

    public static <T> T convertValue(Object fromValue, TypeReference<?> toValueTypeRef) throws IllegalArgumentException {
        return StringUtils.isEmpty(fromValue) ? null : mapper.convertValue(fromValue, toValueTypeRef);
    }

    public static <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
        return StringUtils.isEmpty(fromValue) ? null : mapper.convertValue(fromValue, toValueType);
    }

    public static <T> List<T> convertValue(List<Object> fromValueList, Class<T> toValueType) throws IllegalArgumentException {
        List<T> resultList = new ArrayList<T>();
        for (Object fromValue : fromValueList) {
            resultList.add(convertValue(fromValue, toValueType));
        }
        return StringUtils.isEmpty(fromValueList) ? null : resultList;
    }

    public static <T> List<T> convertValue(List<Object> fromValueList, TypeReference<?> toValueTypeRef) throws IllegalArgumentException {
        List<T> resultList = new ArrayList<T>();
        for (Object fromValue : fromValueList) {
            resultList.add(convertValue(fromValue, toValueTypeRef));
        }
        return StringUtils.isEmpty(fromValueList) ? null : resultList;
    }

    public static <T> List<T> convertValue(List<Object> fromValueList, JavaType toValueType) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        List<T> resultList = new ArrayList<T>();
        for (Object fromValue : fromValueList) {
            resultList.add(convertValue(fromValue, toValueType));
        }
        return StringUtils.isEmpty(fromValueList) ? null : resultList;
    }

    public static String doPost(String url, Map<String, String> param) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
                httpPost.setEntity(entity);
                response = httpClient.execute(httpPost);
                resultString = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    public static String doGet(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            HttpGet httpGet = new HttpGet(url);
            response = httpClient.execute(httpGet);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * @param date
     * @param dateFormat : e.g:yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String formatDateByPattern(Date date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String formatTimeStr = null;
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }

    /***
     * convert Date to cron ,eg.  "0 06 10 15 1 ? 2014"
     * @param date  : 时间点
     * @return
     */
    public static String getCron(java.util.Date date) {
        String dateFormat = "ss mm HH dd MM ?";
        return formatDateByPattern(date, dateFormat);
    }

    public static String changeCronForSpecial(String originCron) {
        String nowCron = Hzq.getCron(new Date());
        String[] nowCrons = nowCron.split(" ");
        String[] originCrons = originCron.split(" ");
        // 不是同一月份
        if (!nowCrons[4].equals(originCrons[4])) {
            return originCron;
        } else if (!nowCrons[3].equals(originCrons[3])) {
            return originCron.substring(0, 12) + "* ?";
        }
        return null;
    }
}
