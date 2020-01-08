package com.iflytek.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iflytek.risk.common.*;
import com.iflytek.risk.entity.Dictionary;
import com.iflytek.risk.entity.*;
import com.iflytek.risk.enums.BusinessEnum;
import com.iflytek.risk.enums.CommonConstants;
import com.iflytek.risk.enums.SystemMessageEnum;
import com.iflytek.risk.mapper.LitigationCasesMapper;
import com.iflytek.risk.mapper.SystemMapper;
import com.iflytek.risk.sec.SSOUser;
import com.iflytek.risk.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
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
 * 诉讼类案件表  服务实现类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
@Service
@Transactional(propagation = Propagation.NESTED, isolation = Isolation.DEFAULT, readOnly = false, rollbackFor = Exception.class)
public class LitigationCasesServiceImpl extends ServiceImpl<LitigationCasesMapper, LitigationCases> implements ILitigationCasesService {

    @Resource
    IProcessInformationService processInformationService;
    @Resource
    IHearInformationService hearInformationService;
    @Resource
    IEnclosureFilesService enclosureFilesService;
    @Resource
    IRemindTodoService remindTodoService;
    @Resource
    SystemMapper systemMapper;
    @Resource
    CacheUtil cacheUtil;
    @Value("${file.direction}")
    private String templatesRoot;
    @Value("${file.template.litigationCases}")
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
        LitigationCases litigationCases = Hzq.convertValue(requestBean.getInfo(), LitigationCases.class);
        String errorCode = CommonConstants.FAIL.getCode();
        assert litigationCases != null;
        if (StringUtils.isEmpty(litigationCases.getFlowNo())) {
            return new ResponseBean(
                    errorCode,
                    "流程编号为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getAscriptionCompany())) {
            return new ResponseBean(
                    errorCode,
                    "纠纷发生法人主体为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getUnitName())) {
            return new ResponseBean(
                    errorCode,
                    "业务所在部门为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getApplicant())) {
            return new ResponseBean(
                    errorCode,
                    "申请人(如果有域账号请传姓名+域账号)为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getTargetName())) {
            return new ResponseBean(
                    errorCode,
                    "对方名称为空"
            );
        }

        if (litigationCases.getApplyDate() == null) {
            return new ResponseBean(
                    errorCode,
                    "申请日期为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getAppeal())) {
            return new ResponseBean(
                    errorCode,
                    "诉求(传code，字典值如下：01-要求对方履行合同义务，02-要求对方承担缔约过失责任、赔偿损失，03-要求对方承担违约责任，04-要求解除合同，05-要求对方返还已支付款项，06-要求对方承担侵权责任，07-要求对方承担维权合理支出， 08-回函/应诉)为空"
            );
        }

//        if (StringUtils.isEmpty(litigationCases.getOtherAppeal())) {
//            return new ResponseBean(
//                    errorCode,
//                    "其他诉求补充说明为空"
//            );
//        }

//        if (litigationCases.getMoney() == null || BigDecimal.ZERO.equals(litigationCases.getMoney())) {
//            return new ResponseBean(
//                    errorCode,
//                    "金额为空或金额为0"
//            );
//        }

        if (StringUtils.isEmpty(litigationCases.getCaseBrief())) {
            return new ResponseBean(
                    errorCode,
                    "事件简介为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getDisputeType())) {
            return new ResponseBean(
                    errorCode,
                    "纠纷类型(传code，字典值如下：0-买卖合同纠纷，1-建设工程合同纠纷，2-技术合同纠份，3-保险合同纠份，4-服务合同纠纷，5-著作权纠纷，6-商标权纠纷，7-机动车交通事故责任纠纷，8-商业秘密纠纷，9-不正当竞争纠纷，10-肖像权纠纷，11-劳动合同纠纷，12-投融资类纠纷，13-其它类纠纷)为空"
            );
        }

        if (StringUtils.isEmpty(litigationCases.getServiceType())) {
            return new ResponseBean(
                    errorCode,
                    "服务类型(传code，字典值如下：0-律师函，1-诉讼、仲裁、应诉，2-其他函件，3-催款函，4-证据公证)为空"
            );
        }

//        if (StringUtils.isEmpty(litigationCases.getServicePersonal())) {
//            return new ResponseBean(
//                    errorCode,
//                    "服务人员(域账号)为空"
//            );
//        }
        // 根据流程编号 判断是已存在，暂时 提示错误
        QueryWrapper<LitigationCases> queryWrapper = new QueryWrapper<LitigationCases>();
        queryWrapper.eq("flow_no", litigationCases.getFlowNo());
        List<LitigationCases> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return new ResponseBean(
                    errorCode,
                    "流程编号已存在"
            );
        }
        //入库
        litigationCases.setCostFlag("no");
        litigationCases.setOutsideLawyerFlag("no");
        this.save(litigationCases);
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
            List<Map> hearInformationList = (ArrayList<Map>) object.get("hearInformationList");
            List<EnclosureFiles> files = object.get("files") != null ? Hzq.convertValue((ArrayList) object.get("files"), EnclosureFiles.class) : null;
            object.remove("cooperationPersonnel");
            object.remove("hearInformationList");
            object.remove("files");
            LitigationCases litigationCases = Hzq.convertValue(requestBean.getInfo(), LitigationCases.class);
            //生成流程编号
            String flowNo = generateFlowNo();
            litigationCases.setFlowNo(flowNo);
            //处理协作人员
            if (!org.springframework.util.CollectionUtils.isEmpty(cooperationPersonnelList)) {
                litigationCases.setCooperationPerson(RiskUtils.listToString(cooperationPersonnelList, ","));
            }
            // 保存
            boolean saveResult = this.save(litigationCases);
            if (saveResult) {
                // 保存成功后，将关联数据保存
                // 1、保存协作人员
                // TODO 等接口调试

                // 2、保存审理信息
                if (hearInformationList != null && hearInformationList.size() > 0) {
                    for (Map hearInfo : hearInformationList) {
                        List processList = (ArrayList) hearInfo.get("caseProcess");
                        hearInfo.remove("caseProcess");
                        HearInformation hearInformation = Hzq.convertValue(hearInfo, HearInformation.class);
                        hearInformation.setRelationId(litigationCases.getId());
                        boolean hearSaveFlag = hearInformationService.save(hearInformation);
                        if (hearSaveFlag) {
                            // 3、 保存进度信息
                            if (processList != null && processList.size() > 0) {
                                List<ProcessInformation> pList = new ArrayList<>();
                                for (Object obj : processList) {
                                    Map map = (HashMap) obj;
                                    String processContent = (String) map.get("content");
                                    if (!StringUtils.isEmpty(processContent)) {
                                        Integer sort = (Integer) map.get("sort");
                                        ProcessInformation processInformation = new ProcessInformation();
                                        processInformation.setContent(processContent);
                                        processInformation.setRelationId(hearInformation.getId());
                                        processInformation.setDataType(BusinessEnum.CASES.getValue());
                                        processInformation.setSort(sort.longValue());
                                        pList.add(processInformation);
                                    }
                                }
                                if (pList != null && pList.size() > 0) {
                                    boolean processSaveFlag = processInformationService.saveBatch(pList);
                                    if (!processSaveFlag) {
                                        throw new Exception("process Information save fail");
                                    }
                                }
                            }
                        } else {
                            throw new Exception("hear Information save fail");
                        }
                    }
                }

                // 3、附件保存
                if (files != null && files.size() > 0) {
                    for (EnclosureFiles file : files) {
                        file.setBusinessId(litigationCases.getId());
                        file.setBusinessType(BusinessEnum.CASES.getValue());
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
        QueryWrapper<LitigationCases> queryWrapper = new QueryWrapper<LitigationCases>();
        queryWrapper.orderByDesc("flow_no");
        queryWrapper.last("limit 0,1");
        List<LitigationCases> list = this.list(queryWrapper);
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
        return new ResponseBean(this.saveBatch(Hzq.convertValue(requestBean.getInfos(), LitigationCases.class)));
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
            List<Map> hearInformationList = (ArrayList<Map>) object.get("hearInformationList");
            //List caseAnalysisList = (ArrayList) object.get("caseAnalysis");
            List<EnclosureFiles> files = object.get("files") != null ? Hzq.convertValue((ArrayList) object.get("files"), EnclosureFiles.class) : null;
            object.remove("cooperationPersonnel");
            object.remove("hearInformationList");
            object.remove("files");
            object.remove("caseAnalysis");
            LitigationCases litigationCases = Hzq.convertValue(requestBean.getInfo(), LitigationCases.class);
            litigationCases.setUpdateTime(systemMapper.getNow());
            //判断当前操作人是否有权限操作该数据
            if (!checkServicePersonal(request, litigationCases.getId())) {
                return new ResponseBean(
                        CommonConstants.FAIL.getCode(),
                        "您没有相关权限，请联系管理员"
                );
            }
            //处理协作人员
            if (!CollectionUtils.isEmpty(cooperationPersonnelList)) {
                litigationCases.setCooperationPerson(RiskUtils.listToString(cooperationPersonnelList, ","));
            }
            // 更新
            boolean updateMainResult = this.updateById(litigationCases);
            if (updateMainResult) {
                // 保存成功后，将关联数据保存
                // 1、保存协作人员
                // TODO 等接口调试

                // 2、保存审理信息
                List<String> saveHearIds = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(hearInformationList)) {
                    for (Map hearInfo : hearInformationList) {
                        List processList = (ArrayList) hearInfo.get("caseProcess");
                        hearInfo.remove("caseProcess");
                        HearInformation hearInformation = Hzq.convertValue(hearInfo, HearInformation.class);
                        hearInformation.setRelationId(litigationCases.getId());
                        if (StringUtils.isEmpty(hearInformation.getId())) {
                            // 新增
                            boolean save = hearInformationService.save(hearInformation);
                            if (!save) {
                                throw new Exception("hear Information save fail");
                            }
                        } else {
                            hearInformation.setUpdateTime(systemMapper.getNow());
                            if (hearInformationService.updateById(hearInformation)) {
                                QueryWrapper<ProcessInformation> queryProcessWrapper = new QueryWrapper<>();
                                queryProcessWrapper
                                        .eq("relation_id", hearInformation.getId())
                                        .eq("data_type", BusinessEnum.CASES.getValue());
                                processInformationService.remove(queryProcessWrapper);
                            } else {
                                throw new Exception("hear Information update fail");
                            }
                        }

                        saveHearIds.add(hearInformation.getId());
                        // 新增案件进度信息
                        if (CollectionUtils.isNotEmpty(processList)) {
                            List<ProcessInformation> pList = new ArrayList<>();
                            for (Object obj : processList) {
                                Map map = (HashMap) obj;
                                String processContent = (String) map.get("content");
                                if (!StringUtils.isEmpty(processContent)) {
                                    Integer sort = (Integer) map.get("sort");
                                    ProcessInformation processInformation = new ProcessInformation();
                                    processInformation.setContent(processContent);
                                    processInformation.setRelationId(hearInformation.getId());
                                    processInformation.setDataType(BusinessEnum.CASES.getValue());
                                    processInformation.setSort(sort.longValue());
                                    pList.add(processInformation);
                                }
                            }
                            if (CollectionUtils.isNotEmpty(pList)) {
                                boolean processSaveFlag = processInformationService.saveBatch(pList);
                                if (!processSaveFlag) {
                                    throw new Exception("process Information save fail");
                                }
                            }
                        }
                    }
                }
                // 如果没有审理数据，则将之前的审理及其中的案件进展数据删除
                QueryWrapper<HearInformation> queryHearWrapper = new QueryWrapper<HearInformation>();
                queryHearWrapper.eq("relation_id", litigationCases.getId());
                if (CollectionUtils.isNotEmpty(saveHearIds)) {
                    queryHearWrapper.notIn("id", saveHearIds);
                }
                List<HearInformation> deleteHearList = hearInformationService.list(queryHearWrapper);
                if (CollectionUtils.isNotEmpty(deleteHearList)) {
                    for (HearInformation hear : deleteHearList) {
                        QueryWrapper<ProcessInformation> queryProcessWrapper = new QueryWrapper<ProcessInformation>();
                        queryProcessWrapper.eq("relation_id", hear.getId()).eq("data_type", BusinessEnum.CASES.getValue());
                        processInformationService.remove(queryProcessWrapper);
                    }

                    boolean hearDeleteFlag = hearInformationService.remove(queryHearWrapper);
                    if (!hearDeleteFlag) {
                        throw new Exception("hear delete fail");
                    }
                }
                // 3、附件保存
                QueryWrapper<EnclosureFiles> queryFilesWrapper = new QueryWrapper<EnclosureFiles>();
                queryFilesWrapper.eq("business_type", BusinessEnum.CASES.getValue()).eq("business_id", litigationCases.getId());
                enclosureFilesService.remove(queryFilesWrapper);
                if (files != null && files.size() > 0) {
                    for (EnclosureFiles file : files) {
                        file.setId(null);
                        file.setBusinessId(litigationCases.getId());
                        file.setBusinessType(BusinessEnum.CASES.getValue());
                    }
                    enclosureFilesService.saveBatch(files, files.size());
                }
            }
            return new ResponseBean(updateMainResult);
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
            LitigationCases litigationCases = this.getById(id);
            String servicePersonal = litigationCases.getServicePersonal();
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
        return new ResponseBean(this.updateBatchById(Hzq.convertValue(requestBean.getInfos(), LitigationCases.class)));
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
        if (flag) {
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
        if (flag) {
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
        LitigationCases litigationCases = this.getById((String) requestBean.getInfo());
        Map map = Hzq.beanToMap(litigationCases);
        //协作人员
        String cp = litigationCases.getCooperationPerson();
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
        // 获取审理信息
        QueryWrapper<HearInformation> queryHearWrapper = new QueryWrapper<HearInformation>();
        queryHearWrapper.orderByAsc("sort");
        queryHearWrapper.eq("relation_id", litigationCases.getId());
        List<HearInformation> hearList = hearInformationService.list(queryHearWrapper);
        List<Map> hearInformationList = new ArrayList<Map>();
        if (hearList != null && hearList.size() > 0) {
            for (HearInformation hear : hearList) {
                QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
                queryWrapper
                        .orderByAsc("sort")
                        .eq("relation_id", hear.getId())
                        .eq("data_type", BusinessEnum.CASES.getValue());
                List<ProcessInformation> processList = processInformationService.list(queryWrapper);
                if (processList == null || processList.size() == 0) {
                    processList.add(new ProcessInformation());
                }
                Map hearMap = Hzq.beanToMap(hear);
                hearMap.put("caseProcess", processList);
                hearInformationList.add(hearMap);
            }
        } else {
            // 组合空数据返回给前台，防止前台报错
            List<ProcessInformation> processList = new ArrayList<ProcessInformation>();
            processList.add(new ProcessInformation());
            Map nullMap = Hzq.beanToMap(new HearInformation());
            nullMap.put("caseProcess", processList);
            hearInformationList.add(nullMap);
        }
        map.put("hearInformationList", hearInformationList);
        // 获取附件数据
        QueryWrapper<EnclosureFiles> queryFilesWrapper = new QueryWrapper<EnclosureFiles>();
        queryFilesWrapper.eq("business_type", BusinessEnum.CASES.getValue()).eq("business_id", litigationCases.getId());
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
        QueryWrapper<LitigationCases> queryWrapper = new QueryWrapper<LitigationCases>();
        Map queryMap = page.getRecords().size() > 0 ? (HashMap) page.getRecords().get(0) : null;
        // 对于实体类不存在字段，需提前拿出来并删除原有，否则，反序列化会失败
        // TODO
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
        LitigationCases litigationCases = Hzq.convertValue(queryMap, LitigationCases.class);
        // 如果没有排序字段，默认按照更新事件倒序排列
        queryWrapper.orderByDesc("update_time");
        queryParamsSet(queryWrapper, litigationCases);
        //管理员查询权限控制
        queryParamsSetByAdmin(request, queryWrapper);
        return new ResponseBean(this.page(page, queryWrapper));
    }

    /**
     * 设置权限
     */
    private void queryParamsSetByAdmin(HttpServletRequest request, QueryWrapper<LitigationCases> queryWrapper) {
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
     * 列表公共参数查询
     *
     * @param queryWrapper
     * @param litigationCases
     */
    private void queryParamsSet(QueryWrapper<LitigationCases> queryWrapper, LitigationCases litigationCases) {
        // 在办、结案区分
        if (!StringUtils.isEmpty(litigationCases.getCaseStatus())) {
            queryWrapper.eq("case_status", litigationCases.getCaseStatus());
        }
        if (!StringUtils.isEmpty(litigationCases.getAscriptionCompany())) {
            queryWrapper.like("ascription_company", litigationCases.getAscriptionCompany());
        }
        if (!StringUtils.isEmpty(litigationCases.getTargetName())) {
            queryWrapper.like("target_name", litigationCases.getTargetName());
        }
        if (!StringUtils.isEmpty(litigationCases.getServicePersonal())) {
            queryWrapper.like("service_personal", litigationCases.getServicePersonal());
        }
        if (!StringUtils.isEmpty(litigationCases.getDisputeType())) {
            queryWrapper.eq("dispute_type", litigationCases.getDisputeType());
        }
        if (!StringUtils.isEmpty(litigationCases.getServiceType())) {
            queryWrapper.eq("service_type", litigationCases.getServiceType());
        }
        if (!StringUtils.isEmpty(litigationCases.getRiskGrade())) {
            queryWrapper.eq("risk_grade", litigationCases.getRiskGrade());
        }
        if (!StringUtils.isEmpty(litigationCases.getCaseType())) {
            queryWrapper.eq("case_type", litigationCases.getCaseType());
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
        LitigationCases litigationCases = Hzq.convertValue(requestBean.getInfo(), LitigationCases.class);
        litigationCases.setUpdateTime(systemMapper.getNow());
        return new ResponseBean(this.updateById(litigationCases));
    }

    /**
     * 公共下载方法
     *
     * @param queryMap 下载参数
     * @param request  请求
     * @param response 返回
     */
    public void downFile(Map queryMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
        QueryWrapper<LitigationCases> queryWrapper = new QueryWrapper<LitigationCases>();
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
        LitigationCases litigationCases = Hzq.convertValue(queryMap, LitigationCases.class);
        queryWrapper.orderByDesc("update_time");
        queryParamsSet(queryWrapper, litigationCases);
        //管理员查询权限控制
        queryParamsSetByAdmin(request, queryWrapper);
        List<LitigationCases> list = this.list(queryWrapper);
        writeExcel(list, "法务风险管理系统诉案件", response);
    }

    public void writeExcel(List<LitigationCases> list, String fileName, HttpServletResponse response) {
        String t1 = "";
        String t2 = "";
        String numT = "";
        String childT = "";
        String comma = ",";
        //获取配置文件数据
        String arr[] = exportTemplate.split(";");
        for (int i = 0; i < arr.length; i++) {
            if (i == 0) {
                t1 = arr[i];
            }
            if (i == 1) {
                numT = arr[i];
            }
            if (i == 2) {
                childT = arr[i].substring(1, arr[i].length() - 1);
            }
            if (i == 3) {
                t2 = arr[i];
            }
        }
        //创建标题
        //String t1 = "流程编号,申请日期,申请人,纠纷发生法人主体,业务所在部门,对方名称,金额,案件类型,纠纷类型,服务类型,服务人员,协作人员,诉求,案件简介,是否外聘律师,律所名称,律师姓名,是否产生费用,费用总额,保全费,诉讼费,代理费,其他费用,风险等级";
        //String numT = "仲裁程序,一审程序,二审程序,申请再审程序，再审程序，终审程序";
        //String childT = "立案受理时间,开庭时间,审理机构,案件分析,初步处理/答复意见,案件进展";
        //String t2 = "回款/减损金额,案件小结,案件复盘,案件状态";

        String[] firstArray = t1.split(comma);
        String[] childArray = childT.split(comma);
        String[] endArray = t2.split(comma);
        String[] numArray = numT.split(comma);
        //获取表头内容
        int headRow = 2;
        int headCol = firstArray.length + endArray.length + (numArray.length * childArray.length);
        String[][] headContext = new String[headRow][headCol];
        for (int i = 0; i < headRow; i++) {
            int cont = 0;
            for (int j = cont; j < firstArray.length; j++) {
                headContext[i][j] = firstArray[j];
            }

            cont += firstArray.length;
            for (int num = 0; num < numArray.length; num++) {
                for (int j = 0; j < childArray.length; j++) {
                    if (i == 0) {
                        headContext[i][j + cont] = numArray[num];
                    } else {
                        headContext[i][j + cont] = childArray[j];
                    }
                }

                cont += childArray.length;
            }

            for (int j = 0; j < endArray.length; j++) {
                headContext[i][j + cont] = endArray[j];
            }
        }
        // 第一步，创建一个HSSFWorkbook，对应一个Excel文件
        XSSFWorkbook wb = new XSSFWorkbook();

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        XSSFSheet sheet = wb.createSheet("Sheet1");

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        XSSFRow row = sheet.createRow(0);

        // 第四步，创建单元格，并设置值表头 设置表头居中
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont cell_Font = wb.createFont();
        cell_Font.setFontName("宋体");
        cell_Font.setFontHeightInPoints((short) 11);
        style.setFont(cell_Font);
        style.setAlignment(HorizontalAlignment.CENTER); // 创建一个居中格式
        //声明列对象
        XSSFCell cell;
        //设置表头数据
        for (int i = 0; i < headRow; i++) {
            if (i > 0) {
                row = sheet.createRow(i);
            }

            for (int j = 0; j < headCol; j++) {
                cell = row.createCell(j);
                cell.setCellValue(headContext[i][j]);
                cell.setCellStyle(style);
            }
        }

        //设置表头样式
        for (int i = 0; i < firstArray.length; i++) {
            sheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));
        }

        int cont = firstArray.length;
        for (int i = 0; i < numArray.length; i++) {
            int len = cont + childArray.length;
            sheet.addMergedRegion(new CellRangeAddress(0, 0, cont, len - 1));
            cont = len;
        }

        for (int i = cont; i < cont + endArray.length; i++) {
            sheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));
        }
        //获取查询结果数据
        List<Map> mapList = listToMap(list);
        String[][] content = new String[mapList.size()][headCol];
        for (int i = 0; i < mapList.size(); i++) {
            Map tr = mapList.get(i);
            int cellnum = 0;
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("flowNo")) ? (String) tr.get("flowNo") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("applyDate")) ? (String) tr.get("applyDate") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("applicant")) ? (String) tr.get("applicant") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("ascriptionCompany")) ? (String) tr.get("ascriptionCompany") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("unitName")) ? (String) tr.get("unitName") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("targetName")) ? (String) tr.get("targetName") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("money")) ? ((BigDecimal) tr.get("money")).setScale(2, BigDecimal.ROUND_UP).toString() : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("caseType")) ? (String) tr.get("caseType") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("disputeType")) ? (String) tr.get("disputeType") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("serviceType")) ? (String) tr.get("serviceType") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("servicePersonal")) ? (String) tr.get("servicePersonal") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("cooperationPerson")) ? (String) tr.get("cooperationPerson") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("appeal")) ? (String) tr.get("appeal") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("caseBrief")) ? (String) tr.get("caseBrief") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("outsideLawyerFlag")) ? (String) tr.get("outsideLawyerFlag") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("lawFirmName")) ? (String) tr.get("lawFirmName") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("lawerName")) ? (String) tr.get("lawerName") : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("costFlag")) ? (String) tr.get("costFlag") + "" : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("costAmount")) ? ((BigDecimal) tr.get("costAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("preservationAmount")) ? ((BigDecimal) tr.get("preservationAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("litigationAmount")) ? ((BigDecimal) tr.get("litigationAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("agentAmount")) ? ((BigDecimal) tr.get("agentAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("otherAmount")) ? ((BigDecimal) tr.get("otherAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][cellnum++] = !StringUtils.isEmpty(tr.get("riskGrade")) ? (String) tr.get("riskGrade") : "";
            //审理
            List<Map> hearInfoListMap = (List<Map>) tr.get("hearInfoListMap");
            if (CollectionUtils.isNotEmpty(hearInfoListMap)) {
                for (Map hearInfo : hearInfoListMap) {
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("hearingProcedure")) ? (String) hearInfo.get("hearingProcedure") : "";
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("filingDate")) ? (String) hearInfo.get("filingDate") : "";
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("openDate")) ? (String) hearInfo.get("openDate") : "";
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("hearingOrgan")) ? (String) hearInfo.get("hearingOrgan") : "";
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("caseAnalysis")) ? (String) hearInfo.get("caseAnalysis") : "";
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("preliminaryOpinion")) ? (String) hearInfo.get("preliminaryOpinion") : "";
                    content[i][cellnum++] = !StringUtils.isEmpty(hearInfo.get("caseProcess")) ? (String) hearInfo.get("caseProcess") : "";
                }
            }
            //结尾
            content[i][headCol - 4] = !StringUtils.isEmpty(tr.get("derogationAmount")) ? ((BigDecimal) tr.get("derogationAmount")).setScale(2, BigDecimal.ROUND_UP).toString() + "" : "";
            content[i][headCol - 3] = !StringUtils.isEmpty(tr.get("summary")) ? (String) tr.get("summary") : "";
            content[i][headCol - 2] = !StringUtils.isEmpty(tr.get("replay")) ? (String) tr.get("replay") : "";
            content[i][headCol - 1] = !StringUtils.isEmpty(tr.get("closingMethod")) ? (String) tr.get("closingMethod") : "";
        }

        //创建内容
        for (int i = 0; i < content.length; i++) {
            row = sheet.createRow(i + headRow);
            for (int j = 0; j < headCol; j++) {
                //将内容按顺序赋给对应的列对象
                row.createCell(j).setCellValue(content[i][j]);
            }
        }
        //数据写入表格
        try {
            ExcelUtil.setResponseHeader(response, fileName + "-" + System.currentTimeMillis() + ".xlsx");
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map> listToMap(List<LitigationCases> litigationCasesList) {
        if (CollectionUtils.isEmpty(litigationCasesList)) {
            return new ArrayList<>(0);
        }

        List<Map> maps = new ArrayList<>();
        Map<String, List<Dictionary>> cacheMap = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (LitigationCases litigationCases : litigationCasesList) {
            Map map = Hzq.beanToMap(litigationCases);
            // 申请日期
            if (!StringUtils.isEmpty(litigationCases.getApplyDate())) {
                map.put("applyDate", sdf.format(litigationCases.getApplyDate()));
            }
            // 案件类型
            if (!StringUtils.isEmpty(litigationCases.getDisputeType())) {
                map.put("caseType", cacheUtil.getValueByCode("caseType", litigationCases.getCaseType(), cacheMap));
            }
            // 纠纷类型
            if (!StringUtils.isEmpty(litigationCases.getDisputeType())) {
                map.put("disputeType", cacheUtil.getValueByCode("disputeType", litigationCases.getDisputeType(), cacheMap));
            }
            // 服务类型
            if (!StringUtils.isEmpty(litigationCases.getServiceType())) {
                map.put("serviceType", cacheUtil.getValueByCode("serviceType", litigationCases.getServiceType(), cacheMap));
            }
            // 诉求
            if (!StringUtils.isEmpty(litigationCases.getAppeal())) {
                String[] appeals = litigationCases.getAppeal().split(",");
                StringBuffer sbf = new StringBuffer();
                for (String appealType : appeals) {
                    sbf.append(cacheUtil.getValueByCode("appealType", litigationCases.getAppeal(), cacheMap));
                }
                map.put("appeal", sbf.toString());
            }
            // 是否外聘律师
            if (!StringUtils.isEmpty(litigationCases.getOutsideLawyerFlag())) {
                map.put("outsideLawyerFlag", cacheUtil.getValueByCode("outsideLawerFlag", litigationCases.getOutsideLawyerFlag(), cacheMap));
            }
            // 是否产生费用
            if (!StringUtils.isEmpty(litigationCases.getCostFlag())) {
                map.put("costFlag", cacheUtil.getValueByCode("costFlag", litigationCases.getCostFlag(), cacheMap));
            }
            // 风险等级
            if (!StringUtils.isEmpty(litigationCases.getRiskGrade())) {
                map.put("riskGrade", cacheUtil.getValueByCode("riskGrade", litigationCases.getRiskGrade(), cacheMap));
            }
            // 案件状态
            if (!StringUtils.isEmpty(litigationCases.getCaseStatus())) {
                map.put("closingMethod", cacheUtil.getValueByCode("closingMethod", litigationCases.getClosingMethod(), cacheMap));
            }

            //审理信息
            List<Map> hearInfoListMap = getHearInfoMap(cacheMap, sdf, litigationCases.getId());
            map.put("hearInfoListMap", hearInfoListMap);
            maps.add(map);
        }

        return maps;
    }

    /**
     * 获取审理信息map
     */
    private List<Map> getHearInfoMap(Map<String, List<Dictionary>> cacheMap, SimpleDateFormat sdf, String litigationCasesId) {
        QueryWrapper<HearInformation> Wrapper = new QueryWrapper<HearInformation>();
        Wrapper.eq("relation_id", litigationCasesId);
        Wrapper.orderByAsc("sort");
        List<HearInformation> hearInformationLists = hearInformationService.list(Wrapper);
        if (CollectionUtils.isEmpty(hearInformationLists)) {
            return new ArrayList<>(0);
        }

        List<Map> hearInfoListMap = new ArrayList<>();
        for (HearInformation hearInformation : hearInformationLists) {
            Map hearInfoMap = Hzq.beanToMap(hearInformation);
            hearInfoListMap.add(hearInfoMap);
            //审理程序
            if (!StringUtils.isEmpty(hearInformation.getHearingProcedure())) {
                hearInfoMap.put("hearingProcedure", cacheUtil.getValueByCode("hearingProcedure", hearInformation.getHearingProcedure(), cacheMap));
            }
            //立案受理时间
            if (!StringUtils.isEmpty(hearInformation.getFilingDate())) {
                hearInfoMap.put("filingDate", sdf.format(hearInformation.getFilingDate()));
            }
            //开庭时间
            if (!StringUtils.isEmpty(hearInformation.getOpenDate())) {
                hearInfoMap.put("openDate", sdf.format(hearInformation.getOpenDate()));
            }

            // 案件进展
            String progressInfos = getProgress(hearInformation.getId());
            hearInfoMap.put("caseProcess", progressInfos);
        }

        return hearInfoListMap;
    }

    /**
     * 获取案件分析
     */
    private String getProgress(String hearInformationId) {
        QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("relation_id", hearInformationId)
                .eq("data_type", BusinessEnum.CASES.getValue());
        List<ProcessInformation> processInfos = processInformationService.list(queryWrapper);
        if (CollectionUtils.isEmpty(processInfos)) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        for (ProcessInformation processInformation : processInfos) {
            if (!StringUtils.isEmpty(processInformation.getContent())) {
                sb.append(processInformation.getContent() + "  ");
            }
        }

        return sb.toString();
    }
}
