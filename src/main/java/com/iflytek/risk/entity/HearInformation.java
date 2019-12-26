package com.iflytek.risk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import com.iflytek.risk.common.BaseEntityBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 审理信息表 
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_hear_information")
@ApiModel(value="HearInformation对象", description="审理信息表 ")
public class HearInformation extends BaseEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联主键")
    private String relationId;

    @ApiModelProperty(value = "审理机构")
    private String hearingOrgan;

    @ApiModelProperty(value = "审理程序")
    private String hearingProcedure;

    @ApiModelProperty(value = "立案受理时间")
    private Date filingDate;

    @ApiModelProperty(value = "开庭时间")
    private Date openDate;

    @ApiModelProperty(value = "案件分析")
    private String caseAnalysis;

    @ApiModelProperty(value = "初步处理/答复意见")
    private String preliminaryOpinion;

    @ApiModelProperty(value = "结案方式")
    private String closingMethod;

    @ApiModelProperty(value = "顺序")
    private Integer sort;


}
