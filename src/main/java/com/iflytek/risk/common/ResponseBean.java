package com.iflytek.risk.common;

import com.iflytek.risk.enums.CommonConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 统一结果集
 *
 * @author yinfan
 * @date 2017年3月14日上午10:45:52
 */
@ApiModel(value = "ResponseBean", description = "基础返回类")
public class ResponseBean<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6351210629803310701L;

    /**
     * 0 成功 1 失败
     */
    @ApiModelProperty(example = "0", value = "0-成功，1-失败，2-无权操作，3-不可重复操作，4-未登陆")
    @Getter
    @Setter
    private String code;

    /**
     * 错误信息
     */
    @ApiModelProperty(example = "操作失败", value = "返回信息")
    @Getter
    @Setter
    private String msg;

    /**
     * 业务数据
     */
    @ApiModelProperty(example = "{}", value = "结果数")
    @Getter
    @Setter
    private T data;

    public ResponseBean() {
        super();
        this.code = CommonConstants.SUCCESS.getCode();
        this.msg = CommonConstants.SUCCESS.getMessage();
    }

    public ResponseBean(String code, String message) {
        super();
        this.code = code;
        this.msg = message;
    }

    public ResponseBean(T data) {
        super();
        this.data = data;
        this.code = CommonConstants.SUCCESS.getCode();
    }

    public ResponseBean(String code, String message, T data) {
        super();
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
