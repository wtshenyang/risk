package com.iflytek.risk.interfaceController;

import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.service.ILitigationCasesService;
import com.iflytek.risk.service.INonLitigationCasesService;
import com.iflytek.risk.service.IRiskService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangzhiqiang
 * 接口
 */
@RestController
@RequestMapping("/interface")
public class InterfaceController {
    @Resource
    INonLitigationCasesService nonLitigationCasesService;
    @Resource
    ILitigationCasesService litigationCasesService;

    @ApiOperation(value = "诉讼类案件新增接口", notes = "诉讼类案件新增接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bh", value = "流程编号", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "gsgs", value = "纠纷发生法人主体", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "bmmc", value = "业务所在部门", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sqr", value = "申请人(如果有域账号请传姓名+域账号)", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "dfmc", value = "对方名称", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sqrq", value = "申请日期(yyyy-MM-dd)", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sqlx",
                    value = "诉求(传code，字典值如下：01-要求对方履行合同义务，02-要求对方承担缔约过失责任、赔偿损失，03-要求对方承担违约责任，" +
                            "04-要求解除合同，05-要求对方返还已支付款项，06-要求对方承担侵权责任，07-要求对方承担维权合理支出， 08-回函/应诉)",
                    dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sq", value = "其他诉求补充说明", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "je", value = "金额", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sy", value = "事件简介", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "jflx",
                    value = "纠纷类型(传code，字典值如下：0-买卖合同纠纷，1-建设工程合同纠纷，2-技术合同纠份，3-保险合同纠份，4-服务合同纠纷，5-著作权纠纷，" +
                            "6-商标权纠纷，7-机动车交通事故责任纠纷，8-商业秘密纠纷，9-不正当竞争纠纷，10-肖像权纠纷，11-劳动合同纠纷，12-投融资类纠纷，13-其它类纠纷)",
                    dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "fwlx", value = "服务类型(传code，字典值如下：0-律师函，1-诉讼、仲裁、应诉，2-其他函件，3-催款函，4-证据公证)", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "fwry", value = "服务人员(域账号)", dataType = "String", required = true, paramType = "query")
    })
    @PostMapping("/casesAdd")
    @ResponseBody
    public ResponseBean casesAdd(
            String bh,
            String gsgs,
            String bmmc,
            String sqr,
            String dfmc,
            String sqrq,
            String sqlx,
            String sq,
            String je,
            String sy,
            String jflx,
            String fwlx,
            String fwry
    ) {
        Map infoMap = new HashMap();
        infoMap.put("flowNo", bh);
        infoMap.put("ascriptionCompany", gsgs);
        infoMap.put("unitName", bmmc);
        infoMap.put("applicant", sqr);
        infoMap.put("targetName", dfmc);
        infoMap.put("applyDate", sqrq);
        infoMap.put("appeal", sqlx);
        infoMap.put("otherAppeal", sq);
        infoMap.put("money", je);
        infoMap.put("caseBrief", sy);
        infoMap.put("disputeType", jflx);
        infoMap.put("serviceType", fwlx);
        infoMap.put("servicePersonal", fwry);
        RequestBean requestBean = new RequestBean("interfaceAdd", null, null, infoMap, null);
        return litigationCasesService.interfaceHandle(requestBean);
    }


    @ApiOperation(value = "非诉讼类案件新增接口", notes = "非诉讼类案件新增接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bh", value = "流程编号", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "gsgs", value = "纠纷发生法人主体", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "bmmc", value = "业务所在部门", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sqr", value = "申请人(如果有域账号请传姓名+域账号)", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "dfmc", value = "对方名称", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sqrq", value = "申请日期(yyyy-MM-dd)", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sqlx",
                    value = "诉求(传code，字典值如下：01-要求对方履行合同义务，02-要求对方承担缔约过失责任、赔偿损失，03-要求对方承担违约责任，" +
                            "04-要求解除合同，05-要求对方返还已支付款项，06-要求对方承担侵权责任，07-要求对方承担维权合理支出， 08-回函/应诉)",
                    dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sq", value = "其他诉求补充说明", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "je", value = "金额", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "sy", value = "事件简介", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "jflx",
                    value = "纠纷类型(传code，字典值如下：0-买卖合同纠纷，1-建设工程合同纠纷，2-技术合同纠份，3-保险合同纠份，4-服务合同纠纷，5-著作权纠纷，" +
                            "6-商标权纠纷，7-机动车交通事故责任纠纷，8-商业秘密纠纷，9-不正当竞争纠纷，10-肖像权纠纷，11-劳动合同纠纷，12-投融资类纠纷，13-其它类纠纷)",
                    dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "fwlx", value = "服务类型(传code，字典值如下：0-律师函，1-诉讼、仲裁、应诉，2-其他函件，3-催款函，4-证据公证)", dataType = "String", required = true, paramType = "query"),
            @ApiImplicitParam(name = "fwry", value = "服务人员(域账号)", dataType = "String", required = true, paramType = "query")
    })
    @PostMapping("/casesNoneAdd")
    @ResponseBody
    public ResponseBean casesNoneAdd(
            String bh,
            String gsgs,
            String bmmc,
            String sqr,
            String dfmc,
            String sqrq,
            String sqlx,
            String sq,
            String je,
            String sy,
            String jflx,
            String fwlx,
            String fwry
    ) {
        Map infoMap = new HashMap();
        infoMap.put("flowNo", bh);
        infoMap.put("ascriptionCompany", gsgs);
        infoMap.put("unitName", bmmc);
        infoMap.put("applicant", sqr);
        infoMap.put("targetName", dfmc);
        infoMap.put("applyDate", sqrq);
        infoMap.put("appeal", sqlx);
        infoMap.put("otherAppeal", sq);
        infoMap.put("money", je);
        infoMap.put("caseBrief", sy);
        infoMap.put("disputeType", jflx);
        infoMap.put("serviceType", fwlx);
        infoMap.put("servicePersonal", fwry);
        RequestBean requestBean = new RequestBean("interfaceAdd", null, null, infoMap, null);
        return nonLitigationCasesService.interfaceHandle(requestBean);
    }
}
