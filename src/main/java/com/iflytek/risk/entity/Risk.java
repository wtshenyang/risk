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
 * 风险信息管理表
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_risk")
@ApiModel(value="Risk对象", description="风险信息管理表")
public class Risk extends BaseEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "登记日期")
    private Date registrateTime;

    @ApiModelProperty(value = "风险发生法人主体")
    private String legalEntity;

    @ApiModelProperty(value = "业务部门")
    private String businessUnit;

    @ApiModelProperty(value = "风险等级")
    private String riskGrade;

    @ApiModelProperty(value = "风险类型")
    private String riskType;

    @ApiModelProperty(value = "服务人员")
    private String servicePersonal;

    @ApiModelProperty(value = "协作人员")
    private String cooperationPerson;

    @ApiModelProperty(value = "风险事项")
    private String riskMatter;

    @ApiModelProperty(value = "风险分析")
    private String riskAnalysis;

    @ApiModelProperty(value = "初步应对策略")
    private String preliminaryOpinion;

    @ApiModelProperty(value = "事件进展")
    private String eventProgress;

    @ApiModelProperty(value = "总结/复盘")
    private String summary;

    @ApiModelProperty(value = "事件状态")
    private String eventStatus;


}
