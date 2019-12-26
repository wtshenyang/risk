package com.iflytek.risk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iflytek.risk.common.BaseEntityBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 提醒记录表
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_remind_todo")
@ApiModel(value="RemindTodo对象", description="提醒记录表 ")
public class RemindTodo extends BaseEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联主键")
    private String relationId;

    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "提醒人员主键")
    private String personnelId;

    @ApiModelProperty(value = "名字")
    private String name;

    @ApiModelProperty(value = "事件名称")
    private String eventTitle;

    @ApiModelProperty(value = "目标邮箱")
    private String targetEmail;

    @ApiModelProperty(value = "提醒日期")

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date remindDate;

    @ApiModelProperty(value = "提醒日期正则")
    private String remindDateRegular;

    @ApiModelProperty(value = "是否已发送 0未发送(默认)，1已发送")
    private Integer sendFlag;


}
