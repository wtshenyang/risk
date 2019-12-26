package com.iflytek.risk.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * <p>
 * 系统日志表  Mapper 接口
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-22
 */
public interface SystemMapper {
    @Select("select now() from dual")
    Date getNow();
}
