package com.iflytek.risk.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @program: law-risk->EntityEntityBean
 * @description: 实体类基础公共方法
 * @author: 黄智强
 * @create: 2019-11-12 12:10
 **/
public class BaseEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @Setter
    @Getter
    @TableId(type = IdType.UUID)
    private String id;

    @ApiModelProperty(value = "创建时间")
    @Setter
    @Getter
    private Date createTime;

    @ApiModelProperty(value = "创建人")
    @Setter
    @Getter
    private String creator;

    @ApiModelProperty(value = "更新时间")
    @Setter
    @Getter
    private Date updateTime;

    @ApiModelProperty(value = "删除标记 0未删除，1删除")
    @Setter
    @Getter
    @TableLogic
    private Integer deleteFlag;
}
