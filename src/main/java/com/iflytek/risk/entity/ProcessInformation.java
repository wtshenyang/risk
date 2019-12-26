package com.iflytek.risk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iflytek.risk.common.BaseSimpleEntityBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 进展信息表
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_process_information")
@ApiModel(value = "ProcessInformation对象", description = "进展信息表")
public class ProcessInformation extends BaseSimpleEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联主键")
    private String relationId;

    @ApiModelProperty(value = "数据类型")
    private String dataType;

    @ApiModelProperty(value = "进展内容")
    private String content;

    @ApiModelProperty(value = "顺序")
    private Long sort;

}
