package com.iflytek.risk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iflytek.risk.common.BaseEntityBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 附件表 
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_fwfx_enclosure_files")
@ApiModel(value="EnclosureFiles对象", description="附件表 ")
public class EnclosureFiles extends BaseEntityBean {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联主键")
    private String businessId;

    @ApiModelProperty(value = "所属业务类型")
    private String businessType;

    @ApiModelProperty(value = "附件名称")
    private String fileName;

    @ApiModelProperty(value = "文件夹路径")
    private String folderCode;

    @ApiModelProperty(value = "附件大小 单位KB")
    private Long fileSize;

    @ApiModelProperty(value = "附件类型")
    private String fileType;

    @ApiModelProperty(value = "组名")
    private String groupName;

    @ApiModelProperty(value = "附件path")
    private String filePath;

    @ApiModelProperty(value = "下载地址")
    private String fileUrl;


}
