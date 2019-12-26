package com.iflytek.risk.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 系统提示枚举
 */
@Getter
public enum SystemMessageEnum {
    // type匹配失败提示
    HANDLE_NOT_IN("type类型匹配失败，可选项(add,addBatch,updateAll,updateAllBatch,updateSelect,updateSelectBatch,deletePhysical,deletePhysicalBatch,deleteLogical,deleteLogicalBatch,getInfoById,getListByCondition,getAll,getPage)"),
    // 参数为空提示
    ENTITY_IS_NULL("参数为空"),
    ;

    SystemMessageEnum(String value) {
        this.value = value;
    }

    @EnumValue
    private String value;
}
