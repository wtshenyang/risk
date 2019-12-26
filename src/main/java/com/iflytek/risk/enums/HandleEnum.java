package com.iflytek.risk.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * 操作枚举
 */
@Getter
public enum HandleEnum {
    //默认操作
    ADD("add", "新增"),
    ADD_BATCH("addBatch", "批量新增"),
    UPDATE_ALL("updateAll", "更新所有字段"),
    UPDATE_SELECT("updateSelect", "更新有值字段"),
    UPDATE_ALL_BATCH("updateAllBatch", "批量更新所有字段"),
    UPDATE_SELECT_BATCH("updateSelectBatch", "批量更新有值字段"),
    DELETE_PHYSICAL("deletePhysical", "物理删除"),
    DELETE_LOGICAL("deleteLogical", "逻辑删除"),
    DELETE_PHYSICAL_BATCH("deletePhysicalBatch", "物理删除"),
    DELETE_LOGICAL_BATCH("deleteLogicalBatch", "逻辑删除"),
    GET_INFO_BY_ID("getInfoById", "获取单条数据"),
    GET_LIST_BY_CONDITION("getListByCondition", "根据条件查询多个"),
    GET_ALL("getAll", "查询全部"),
    GET_PAGE("getPage", "查询全部"),
    EMPTY("", "空"),
    // 根据业务需要，增加积累
    DELETE_AND_ADD("deleteAndAdd", "删除之前的数据，并新增"),
    UPDATE_SELF_INFO("updateSelfInfo", "更新主表自己");

    HandleEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @EnumValue
    private String value;
    private String desc;

    public static HandleEnum getTypeByValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return EMPTY;
        }
        for (HandleEnum enums : HandleEnum.values()) {
            if (enums.getValue().equals(value)) {
                return enums;
            }
        }
        return EMPTY;
    }
}
