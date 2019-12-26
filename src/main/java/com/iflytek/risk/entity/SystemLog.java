package com.iflytek.risk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import com.iflytek.risk.common.BaseSimpleEntityBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统日志表
 * </p>
 *
 * @author 黄智强
 * @since 2019-12-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_system_log")
@ApiModel(value = "SystemLog对象", description = "系统日志表 ")
public class SystemLog extends BaseSimpleEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "操作人信息")
    private String operationData;

    @ApiModelProperty(value = "请求类型")
    private String requestType;

    @ApiModelProperty(value = "请求地址")
    private String requestUrl;

    @ApiModelProperty(value = "请求数据")
    private String requestData;

    @ApiModelProperty(value = "返回数据")
    private String responseData;

    @ApiModelProperty(value = "时间")
    private LocalDateTime requestTime;


}
