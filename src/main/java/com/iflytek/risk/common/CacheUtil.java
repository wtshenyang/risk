package com.iflytek.risk.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iflytek.risk.entity.Dictionary;
import com.iflytek.risk.service.IDictionaryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @program: law-risk->CacheConfig
 * @description: 缓存类
 * @author: 黄智强
 * @create: 2019-11-28 15:09
 **/
@Component
public class CacheUtil {
    @Resource
    IDictionaryService dictionaryService;

    /**
     * 获取值
     *
     * @param keyCode
     * @param valueCode
     * @param cacheMap
     * @return
     */
    public String getValueByCode(String keyCode, String valueCode, Map<String, List<Dictionary>> cacheMap) {
        List<Dictionary> list = cacheMap.get(keyCode);
        String valueName = "";
        if (list != null && list.size() > 0) {
            for (Dictionary dictionary : list) {
                if (dictionary.getCode().equals(valueCode)) {
                    valueName = dictionary.getName();
                }
            }
        } else {
            QueryWrapper<Dictionary> queryWrapper = new QueryWrapper<Dictionary>();
            queryWrapper.eq("parent_code", keyCode);
            List<Dictionary> dictionaries = dictionaryService.list(queryWrapper);
            if (dictionaries != null && dictionaries.size() > 0) {
                cacheMap.put(keyCode, dictionaries);
                for (Dictionary dictionary : dictionaries) {
                    if (dictionary.getCode().equals(valueCode)) {
                        valueName = dictionary.getName();
                    }
                }
            }
        }
        return valueName;
    }
}
