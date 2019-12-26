package com.iflytek.risk.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.iflytek.risk.enums.HandleEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: law-risk->RequestBean
 * @description: 接收参数集合
 * @author: 黄智强
 * @create: 2019-11-08 09:10
 **/
@ApiModel(value = "RequestBean", description = "接收参数集合")
public class RequestBean<T> implements Serializable {
    private static final long serialVersionUID = -6351210629803310653L;


    @ApiModelProperty(example = "add", value = "操作类型")
    @Getter
    private String type;

    public void setType(String type) {
        this.type = type;
        this.handle = HandleEnum.getTypeByValue(type);
    }

    @Setter
    private HandleEnum handle;

    public HandleEnum getHandle() {
        return HandleEnum.getTypeByValue(this.type);
    }

    @ApiModelProperty(example = "新增", value = "操作信息")
    @Getter
    @Setter
    private String message;
    @ApiModelProperty(example = "data", value = "参数")
    @Setter
    private T info;

    public T getInfo() {
        if (info == null && infos != null && infos.size() > 0) {
            ObjectMapper mapper=new ObjectMapper();
            return infos.get(0);
        } else {
            return info;
        }
    }

    @ApiModelProperty(example = "datas", value = "参数集合")
    private List<T> infos;

    public List<T> getInfos() {
        if (this.infos != null && this.infos.size() > 0) {
            return this.infos;
        } else {
            List<T> infoList = new ArrayList<T>();
            if (this.info != null) {
                infoList.add(this.info);
            }
            return infoList;
        }
    }

    public void setInfos(List<T> infos) {
        this.infos = infos;
        if (infos != null && infos.size() > 0) {
            this.info = infos.get(0);
        }
    }

    public RequestBean(String type, HandleEnum handle, String message, T info, List<T> infos) {
        this.type = type;
        this.handle = handle;
        this.message = message;
        this.info = info;
        this.infos = infos;
    }
}
