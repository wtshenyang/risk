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
 * 字典表
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_dictionary")
@ApiModel(value = "Dictionary对象", description = "字典表 ")
public class Dictionary extends BaseSimpleEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "编码")
    private String code;

    @ApiModelProperty(value = "排序")
    private Long sort;

    @ApiModelProperty(value = "父主键")
    private String parentId;

    @ApiModelProperty(value = "父编码")
    private String parentCode;


}
