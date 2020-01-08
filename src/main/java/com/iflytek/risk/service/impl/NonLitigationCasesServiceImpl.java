package com.iflytek.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iflytek.risk.common.*;
import com.iflytek.risk.entity.*;
import com.iflytek.risk.entity.Dictionary;
import com.iflytek.risk.enums.BusinessEnum;
import com.iflytek.risk.enums.CommonConstants;
import com.iflytek.risk.enums.SystemMessageEnum;
import com.iflytek.risk.mapper.NonLitigationCasesMapper;
import com.iflytek.risk.mapper.SystemMapper;
import com.iflytek.risk.sec.SSOUser;
import com.iflytek.risk.service.IEnclosureFilesService;
import com.iflytek.risk.service.INonLitigationCasesService;
import com.iflytek.risk.service.IProcessInformationService;
import com.iflytek.risk.service.IRemindTodoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 非诉讼类案件表  服务实现类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
@Service
@Transactional(propagation = Propagation.NESTED, isolation = Isolation.DEFAULT, readOnly = false, rollbackFor = Exception.class)
public class NonLitigationCasesServiceImpl extends ServiceImpl<NonLitigationCasesMapper, NonLitigationCases> implements INonLitigationCasesService {

    @Resource
    IProcessInformationService processInformationService;
    @Resource
    IEnclosureFilesService enclosureFilesService;
    @Resource
    IRemindTodoService remindTodoService;
    @Resource
    CacheUtil cacheUtil;
    @Resource
    SystemMapper systemMapper;
    @Value("${file.direction}")
    private String templatesRoot;
    @Value("${file.template.nonLitigationCases}")
    private String exportTemplate;
    @Value("${flowNo}")
    private String flowNoHeard;
    //流程编码初始值
    private static int flowNoStartNum = 1;
    //流程编码流水号位数
    private static final int flowNoNumMaxLength = 5;

    /**
     * 公共基础方法
     *
     * @param requestBean
     * @return
     */
    @Override
    public ResponseBean base(RequestBean requestBean, HttpServletRequest request) {
        switch (requestBean.getHandle()) {
            case ADD:
                return add(requestBean);
            case ADD_BATCH:
                return addBatch(requestBean);
            case UPDATE_ALL:
                return updateAllField(requestBean, request);
            case UPDATE_ALL_BATCH:
                return updateAllFieldBatch(requestBean);
            case DELETE_LOGICAL:
                return deleteLogicalSingle(requestBean);
            case DELETE_LOGICAL_BATCH:
                return deleteLogicalBatch(requestBean);
            case GET_INFO_BY_ID:
                return getInfoById(requestBean);
            case GET_LIST_BY_CONDITION:
                return getListByCondition(requestBean);
            case GET_ALL:
                return getAll();
            case GET_PAGE:
                return getPage(requestBean, request);
            case UPDATE_SELF_INFO:
                return updateSelfInfo(requestBean);
            default:
                return new ResponseBean(
                        CommonConstants.FAIL.getCode(),
                        SystemMessageEnum.HANDLE_NOT_IN.getValue()
                );

        }
    }

    /**
     * 接口增加数据
     *
     * @param requestBean 请求参数
     * @return ResponseBean
     */
    @Override
    public ResponseBean interfaceHandle(RequestBean requestBean) {
        //非空校验
        NonLitigationCases nonLitigationCases = Hzq.convertValue(requestBean.getInfo(), NonLitigationCases.class);
        String errorCode = CommonConstants.FAIL.getCode();
        assert nonLitigationCases != null;
        if (StringUtils.isEmpty(nonLitigationCases.getFlowNo())) {
            return new ResponseBean(
                    errorCode,
                    "流程编号为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getAscriptionCompany())) {
            return new ResponseBean(
                    errorCode,
                    "纠纷发生法人主体为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getUnitName())) {
            return new ResponseBean(
                    errorCode,
                    "业务所在部门为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getApplicant())) {
            return new ResponseBean(
                    errorCode,
                    "申请人(如果有域账号请传姓名+域账号)为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getTargetName())) {
            return new ResponseBean(
                    errorCode,
                    "对方名称为空"
            );
        }

        if (nonLitigationCases.getApplyDate() == null) {
            return new ResponseBean(
                    errorCode,
                    "申请日期为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getAppeal())) {
            return new ResponseBean(
                    errorCode,
                    "诉求(传code，字典值如下：01-要求对方履行合同义务，02-要求对方承担缔约过失责任、赔偿损失，03-要求对方承担违约责任，04-要求解除合同，05-要求对方返还已支付款项，06-要求对方承担侵权责任，07-要求对方承担维权合理支出， 08-回函/应诉)为空"
            );
        }

//        if (StringUtils.isEmpty(nonLitigationCases.getOtherAppeal())) {
//            return new ResponseBean(
//                    errorCode,
//                    "其他诉求补充说明为空"
//            );
//        }

//        if (nonLitigationCases.getMoney() == null || BigDecimal.ZERO.equals(nonLitigationCases.getMoney())) {
//            return new ResponseBean(
//                    errorCode,
//                    "金额为空或金额为0"
//            );
//        }

        if (StringUtils.isEmpty(nonLitigationCases.getCaseBrief())) {
            return new ResponseBean(
                    errorCode,
                    "事件简介为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getDisputeType())) {
            return new ResponseBean(
                    errorCode,
                    "纠纷类型(传code，字典值如下：0-买卖合同纠纷，1-建设工程合同纠纷，2-技术合同纠份，3-保险合同纠份，4-服务合同纠纷，5-著作权纠纷，6-商标权纠纷，7-机动车交通事故责任纠纷，8-商业秘密纠纷，9-不正当竞争纠纷，10-肖像权纠纷，11-劳动合同纠纷，12-投融资类纠纷，13-其它类纠纷)为空"
            );
        }

        if (StringUtils.isEmpty(nonLitigationCases.getServiceType())) {
            return new ResponseBean(
                    errorCode,
                    "服务类型(传code，字典值如下：0-律师函，1-诉讼、仲裁、应诉，2-其他函件，3-催款函，4-证据公证)为空"
            );
        }

//        if (StringUtils.isEmpty(nonLitigationCases.getServicePersonal())) {
//            return new ResponseBean(
//                    errorCode,
//                    "服务人员(域账号)为空"
//            );
//        }
        // 根据流程编号 判断是已存在，暂时 提示错误
        QueryWrapper<NonLitigationCases> queryWrapper = new QueryWrapper<NonLitigationCases>();
        queryWrapper.eq("flow_no", nonLitigationCases.getFlowNo());
        List<NonLitigationCases> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return new ResponseBean(
                    errorCode,
                    "流程编号已存在"
            );
        }
        //入库
        nonLitigationCases.setCostFlag("no");
        nonLitigationCases.setOutsideLawyerFlag("no");
        this.save(nonLitigationCases);
        return new ResponseBean(CommonConstants.SUCCESS.getCode(), CommonConstants.SUCCESS.getMessage());
    }

    /**
     * 单个新增
     *
     * @param requestBean
     * @return
     */
    public ResponseBean add(RequestBean requestBean) {
        try {
            // 获取其他数据
            Map object = (LinkedHashMap) requestBean.getInfo();
            List cooperationPersonnelList = (ArrayList) object.get("cooperationPersonnel");
            List processList = (ArrayList) object.get("caseProcess");
            List<EnclosureFiles> files = object.get("files") != null ? Hzq.convertValue((ArrayList) object.get("files"), EnclosureFiles.class) : null;
            object.remove("cooperationPersonnel");
            object.remove("caseProcess");
            object.remove("files");
            NonLitigationCases nonLitigationCases = Hzq.convertValue(requestBean.getInfo(), NonLitigationCases.class);
            //生成流程编号
            String flowNo = generateFlowNo();
            nonLitigationCases.setFlowNo(flowNo);
            //处理协作人员
            if (!org.springframework.util.CollectionUtils.isEmpty(cooperationPersonnelList)) {
                nonLitigationCases.setCooperationPerson(RiskUtils.listToString(cooperationPersonnelList, ","));
            }
            // 保存
            boolean saveResult = this.save(nonLitigationCases);
            if (saveResult) {
                // 保存成功后，将关联数据保存
                // 1、保存协作人员
                // TODO 等接口调试

                // 2、保存案件进展
                if (processList != null && processList.size() > 0) {
                    List<ProcessInformation> pList = new ArrayList<>();
                    Long index = 1L;
                    for (Object obj : processList) {
                        Map map = (HashMap) obj;
                        String processContent = (String) map.get("content");
                        if (!StringUtils.isEmpty(processContent)) {
                            ProcessInformation processInformation = new ProcessInformation();
                            processInformation.setContent(processContent);
                            processInformation.setRelationId(nonLitigationCases.getId());
                            processInformation.setDataType(BusinessEnum.NONE_CASES.getValue());
                            processInformation.setSort(index);
                            pList.add(processInformation);
                            index++;
                        }
                    }
                    if (pList != null && pList.size() > 0) {
                        processInformationService.saveBatch(pList);
                    }
                }
                // 3、附件保存
                if (files != null && files.size() > 0) {
                    for (EnclosureFiles file : files) {
                        file.setId(null);
                        file.setBusinessId(nonLitigationCases.getId());
                        file.setBusinessType(BusinessEnum.NONE_CASES.getValue());
                    }
                    enclosureFilesService.saveBatch(files, files.size());
                }
            }
            return new ResponseBean(saveResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 生成流程编号
     *
     * @return
     */
    private String generateFlowNo() {
        QueryWrapper<NonLitigationCases> queryWrapper = new QueryWrapper<NonLitigationCases>();
        queryWrapper.orderByDesc("flow_no");
        queryWrapper.last("limit 0,1");
        List<NonLitigationCases> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list) || StringUtils.isEmpty(list.get(0).getFlowNo())) {
            return getFlowNum(null);
        }

        String flowNo = list.get(0).getFlowNo();
        //FLSW-YYYYMMDD流水号
        if (flowNo.length() <= 13) {
            return getFlowNum(null);
        }

        return getFlowNum(flowNo.substring(13));
    }

    private synchronized String getFlowNum(String oldFlowNoStr) {
        String timeStr = new SimpleDateFormat("yyyyMMdd").format(new Date()).toString();
        String flowNoFirst = flowNoHeard + timeStr;
        int newFlowNo = 0;
        if (StringUtils.isEmpty(oldFlowNoStr)
                || !isNumeric(oldFlowNoStr)
                || oldFlowNoStr.length() > flowNoNumMaxLength) {
            newFlowNo = flowNoStartNum++;
        } else {
            newFlowNo = Integer.parseInt(oldFlowNoStr.replaceAll("^(0+)", "")) + 1;
        }

        return flowNoFirst + String.format("%0" + flowNoNumMaxLength + "d", newFlowNo);
    }

    /**
     * 判断字符串是否是数字
     *
     * @param str
     * @return
     */
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 批量新增
     *
     * @param requestBean
     * @return
     */
    public ResponseBean addBatch(RequestBean requestBean) {
        return new ResponseBean(this.saveBatch(Hzq.convertValue(requestBean.getInfos(), NonLitigationCases.class)));
    }

    /**
     * 更新单条数据所有字段
     *
     * @param requestBean
     * @return
     */
    public ResponseBean updateAllField(RequestBean requestBean, HttpServletRequest request) {
        try {
            // 获取其他数据
            Map object = (LinkedHashMap) requestBean.getInfo();
            List cooperationPersonnelList = (ArrayList) object.get("cooperationPersonnel");
            List processList = (ArrayList) object.get("caseProcess");
            List<EnclosureFiles> files = object.get("files") != null ? Hzq.convertValue((ArrayList) object.get("files"), EnclosureFiles.class) : null;
            object.remove("cooperationPersonnel");
            object.remove("caseProcess");
            object.remove("files");
            NonLitigationCases nonLitigationCases = Hzq.convertValue(requestBean.getInfo(), NonLitigationCases.class);
            nonLitigationCases.setUpdateTime(systemMapper.getNow());
            //判断当前操作人是否有权限操作该数据
            if (!checkServicePersonal(request, nonLitigationCases.getId())) {
                return new ResponseBean(
                        CommonConstants.FAIL.getCode(),
                        "您没有相关权限，请联系管理员"
                );
            }
            //处理协作人员
            if (!CollectionUtils.isEmpty(cooperationPersonnelList)) {
                nonLitigationCases.setCooperationPerson(RiskUtils.listToString(cooperationPersonnelList, ","));
            }
            // 保存
            boolean updateResult = this.updateById(nonLitigationCases);
            if (updateResult) {
                // 保存成功后，将关联数据保存
                // 1、保存案件进展，删除原有进展数据数据，重新添加
                QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
                queryWrapper
                        .eq("relation_id", nonLitigationCases.getId())
                        .eq("data_type", BusinessEnum.NONE_CASES.getValue());
                processInformationService.remove(queryWrapper);
                if (processList != null && processList.size() > 0) {
                    List<ProcessInformation> pList = new ArrayList<>();
                    Long index = 1L;
                    for (Object obj : processList) {
                        Map map = (HashMap) obj;
                        String processContent = (String) map.get("content");
                        if (!StringUtils.isEmpty(processContent)) {
                            ProcessInformation processInformation = new ProcessInformation();
                            processInformation.setContent(processContent);
                            processInformation.setRelationId(nonLitigationCases.getId());
                            processInformation.setDataType(BusinessEnum.NONE_CASES.getValue());
                            processInformation.setSort(index);
                            pList.add(processInformation);
                            index++;
                        }
                    }
                    if (pList != null && pList.size() > 0) {
                        boolean processSaveFlag = processInformationService.saveBatch(pList);
                        if (!processSaveFlag) {
                            throw new Exception("process information save fail");
                        }
                    }
                }
                // 3、附件保存
                QueryWrapper<EnclosureFiles> queryFilesWrapper = new QueryWrapper<EnclosureFiles>();
                queryFilesWrapper.eq("business_type", BusinessEnum.NONE_CASES.getValue()).eq("business_id", nonLitigationCases.getId());
                enclosureFilesService.remove(queryFilesWrapper);
                if (files != null && files.size() > 0) {
                    for (EnclosureFiles file : files) {
                        file.setId(null);
                        file.setBusinessId(nonLitigationCases.getId());
                        file.setBusinessType(BusinessEnum.NONE_CASES.getValue());
                    }
                    enclosureFilesService.saveBatch(files, files.size());
                }
            } else {
                throw new Exception("main information update fail");
            }
            return new ResponseBean(updateResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 校验服务人员权限
     */
    private boolean checkServicePersonal(HttpServletRequest request, String id) throws Exception {
        //判断当前操作人是否有权限操作该数据
        SSOUser ssoUser = (SSOUser) request.getSession().getAttribute("sso_user_session");
        if (ssoUser == null) {
            return false;
        }
        //是否是管理员
        boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        //非管理员用户只能查看服务人员是自己的数据
        if (!isAdmin) {
            NonLitigationCases nonLitigationCases = this.getById(id);
            String servicePersonal= nonLitigationCases.getServicePersonal();
            String currentSP = ssoUser.getName() + '(' + ssoUser.getAccountName() + ')';
            if (StringUtils.isEmpty(servicePersonal) || !servicePersonal.contains(currentSP)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 更新批量数据所有字段
     *
     * @param requestBean
     * @return
     */
    public ResponseBean updateAllFieldBatch(RequestBean requestBean) {
        return new ResponseBean(this.updateBatchById(Hzq.convertValue(requestBean.getInfos(), NonLitigationCases.class)));
    }

    /**
     * 单条逻辑删除
     *
     * @param requestBean
     * @return
     */
    public ResponseBean deleteLogicalSingle(RequestBean requestBean) {
        //1、删除当前记录
        String id = (String) requestBean.getInfo();
        boolean flag = this.removeById(id);
        //2、删除提醒人员数据
        if(flag){
            QueryWrapper<RemindTodo> queryWrapper = new QueryWrapper<RemindTodo>();
            queryWrapper.eq("relation_id", id);
            remindTodoService.remove(queryWrapper);
        }

        return new ResponseBean(flag);
    }

    /**
     * 批量逻辑删除
     *
     * @param requestBean
     * @return
     */
    public ResponseBean deleteLogicalBatch(RequestBean requestBean) {
        //1、删除当前记录
        List<String> ids = requestBean.getInfos();
        boolean flag = this.removeByIds(ids);
        //2、删除提醒人员数据
        if(flag){
            QueryWrapper<RemindTodo> queryWrapper = new QueryWrapper<RemindTodo>();
            queryWrapper.in("relation_id", ids);
            remindTodoService.remove(queryWrapper);
        }

        return new ResponseBean(flag);
    }

    /**
     * 根据主键获取单条数据
     *
     * @param requestBean
     * @return
     */
    public ResponseBean getInfoById(RequestBean requestBean) {
        // 获取主表数据
        NonLitigationCases nonLitigationCases = this.getById((String) requestBean.getInfo());
        Map map = Hzq.beanToMap(nonLitigationCases);
        //协作人员
        String cp = nonLitigationCases.getCooperationPerson();
        if (!StringUtils.isEmpty(cp)) {
            List cooperationPersonnel = RiskUtils.stringToList(cp, ",");
            map.put("cooperationPersonnel", cooperationPersonnel);
        }
        // 诉求数据处理
        String appeal = (String) map.get("appeal");
        map.remove("appeal");
        if (!StringUtils.isEmpty(appeal)) {
            map.put("appeal", appeal.split(","));
        } else {
            map.put("appeal", new String[0]);
        }
        // 获取案件进展数据
        QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
        queryWrapper
                .orderByAsc("sort")
                .eq("relation_id", requestBean.getInfo())
                .eq("data_type", BusinessEnum.NONE_CASES.getValue());
        List<ProcessInformation> processList = processInformationService.list(queryWrapper);
        map.put("caseProcess", processList);
        // 获取附件数据
        QueryWrapper<EnclosureFiles> queryFilesWrapper = new QueryWrapper<EnclosureFiles>();
        queryFilesWrapper.eq("business_type", BusinessEnum.NONE_CASES.getValue()).eq("business_id", nonLitigationCases.getId());
        List<EnclosureFiles> files = enclosureFilesService.list(queryFilesWrapper);
        files = files != null ? files : new ArrayList<EnclosureFiles>();
        map.put("files", files);
        return new ResponseBean(map);
    }

    /**
     * 根据条件查询数据
     *
     * @param requestBean
     * @return
     */
    public ResponseBean getListByCondition(RequestBean requestBean) {
        Wrapper queryWrapper = new QueryWrapper();
        // TODO 添加查询条件

        return new ResponseBean(this.list(queryWrapper));
    }

    /**
     * 获取全部数据
     *
     * @return
     */
    public ResponseBean getAll() {
        return new ResponseBean(this.list());
    }


    /**
     * 获取分页数据
     *
     * @param requestBean
     * @return
     */
    public ResponseBean getPage(RequestBean requestBean, HttpServletRequest request) {
        Page page = Hzq.convertValue(requestBean.getInfo(), Page.class);
        if (StringUtils.isEmpty(page)) {
            page = new Page();
        }
        QueryWrapper<NonLitigationCases> queryWrapper = new QueryWrapper<NonLitigationCases>();
        Map queryMap = page.getRecords().size() > 0 ? (HashMap) page.getRecords().get(0) : null;
        // 对于实体类不存在字段，需提前拿出来并删除原有，否则，反序列化会失败
        Double moneyStart = (queryMap.get("moneyStart") != null) ? Double.parseDouble(queryMap.get("moneyStart") + "") : null;
        Double moneyEnd = (queryMap.get("moneyEnd") != null) ? Double.parseDouble(queryMap.get("moneyEnd") + "") : null;
        List<Date> applyDateList = (List<Date>) queryMap.get("applyDate");
        if (moneyStart != null) {
            queryMap.remove("moneyStart");
            queryWrapper.ge("money", moneyStart);
        }
        if (moneyEnd != null) {
            queryMap.remove("moneyEnd");
            queryWrapper.le("money", moneyEnd);
        }
        if (applyDateList != null && applyDateList.size() == 2) {
            queryMap.remove("applyDate");
            queryWrapper.between("apply_date", applyDateList.get(0), applyDateList.get(1));
        }
        // 序列化
        NonLitigationCases nonLitigationCases = Hzq.convertValue(queryMap, NonLitigationCases.class);
        // 如果没有排序字段，默认按照更新事件倒序排列
        queryWrapper.orderByDesc("update_time");
        queryParamsSet(queryWrapper, nonLitigationCases);
        //管理员查询权限控制
        queryParamsSetByAdmin(request, queryWrapper);
        return new ResponseBean(this.page(page, queryWrapper));
    }

    /**
     * 设置权限
     */
    private void queryParamsSetByAdmin(HttpServletRequest request, QueryWrapper<NonLitigationCases> queryWrapper) {
        //是否是管理员
        boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        //非管理员用户只能查看服务人员是自己的数据
        if (!isAdmin) {
            SSOUser ssoUser = (SSOUser) request.getSession().getAttribute("sso_user_session");
            if (ssoUser != null) {
                queryWrapper.like("service_personal", ssoUser.getName() + '(' + ssoUser.getAccountName() + ')');
            }
        }
    }

    /**
     * 查询条件参数设置方法
     *
     * @param queryWrapper
     * @param nonLitigationCases
     */
    private void queryParamsSet(QueryWrapper<NonLitigationCases> queryWrapper, NonLitigationCases nonLitigationCases) {
        // 在办、结案区分
        if (!StringUtils.isEmpty(nonLitigationCases.getCaseStatus())) {
            queryWrapper.eq("case_status", nonLitigationCases.getCaseStatus());
        }
        if (!StringUtils.isEmpty(nonLitigationCases.getAscriptionCompany())) {
            queryWrapper.like("ascription_company", nonLitigationCases.getAscriptionCompany());
        }
        if (!StringUtils.isEmpty(nonLitigationCases.getTargetName())) {
            queryWrapper.like("target_name", nonLitigationCases.getTargetName());
        }
        if (!StringUtils.isEmpty(nonLitigationCases.getServicePersonal())) {
            queryWrapper.like("service_personal", nonLitigationCases.getServicePersonal());
        }
        if (!StringUtils.isEmpty(nonLitigationCases.getDisputeType())) {
            queryWrapper.eq("dispute_type", nonLitigationCases.getDisputeType());
        }
        if (!StringUtils.isEmpty(nonLitigationCases.getServiceType())) {
            queryWrapper.eq("service_type", nonLitigationCases.getServiceType());
        }
        if (!StringUtils.isEmpty(nonLitigationCases.getRiskGrade())) {
            queryWrapper.eq("risk_grade", nonLitigationCases.getRiskGrade());
        }
        // TODO 添加其他查询条件
    }

    /**
     * 更新主表
     *
     * @param requestBean
     * @return
     */
    public ResponseBean updateSelfInfo(RequestBean requestBean) {
        NonLitigationCases nonLitigationCases = Hzq.convertValue(requestBean.getInfo(), NonLitigationCases.class);
        nonLitigationCases.setUpdateTime(systemMapper.getNow());
        return new ResponseBean(this.updateById(nonLitigationCases));
    }

    /**
     * 公共下载方法
     *
     * @param queryMap 下载参数
     * @param request  请求
     * @param response 返回
     */
    public void downFile(Map queryMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
        QueryWrapper<NonLitigationCases> queryWrapper = new QueryWrapper<NonLitigationCases>();
        Double moneyStart = (queryMap.get("moneyStart") != null) ? Double.parseDouble(queryMap.get("moneyStart") + "") : null;
        Double moneyEnd = (queryMap.get("moneyEnd") != null) ? Double.parseDouble(queryMap.get("moneyEnd") + "") : null;
        List<Date> applyDateList = (List<Date>) queryMap.get("applyDate");
        if (moneyStart != null) {
            queryMap.remove("moneyStart");
            queryWrapper.ge("money", moneyStart);
        }
        if (moneyEnd != null) {
            queryMap.remove("moneyEnd");
            queryWrapper.le("money", moneyEnd);
        }
        if (applyDateList != null && applyDateList.size() == 2) {
            queryMap.remove("applyDate");
            queryWrapper.between("apply_date", applyDateList.get(0), applyDateList.get(1));
        }
        NonLitigationCases nonLitigationCases = Hzq.convertValue(queryMap, NonLitigationCases.class);
        queryWrapper.orderByDesc("update_time");
        queryParamsSet(queryWrapper, nonLitigationCases);
        //管理员查询权限控制
        queryParamsSetByAdmin(request, queryWrapper);
        List<NonLitigationCases> list = this.list(queryWrapper);
        writeExcel(list, "法务风险管理系统非诉案件", response);
    }

    /**
     * 数据写入
     *
     * @param list
     * @param fileName
     * @param response
     */
    public void writeExcel(List<NonLitigationCases> list, String fileName, HttpServletResponse response) {
        String[] title = exportTemplate.split(",");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map> mapList = this.listToMap(list);
        String[][] content = new String[mapList.size()][];
        for (int i = 0; i < mapList.size(); i++) {
            content[i] = new String[title.length];
            //将对象内容转换成string
            Map tr = mapList.get(i);
            content[i][0] = RiskUtils.getMapValueToString(tr.get("flowNo"));
            content[i][1] = RiskUtils.getMapValueToString(tr.get("applyDate"));
            content[i][2] = RiskUtils.getMapValueToString(tr.get("applicant"));
            content[i][3] = RiskUtils.getMapValueToString(tr.get("ascriptionCompany"));
            content[i][4] = RiskUtils.getMapValueToString(tr.get("unitName"));
            content[i][5] = RiskUtils.getMapValueToString(tr.get("targetName"));
            content[i][6] = !StringUtils.isEmpty(tr.get("money")) ? ((BigDecimal) tr.get("money")).setScale(2, BigDecimal.ROUND_UP).toString() : "";
            content[i][7] = RiskUtils.getMapValueToString(tr.get("disputeType"));
            content[i][8] = RiskUtils.getMapValueToString(tr.get("serviceType"));
            content[i][9] = RiskUtils.getMapValueToString(tr.get("servicePersonal"));
            content[i][10] = RiskUtils.getMapValueToString(tr.get("cooperationPerson"));
            content[i][11] = RiskUtils.getMapValueToString(tr.get("outsideLawyerFlag"));
            content[i][12] = RiskUtils.getMapValueToString(tr.get("lawFirmName"));
            content[i][13] = RiskUtils.getMapValueToString(tr.get("lawerName"));
            content[i][14] = RiskUtils.getMapValueToString(tr.get("costFlag"));
            content[i][15] = !StringUtils.isEmpty(tr.get("costAmount")) ? ((BigDecimal) tr.get("costAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][16] = !StringUtils.isEmpty(tr.get("preservationAmount")) ? ((BigDecimal) tr.get("preservationAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][17] = !StringUtils.isEmpty(tr.get("agentAmount")) ? ((BigDecimal) tr.get("agentAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][18] = !StringUtils.isEmpty(tr.get("otherAmount")) ? ((BigDecimal) tr.get("otherAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][19] = RiskUtils.getMapValueToString(tr.get("caseBrief"));
            content[i][20] = RiskUtils.getMapValueToString(tr.get("appeal"));
            content[i][21] = RiskUtils.getMapValueToString(tr.get("riskGrade"));
            content[i][22] = RiskUtils.getMapValueToString(tr.get("caseAnalysis"));
            content[i][23] = RiskUtils.getMapValueToString(tr.get("preliminaryOpinion"));
            content[i][24] = RiskUtils.getMapValueToString(tr.get("caseProcess"));
            content[i][25] = !StringUtils.isEmpty(tr.get("derogationAmount")) ? ((BigDecimal) tr.get("derogationAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][26] = RiskUtils.getMapValueToString(tr.get("summary"));
            content[i][27] = RiskUtils.getMapValueToString(tr.get("replay"));
            content[i][28] = RiskUtils.getMapValueToString(tr.get("caseStatus"));
        }
        //创建XSSFWorkbook
        XSSFWorkbook wb = ExcelUtil.getXSSFWorkbook("Sheet1", title, content, null);
        try {
            ExcelUtil.setResponseHeader(response, fileName + "-" + System.currentTimeMillis() + ".xlsx");
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出数据转换
     *
     * @param list
     * @return
     */
    public List<Map> listToMap(List<NonLitigationCases> list) {
        List<Map> maps = new ArrayList<Map>();
        Map<String, List<Dictionary>> cacheMap = new HashMap<String, List<Dictionary>>();
        if (list != null && list.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (NonLitigationCases nonLitigationCases : list) {
                Map map = Hzq.beanToMap(nonLitigationCases);
                // 申请日期
                if (!StringUtils.isEmpty(nonLitigationCases.getApplyDate())) {
                    map.put("applyDate", sdf.format(nonLitigationCases.getApplyDate()));
                }
                // 纠纷类型
                if (!StringUtils.isEmpty(nonLitigationCases.getDisputeType())) {
                    map.put("disputeType", cacheUtil.getValueByCode("disputeType", nonLitigationCases.getDisputeType(), cacheMap));
                }
                // 服务类型
                if (!StringUtils.isEmpty(nonLitigationCases.getServiceType())) {
                    map.put("serviceType", cacheUtil.getValueByCode("serviceType", nonLitigationCases.getServiceType(), cacheMap));
                }
                // 诉求
                if (!StringUtils.isEmpty(nonLitigationCases.getAppeal())) {
                    String[] appeals = nonLitigationCases.getAppeal().split(",");
                    StringBuffer sbf = new StringBuffer();
                    for (String appealType : appeals) {
                        sbf.append(cacheUtil.getValueByCode("appealType", nonLitigationCases.getServiceType(), cacheMap));
                    }
                    map.put("appeal", sbf.toString());
                }
                // 是否外聘律师
                if (!StringUtils.isEmpty(nonLitigationCases.getOutsideLawyerFlag())) {
                    map.put("outsideLawyerFlag", cacheUtil.getValueByCode("outsideLawerFlag", nonLitigationCases.getOutsideLawyerFlag(), cacheMap));
                }
                // 是否产生费用
                if (!StringUtils.isEmpty(nonLitigationCases.getCostFlag())) {
                    map.put("costFlag", cacheUtil.getValueByCode("costFlag", nonLitigationCases.getCostFlag(), cacheMap));
                }
                // 风险等级
                if (!StringUtils.isEmpty(nonLitigationCases.getRiskGrade())) {
                    map.put("riskGrade", cacheUtil.getValueByCode("riskGrade", nonLitigationCases.getRiskGrade(), cacheMap));
                }
                // 案件状态
                if (!StringUtils.isEmpty(nonLitigationCases.getCaseStatus())) {
                    map.put("caseStatus", cacheUtil.getValueByCode("caseState", nonLitigationCases.getCaseStatus(), cacheMap));
                }

                // 案件进展
                QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
                queryWrapper
                        .eq("relation_id", nonLitigationCases.getId())
                        .eq("data_type", BusinessEnum.NONE_CASES.getValue());
                List<ProcessInformation> processInformations = processInformationService.list(queryWrapper);
                if (processInformations != null && processInformations.size() > 0) {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (ProcessInformation processInformation : processInformations) {
                        if (!StringUtils.isEmpty(processInformation.getContent())) {
                            stringBuffer.append(processInformation.getContent());
                        }
                    }
                    map.put("caseProcess", stringBuffer.toString());
                }
                maps.add(map);
            }
        }
        return maps;
    }

}
