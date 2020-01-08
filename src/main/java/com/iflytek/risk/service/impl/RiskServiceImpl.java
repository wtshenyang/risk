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
import com.iflytek.risk.mapper.RiskMapper;
import com.iflytek.risk.mapper.SystemMapper;
import com.iflytek.risk.sec.SSOUser;
import com.iflytek.risk.service.IEnclosureFilesService;
import com.iflytek.risk.service.IProcessInformationService;
import com.iflytek.risk.service.IRemindTodoService;
import com.iflytek.risk.service.IRiskService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 风险信息管理表 服务实现类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
@Service
@Transactional(propagation = Propagation.NESTED, isolation = Isolation.DEFAULT, readOnly = false, rollbackFor = Exception.class)
public class RiskServiceImpl extends ServiceImpl<RiskMapper, Risk> implements IRiskService {

    @Resource
    IProcessInformationService processInformationService;
    @Resource
    IEnclosureFilesService enclosureFilesService;
    @Resource
    IRemindTodoService remindTodoService;
    @Resource
    CacheUtil cacheUtil;
    @Value("${file.direction}")
    private String templatesRoot;
    @Resource
    SystemMapper systemMapper;
    @Value("${file.template.risk}")
    private String exportTemplate;

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
     * 单个新增
     *
     * @param requestBean
     * @return
     */
    public ResponseBean add(RequestBean requestBean) {
        try {
            // 获取其他数据
            Map object = (LinkedHashMap) requestBean.getInfo();
            List eventProgress = (ArrayList) object.get("eventProgress");
            List cooperationPersonnel = (ArrayList) object.get("cooperationPersonnel");
            List<EnclosureFiles> files = object.get("files") != null ? Hzq.convertValue((ArrayList) object.get("files"), EnclosureFiles.class) : null;
            object.remove("eventProgress");
            object.remove("cooperationPersonnel");
            object.remove("files");
            Risk risk = Hzq.convertValue(requestBean.getInfo(), Risk.class);
            //处理协作人员
            if (!CollectionUtils.isEmpty(cooperationPersonnel)) {
                risk.setCooperationPerson(RiskUtils.listToString(cooperationPersonnel, ","));
            }
            // 保存
            boolean saveResult = this.save(risk);
            if (saveResult) {
                // 保存成功后，将关联数据保存
                // 1、保存案件进展
                if (eventProgress != null && eventProgress.size() > 0) {
                    List<ProcessInformation> pList = new ArrayList<>();
                    Long index = 1L;
                    for (Object obj : eventProgress) {
                        Map map = (HashMap) obj;
                        String processContent = (String) map.get("content");
                        if (!StringUtils.isEmpty(processContent)) {
                            ProcessInformation processInformation = new ProcessInformation();
                            processInformation.setContent(processContent);
                            processInformation.setRelationId(risk.getId());
                            processInformation.setDataType(BusinessEnum.RISK.getValue());
                            processInformation.setSort(index);
                            pList.add(processInformation);
                            index++;
                        }
                    }
                    if (pList != null && pList.size() > 0) {
                        boolean eventSaveFlag = processInformationService.saveBatch(pList);
                        if (!eventSaveFlag) {
                            throw new Exception("eventProcess information save fail");
                        }
                    }
                }
                // 3、附件保存

                if (files != null && files.size() > 0) {
                    for (EnclosureFiles file : files) {
                        file.setId(null);
                        file.setBusinessId(risk.getId());
                        file.setBusinessType(BusinessEnum.RISK.getValue());
                    }
                    enclosureFilesService.saveBatch(files, files.size());
                }
            } else {
                throw new Exception("main information save fail");
            }
            return new ResponseBean(saveResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 批量新增
     *
     * @param requestBean
     * @return
     */
    public ResponseBean addBatch(RequestBean requestBean) {
        return new ResponseBean(this.saveBatch(Hzq.convertValue(requestBean.getInfos(), Risk.class)));
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
            List eventProgress = (ArrayList) object.get("eventProgress");
            List cooperationPersonnel = (ArrayList) object.get("cooperationPersonnel");
            List<EnclosureFiles> files = object.get("files") != null ? Hzq.convertValue((ArrayList) object.get("files"), EnclosureFiles.class) : null;
            object.remove("eventProgress");
            object.remove("files");
            object.remove("cooperationPersonnel");
            Risk risk = Hzq.convertValue(requestBean.getInfo(), Risk.class);
            risk.setUpdateTime(systemMapper.getNow());
            //判断当前操作人是否有权限操作该数据
            if (!checkServicePersonal(request, risk.getId())) {
                return new ResponseBean(
                        CommonConstants.FAIL.getCode(),
                        "您没有相关权限，请联系管理员"
                );
            }
            //处理协作人员
            if (!CollectionUtils.isEmpty(cooperationPersonnel)) {
                risk.setCooperationPerson(RiskUtils.listToString(cooperationPersonnel, ","));
            }
            // 保存
            boolean updateResult = this.updateById(risk);
            if (updateResult) {
                // 1、保存事件进展，删除原有进展数据数据，重新添加
                QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
                queryWrapper
                        .eq("relation_id", risk.getId())
                        .eq("data_type", BusinessEnum.RISK.getValue());
                processInformationService.remove(queryWrapper);
                if (eventProgress != null && eventProgress.size() > 0) {
                    List<ProcessInformation> pList = new ArrayList<>();
                    Long index = 1L;
                    for (Object obj : eventProgress) {
                        Map map = (HashMap) obj;
                        String processContent = (String) map.get("content");
                        if (!StringUtils.isEmpty(processContent)) {
                            ProcessInformation processInformation = new ProcessInformation();
                            processInformation.setContent(processContent);
                            processInformation.setRelationId(risk.getId());
                            processInformation.setDataType(BusinessEnum.RISK.getValue());
                            processInformation.setSort(index);
                            pList.add(processInformation);
                            index++;
                        }
                    }
                    boolean eventSaveFlag = processInformationService.saveBatch(pList);
                    if (!eventSaveFlag) {
                        throw new Exception("eventProcess information save fail");
                    }
                }
                // 3、附件保存
                QueryWrapper<EnclosureFiles> queryFilesWrapper = new QueryWrapper<EnclosureFiles>();
                queryFilesWrapper.eq("business_type", BusinessEnum.RISK.getValue()).eq("business_id", risk.getId());
                enclosureFilesService.remove(queryFilesWrapper);
                if (files != null && files.size() > 0) {
                    for (EnclosureFiles file : files) {
                        file.setId(null);
                        file.setBusinessId(risk.getId());
                        file.setBusinessType(BusinessEnum.RISK.getValue());
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
            Risk risk = this.getById(id);
            String servicePersonal = risk.getServicePersonal();
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
        return new ResponseBean(this.updateBatchById(Hzq.convertValue(requestBean.getInfos(), Risk.class)));
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
        Risk risk = this.getById((String) requestBean.getInfo());
        Map map = Hzq.beanToMap(risk);
        //协作人员
        String cp = risk.getCooperationPerson();
        if (!StringUtils.isEmpty(cp)) {
            List cooperationPersonnel = RiskUtils.stringToList(cp, ",");
            map.put("cooperationPersonnel",cooperationPersonnel);
        }
        // 获取案件进展数据
        QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
        queryWrapper
                .orderByAsc("sort")
                .eq("relation_id", requestBean.getInfo())
                .eq("data_type", BusinessEnum.RISK.getValue());
        List<ProcessInformation> processList = processInformationService.list(queryWrapper);
        map.put("eventProgress", processList);
        // 获取附件数据
        QueryWrapper<EnclosureFiles> queryFilesWrapper = new QueryWrapper<EnclosureFiles>();
        queryFilesWrapper.eq("business_type", BusinessEnum.RISK.getValue()).eq("business_id", risk.getId());
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
        QueryWrapper<Risk> queryWrapper = new QueryWrapper<Risk>();
        Map queryMap = page.getRecords().size() > 0 ? (HashMap) page.getRecords().get(0) : null;
        // 对于实体类不存在字段，需提前拿出来并删除原有，否则，反序列化会失败
        List<Date> regitraterDateList = (List<Date>) queryMap.get("registrateTime");
        if (regitraterDateList != null && regitraterDateList.size() == 2) {
            queryMap.remove("registrateTime");
            queryWrapper.between("registrate_time", regitraterDateList.get(0), regitraterDateList.get(1));
        }
        // 序列化
        Risk risk = Hzq.convertValue(queryMap, Risk.class);
        // 如果没有排序字段，默认按照更新事件倒序排列
        queryWrapper.orderByDesc("update_time");
        queryParamsSet(queryWrapper, risk);
        //管理员查询权限控制
        queryParamsSetByAdmin(request, queryWrapper);
        return new ResponseBean(this.page(page, queryWrapper));
    }

    /**
     * 设置权限
     */
    private void queryParamsSetByAdmin(HttpServletRequest request, QueryWrapper<Risk> queryWrapper) {
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

    private void queryParamsSet(QueryWrapper<Risk> queryWrapper, Risk risk) {
        // 在办、结案区分
        if (!StringUtils.isEmpty(risk.getEventStatus())) {
            queryWrapper.eq("event_status", risk.getEventStatus());
        }
        if (!StringUtils.isEmpty(risk.getLegalEntity())) {
            queryWrapper.like("legal_entity", risk.getLegalEntity());
        }
        if (!StringUtils.isEmpty(risk.getBusinessUnit())) {
            queryWrapper.like("business_unit", risk.getBusinessUnit());
        }
        if (!StringUtils.isEmpty(risk.getRiskMatter())) {
            queryWrapper.like("risk_matter", risk.getRiskMatter());
        }
        if (!StringUtils.isEmpty(risk.getRiskGrade())) {
            queryWrapper.eq("risk_grade", risk.getRiskGrade());
        }
        if (!StringUtils.isEmpty(risk.getRiskType())) {
            queryWrapper.eq("risk_type", risk.getRiskType());
        }
        if (!StringUtils.isEmpty(risk.getServicePersonal())) {
            queryWrapper.like("service_personal", risk.getServicePersonal());
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
        Risk risk = Hzq.convertValue(requestBean.getInfo(), Risk.class);
        risk.setUpdateTime(systemMapper.getNow());
        return new ResponseBean(this.updateById(risk));
    }

    /**
     * 公共下载方法
     *
     * @param queryMap 下载参数
     * @param request  请求
     * @param response 返回
     */
    public void downFile(Map queryMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
        QueryWrapper<Risk> queryWrapper = new QueryWrapper<Risk>();
        List<Date> regitraterDateList = (List<Date>) queryMap.get("registrateTime");
        if (regitraterDateList != null && regitraterDateList.size() == 2) {
            queryMap.remove("registrateTime");
            queryWrapper.between("registrate_time", regitraterDateList.get(0), regitraterDateList.get(1));
        }

        Risk risk = Hzq.convertValue(queryMap, Risk.class);
        queryWrapper.orderByDesc("update_time");
        queryParamsSet(queryWrapper, risk);
        //管理员查询权限控制
        queryParamsSetByAdmin(request, queryWrapper);
        List<Risk> list = this.list(queryWrapper);
        writeExcel(list, "法务风险管理系统风险登记信息", response);
    }

    /**
     * 数据写入
     *
     * @param list
     * @param fileName
     * @param response
     */
    public void writeExcel(List<Risk> list, String fileName, HttpServletResponse response) {
        String[] title = exportTemplate.split(",");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map> mapList = this.listToMap(list);

        String[][] content = new String[mapList.size()][];
        for (int i = 0; i < mapList.size(); i++) {
            content[i] = new String[title.length];
            //将对象内容转换成string
            Map tr = mapList.get(i);
            content[i][0] = RiskUtils.getMapValueToString(tr.get("registrateTime"));
            content[i][1] = RiskUtils.getMapValueToString(tr.get("legalEntity"));
            content[i][2] = RiskUtils.getMapValueToString(tr.get("businessUnit"));
            content[i][3] = RiskUtils.getMapValueToString(tr.get("riskGrade"));
            content[i][4] = RiskUtils.getMapValueToString(tr.get("riskType"));
            content[i][5] = RiskUtils.getMapValueToString(tr.get("servicePersonal"));
            content[i][6] = RiskUtils.getMapValueToString(tr.get("cooperationPerson"));
            content[i][7] = RiskUtils.getMapValueToString(tr.get("riskMatter"));
            content[i][8] = RiskUtils.getMapValueToString(tr.get("riskAnalysis"));
            content[i][9] = RiskUtils.getMapValueToString(tr.get("preliminaryOpinion"));
            content[i][10] = RiskUtils.getMapValueToString(tr.get("eventProgress"));
            content[i][11] = RiskUtils.getMapValueToString(tr.get("summary"));

        }
        //创建HSSFWorkbook
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
    public List<Map> listToMap(List<Risk> list) {
        List<Map> maps = new ArrayList<Map>();
        Map<String, List<Dictionary>> cacheMap = new HashMap<String, List<Dictionary>>();
        if (list != null && list.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Risk risk : list) {
                Map map = Hzq.beanToMap(risk);
                // 登记日期
                if (!StringUtils.isEmpty(risk.getRegistrateTime())) {
                    map.put("registrateTime", sdf.format(risk.getRegistrateTime()));
                }
                // 风险等级
                if (!StringUtils.isEmpty(risk.getRiskGrade())) {
                    map.put("riskGrade", cacheUtil.getValueByCode("riskGrade", risk.getRiskGrade(), cacheMap));
                }
                // 风险类型
                if (!StringUtils.isEmpty(risk.getRiskType())) {
                    map.put("riskType", cacheUtil.getValueByCode("riskType", risk.getRiskType(), cacheMap));
                }

                // 事件进展
                QueryWrapper<ProcessInformation> queryWrapper = new QueryWrapper<ProcessInformation>();
                queryWrapper
                        .eq("relation_id", risk.getId())
                        .eq("data_type", BusinessEnum.RISK.getValue());
                List<ProcessInformation> processInformations = processInformationService.list(queryWrapper);
                if (processInformations != null && processInformations.size() > 0) {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (ProcessInformation processInformation : processInformations) {
                        if (!StringUtils.isEmpty(processInformation.getContent())) {
                            stringBuffer.append(processInformation.getContent());
                        }
                    }
                    map.put("eventProgress", stringBuffer.toString());
                }
                maps.add(map);
            }
        }
        return maps;
    }


}
