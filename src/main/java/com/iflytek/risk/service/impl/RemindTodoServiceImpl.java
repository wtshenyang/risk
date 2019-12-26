package com.iflytek.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iflytek.risk.common.Hzq;
import com.iflytek.risk.common.RequestBean;
import com.iflytek.risk.common.ResponseBean;
import com.iflytek.risk.entity.RemindTodo;
import com.iflytek.risk.enums.BusinessEnum;
import com.iflytek.risk.enums.CommonConstants;
import com.iflytek.risk.enums.SystemMessageEnum;
import com.iflytek.risk.mapper.RemindTodoMapper;
import com.iflytek.risk.schedule.CronTaskRegistrar;
import com.iflytek.risk.schedule.SchedulingRunnable;
import com.iflytek.risk.service.IRemindTodoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 提醒记录表  服务实现类
 * </p>
 *
 * @author 黄智强
 * @since 2019-11-19
 */
@Service
@Transactional(propagation = Propagation.NESTED, isolation = Isolation.DEFAULT, readOnly = false, rollbackFor = Exception.class)
public class RemindTodoServiceImpl extends ServiceImpl<RemindTodoMapper, RemindTodo> implements IRemindTodoService {

    @Resource
    private JavaMailSender jms;
    @Value("${spring.mail.username}")
    private String formUser;
    @Value("${spring.mail.title}")
    private String mailTitle;
    @Value("${spring.mail.emailSuffix}")
    private String emailSuffix;
    @Resource
    private CronTaskRegistrar cronTaskRegistrar;

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
                return updateAllField(requestBean);
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
                return getPage(requestBean);
            case DELETE_AND_ADD:
                return deleteAndAdd(requestBean);
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
        return new ResponseBean(this.save(Hzq.convertValue(requestBean.getInfo(), RemindTodo.class)));
    }

    /**
     * 批量新增
     *
     * @param requestBean
     * @return
     */
    public ResponseBean addBatch(RequestBean requestBean) {
        return new ResponseBean(this.saveBatch(Hzq.convertValue(requestBean.getInfos(), RemindTodo.class)));
    }

    /**
     * 更新单条数据所有字段
     *
     * @param requestBean
     * @return
     */
    public ResponseBean updateAllField(RequestBean requestBean) {
        return new ResponseBean(this.updateById(Hzq.convertValue(requestBean.getInfo(), RemindTodo.class)));
    }

    /**
     * 更新批量数据所有字段
     *
     * @param requestBean
     * @return
     */
    public ResponseBean updateAllFieldBatch(RequestBean requestBean) {
        return new ResponseBean(this.updateBatchById(Hzq.convertValue(requestBean.getInfos(), RemindTodo.class)));
    }

    /**
     * 单条逻辑删除
     *
     * @param requestBean
     * @return
     */
    public ResponseBean deleteLogicalSingle(RequestBean requestBean) {
        return new ResponseBean(this.removeById((String) requestBean.getInfo()));
    }

    /**
     * 批量逻辑删除
     *
     * @param requestBean
     * @return
     */
    public ResponseBean deleteLogicalBatch(RequestBean requestBean) {
        return new ResponseBean(this.removeByIds((Collection<String>) requestBean.getInfo()));
    }

    /**
     * 根据主键获取单条数据
     *
     * @param requestBean
     * @return
     */
    public ResponseBean getInfoById(RequestBean requestBean) {
        return new ResponseBean(this.getById((String) requestBean.getInfo()));
    }

    /**
     * 根据条件查询数据
     *
     * @param requestBean
     * @return
     */
    public ResponseBean getListByCondition(RequestBean requestBean) {
        QueryWrapper queryWrapper = new QueryWrapper();
        // TODO 添加查询条件
        RemindTodo remindTodo = Hzq.convertValue(requestBean.getInfo(), RemindTodo.class);
        if (!StringUtils.isEmpty(remindTodo.getRelationId())) {
            queryWrapper.orderByAsc("remind_date");
            queryWrapper.orderByAsc("update_time");
            queryWrapper.orderByAsc("personnel_id");
            queryWrapper.eq("relation_id", remindTodo.getRelationId());
        }
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
    public ResponseBean getPage(RequestBean requestBean) {
        Page page = Hzq.convertValue(requestBean.getInfo(), Page.class);
        if (StringUtils.isEmpty(page)) {
            page = new Page();
        }
        Wrapper queryWrapper = new QueryWrapper();
        // TODO 添加查询条件

        return new ResponseBean(this.page(page, queryWrapper));
    }

    /**
     * 批量新增(新增之前，删除原有数据)
     *
     * @param requestBean
     * @return
     */
    public ResponseBean deleteAndAdd(RequestBean requestBean) {
        List<RemindTodo> remindList = Hzq.convertValue(requestBean.getInfos(), RemindTodo.class);
        if (remindList != null && remindList.size() > 0) {
            String relationId = remindList.get(0).getRelationId();
            //去除已发送邮件的定时任务
            removeHasSendEmailTask(relationId);
            QueryWrapper<RemindTodo> queryWrapper = new QueryWrapper<RemindTodo>();
            queryWrapper.eq("relation_id", relationId);
            this.remove(queryWrapper);
            for (RemindTodo remindTodo : remindList) {
                remindTodo.setId(null);
                remindTodo.setCreateTime(null);
                remindTodo.setUpdateTime(null);
                String targetEmailOrigin = remindTodo.getPersonnelId().split("\\(")[1];
                String targetEmail = targetEmailOrigin.substring(0, targetEmailOrigin.length() - 1) + "@" + emailSuffix;
                remindTodo.setTargetEmail(targetEmail);
                remindTodo.setRemindDateRegular(Hzq.getCron(remindTodo.getRemindDate()));
                this.save(remindTodo);
                this.addTask(remindTodo);
            }
            return new ResponseBean(true);
        } else {
            return new ResponseBean(
                    CommonConstants.FAIL.getCode(),
                    SystemMessageEnum.ENTITY_IS_NULL.getValue()
            );
        }
    }

    /**
     * 去除已发送邮件的定时任务
     *
     * @param relationId
     */
    private void removeHasSendEmailTask(String relationId) {
        if(StringUtils.isEmpty(relationId)){
            return;
        }

        QueryWrapper<RemindTodo> queryWrapper = new QueryWrapper<RemindTodo>();
        queryWrapper.eq("relation_id", relationId);
        queryWrapper.eq("send_flag", Integer.parseInt(BusinessEnum.MAIL_SEND.getValue()));
        List<RemindTodo> dbRemindList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(dbRemindList)) {
            return;
        }

        for (RemindTodo remindTodo : dbRemindList) {
            SchedulingRunnable task = new SchedulingRunnable("remindTodoServiceImpl", "sendMail", remindTodo.getId());
            cronTaskRegistrar.removeCronTask(task);
        }
    }

    /**
     * @param remindId 主键
     */
    @Override
    public void sendMail(String remindId) {
        // 获取待发送提醒数据
        QueryWrapper<RemindTodo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("send_flag", BusinessEnum.MAIL_NOT_SEND).eq("id", remindId);
        RemindTodo remindTodo = this.getOne(queryWrapper);
        if (remindTodo == null) {
            log.error("target info is not exist or already send, result send mail fail, target Id=" + remindId);
        } else {
            SimpleDateFormat sdfTemp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long start = System.currentTimeMillis();
            log.warn("===================开始发送邮件=========================" + sdfTemp.format(new Date()));
            SimpleMailMessage mainMessage = new SimpleMailMessage();
            log.warn("to:" + remindTodo.getTargetEmail());
            mainMessage.setFrom(formUser);
            mainMessage.setTo(remindTodo.getTargetEmail());
            mainMessage.setSubject(mailTitle);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            mainMessage.setText("[提醒时间]" + sdf.format(remindTodo.getRemindDate()) + "\n[待办事项]" + remindTodo.getEventTitle());
            log.warn("content:" + remindTodo.toString());
            jms.send(mainMessage);
            // 发送成功后 更新原始数据
            remindTodo.setSendFlag(Integer.parseInt(BusinessEnum.MAIL_SEND.getValue()));
            this.updateById(remindTodo);
            log.warn("===================结束发送邮件=========================" + sdfTemp.format(new Date()));
        }
    }

    public void addTask(RemindTodo remindTodo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String remindId = remindTodo.getId();
        String cron = remindTodo.getRemindDateRegular();
        if (!StringUtils.isEmpty(cron)) {
            log.warn("添加定时发送邮件任务-任务名称：" + remindTodo.getEventTitle()
                    + "，任务主键：" + remindTodo.getId()
                    + "，执行时间：" + sdf.format(remindTodo.getRemindDate())
                    + "，目标邮箱：" + remindTodo.getTargetEmail()
                    + "，正则：" + remindTodo.getRemindDateRegular()
            );
            SchedulingRunnable task = new SchedulingRunnable("remindTodoServiceImpl", "sendMail", remindId);
            cronTaskRegistrar.addCronTask(task, cron);
        }
    }
}
