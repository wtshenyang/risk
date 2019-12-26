DROP TABLE if exists t_fwfx_non_litigation_cases;/*SkipError*/
CREATE TABLE t_fwfx_non_litigation_cases
(
    id                  VARCHAR(64) NOT NULL COMMENT '主键',
    flow_no             VARCHAR(64) COMMENT '流程编号',
    apply_date          DATE COMMENT '申请日期',
    ascription_company  VARCHAR(64) COMMENT '纠纷发生法人主体',
    unit_name           VARCHAR(64) COMMENT '业务所在部门',
    applicant           VARCHAR(64) COMMENT '申请人',
    target_name         VARCHAR(32) COMMENT '对方名称',
    money               DECIMAL(32, 10) COMMENT '金额',
    case_type           VARCHAR(32) COMMENT '案件类型',
    dispute_type        VARCHAR(32) COMMENT '纠纷类型',
    service_type        VARCHAR(32) COMMENT '服务类型',
    service_personal    TEXT COMMENT '服务人员',
    cooperation_person  TEXT COMMENT '协作人员',
    appeal              TEXT COMMENT '诉求',
    other_appeal        TEXT COMMENT '其他诉求补充说明',
    case_brief          TEXT COMMENT '案件简介',
    outside_lawyer_flag VARCHAR(32) COMMENT '外聘律师',
    law_firm_name       VARCHAR(128) COMMENT '律所名称',
    lawer_name          VARCHAR(32) COMMENT '律师姓名',
    cost_flag           VARCHAR(32) COMMENT '是否产生费用',
    cost_amount         DECIMAL(32, 8) COMMENT '费用总额',
    preservation_amount DECIMAL(32, 8) COMMENT '保全费',
    agent_amount        DECIMAL(32, 8) COMMENT '代理费',
    other_amount        DECIMAL(32, 8) COMMENT '其他费用',
    case_analysis       TEXT COMMENT '案件分析',
    preliminary_opinion TEXT COMMENT '初步处理/答复意见',
    derogation_amount   DECIMAL(32, 8) COMMENT '回款/减损金额',
    risk_grade          VARCHAR(32) COMMENT '风险等级',
    case_status         VARCHAR(32) DEFAULT 'working' COMMENT '案件状态',
    summary             TEXT COMMENT '案件小结',
    replay              TEXT COMMENT '案件复盘',
    create_time         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator             VARCHAR(64) COMMENT '创建人',
    update_time         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag         INT         DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '非诉讼类案件表 ';

ALTER TABLE t_fwfx_non_litigation_cases
    COMMENT '非诉讼类案件表';
DROP TABLE if exists t_fwfx_litigation_cases;/*SkipError*/
CREATE TABLE t_fwfx_litigation_cases
(
    id                  VARCHAR(64) NOT NULL COMMENT '主键',
    flow_no             VARCHAR(64) COMMENT '流程编号',
    apply_date          DATE COMMENT '申请日期',
    ascription_company  VARCHAR(64) COMMENT '公司归属',
    unit_name           VARCHAR(64) COMMENT '部门名称',
    applicant           VARCHAR(64) COMMENT '申请人',
    target_name         VARCHAR(32) COMMENT '对方名称',
    money               DECIMAL(32, 10) COMMENT '金额',
    case_type           VARCHAR(32) COMMENT '案件类型',
    dispute_type        VARCHAR(32) COMMENT '纠纷类型',
    service_type        VARCHAR(32) COMMENT '服务类型',
    service_personal    TEXT COMMENT '服务人员',
    cooperation_person  TEXT COMMENT '协作人员',
    appeal              TEXT COMMENT '诉求',
    other_appeal        TEXT COMMENT '其他诉求补充说明',
    case_brief          TEXT COMMENT '案件简介',
    outside_lawyer_flag VARCHAR(32) COMMENT '外聘律师',
    law_firm_name       VARCHAR(128) COMMENT '律所名称',
    lawer_name          VARCHAR(32) COMMENT '律师姓名',
    cost_flag           VARCHAR(32) COMMENT '是否产生费用',
    cost_amount         DECIMAL(32, 8) COMMENT '费用总额',
    preservation_amount DECIMAL(32, 8) COMMENT '保全费',
    agent_amount        DECIMAL(32, 8) COMMENT '代理费',
    litigation_amount   DECIMAL(32, 8) COMMENT '诉讼费',
    other_amount        DECIMAL(32, 8) COMMENT '其他费用',
    derogation_amount   DECIMAL(32, 8) COMMENT '回款/减损金额',
    risk_grade          VARCHAR(32) COMMENT '风险等级',
    case_status         VARCHAR(32) DEFAULT 'working' COMMENT '案件状态',
    closing_method      VARCHAR(32) COMMENT '结案方式',
    summary             TEXT COMMENT '案件小结',
    replay              TEXT COMMENT '案件复盘',
    create_time         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator             VARCHAR(64) COMMENT '创建人',
    update_time         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag         INT         DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '诉讼类案件表 ';

ALTER TABLE t_fwfx_litigation_cases
    COMMENT '诉讼类案件表';
DROP TABLE if exists t_fwfx_risk;/*SkipError*/
CREATE TABLE t_fwfx_risk
(
    id                  VARCHAR(64) NOT NULL COMMENT '主键',
    registrate_time     DATETIME COMMENT '登记日期',
    legal_entity        VARCHAR(64) COMMENT '风险发生法人主体',
    business_unit       VARCHAR(32) COMMENT '业务部门',
    risk_grade          VARCHAR(32) COMMENT '风险等级',
    risk_type           VARCHAR(32) COMMENT '风险类型',
    service_personal    TEXT COMMENT '服务人员',
    cooperation_person  TEXT COMMENT '协作人员',
    risk_matter         TEXT COMMENT '风险事项',
    risk_analysis       TEXT COMMENT '风险分析',
    preliminary_opinion TEXT COMMENT '初步应对策略',
    event_progress      TEXT COMMENT '事件进展',
    summary             TEXT COMMENT '总结/复盘',
    event_status        VARCHAR(32) DEFAULT 'working' COMMENT '事件状态',
    create_time         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator             VARCHAR(64) COMMENT '创建人',
    update_time         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag         INT         DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '风险信息管理表';

ALTER TABLE t_fwfx_risk
    COMMENT '风险信息管理表';
DROP TABLE if exists t_fwfx_enclosure_files;/*SkipError*/
CREATE TABLE t_fwfx_enclosure_files
(
    id            VARCHAR(64) NOT NULL COMMENT '主键',
    business_id   VARCHAR(64) COMMENT '关联主键',
    business_type VARCHAR(32) COMMENT '所属业务类型',
    file_name     VARCHAR(64) COMMENT '附件名称',
    folder_code   VARCHAR(64) COMMENT '文件夹路径',
    file_size     BIGINT COMMENT '附件大小 单位KB',
    file_type     VARCHAR(32) COMMENT '附件类型',
    group_name    VARCHAR(64) COMMENT '组名',
    file_path     VARCHAR(3072) COMMENT '附件path',
    file_url      TEXT COMMENT '下载地址',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator       VARCHAR(64) COMMENT '创建人',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag   INT      DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '附件表 ';
DROP TABLE if exists t_fwfx_enclosure_files;/*SkipError*/
CREATE TABLE t_fwfx_enclosure_files
(
    id            VARCHAR(64) NOT NULL COMMENT '主键',
    business_id   VARCHAR(64) COMMENT '关联主键',
    business_type VARCHAR(32) COMMENT '所属业务类型',
    file_name     VARCHAR(64) COMMENT '附件名称',
    folder_code   VARCHAR(64) COMMENT '文件夹路径',
    file_size     BIGINT COMMENT '附件大小 单位KB',
    file_type     VARCHAR(32) COMMENT '附件类型',
    group_name    VARCHAR(64) COMMENT '组名',
    file_path     VARCHAR(3072) COMMENT '附件path',
    file_url      TEXT COMMENT '下载地址',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator       VARCHAR(64) COMMENT '创建人',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag   INT      DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '附件表 ';

ALTER TABLE t_fwfx_enclosure_files
    COMMENT '附件表';
DROP TABLE if exists t_fwfx_dictionary;/*SkipError*/
CREATE TABLE t_fwfx_dictionary
(
    id          VARCHAR(64) NOT NULL COMMENT '主键',
    name        VARCHAR(64) COMMENT '名称',
    code        VARCHAR(64) COMMENT '编码',
    sort        BIGINT DEFAULT 0 COMMENT '排序',
    parent_id   VARCHAR(64) COMMENT '父主键',
    parent_code VARCHAR(64) COMMENT '父编码',
    PRIMARY KEY (id)
) COMMENT = '字典表 ';

ALTER TABLE t_fwfx_dictionary
    COMMENT '字典表';
DROP TABLE if exists t_fwfx_hear_information;/*SkipError*/
CREATE TABLE t_fwfx_hear_information
(
    id                  VARCHAR(64) NOT NULL COMMENT '主键',
    relation_id         VARCHAR(64) NOT NULL COMMENT '关联主键',
    hearing_organ       VARCHAR(32) COMMENT '审理机构',
    hearing_procedure   VARCHAR(32) COMMENT '审理程序',
    filing_date         DATE COMMENT '立案受理时间',
    open_date           DATE COMMENT '开庭时间',
    case_analysis       TEXT COMMENT '案件分析',
    preliminary_opinion TEXT COMMENT '初步处理/答复意见',
    closing_method      VARCHAR(32) COMMENT '结案方式',
    sort                INT COMMENT '顺序',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator             VARCHAR(64) COMMENT '创建人',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag         INT      DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '审理信息表 ';

ALTER TABLE t_fwfx_hear_information
    COMMENT '审理信息表';
DROP TABLE if exists t_fwfx_process_information;/*SkipError*/
CREATE TABLE t_fwfx_process_information
(
    id          VARCHAR(64) NOT NULL COMMENT '主键',
    relation_id VARCHAR(64) NOT NULL COMMENT '关联主键',
    data_type   VARCHAR(32) COMMENT '数据类型',
    content     TEXT COMMENT '进展内容',
    sort        INT DEFAULT 1 COMMENT '顺序',
    PRIMARY KEY (id)
) COMMENT = '进展信息表';

ALTER TABLE t_fwfx_process_information
    COMMENT '进展信息表';
DROP TABLE if exists t_fwfx_system_log;/*SkipError*/
CREATE TABLE t_fwfx_system_log(
    id VARCHAR(64) NOT NULL   COMMENT '主键' ,
    operation_data TEXT    COMMENT '操作人信息' ,
    request_type VARCHAR(64)    COMMENT '请求类型' ,
    request_url TEXT    COMMENT '请求地址' ,
    request_data TEXT    COMMENT '请求数据' ,
    response_data TEXT    COMMENT '返回数据' ,
    request_time DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '时间' ,
    PRIMARY KEY (id)
) COMMENT = '系统日志表 ';

ALTER TABLE t_fwfx_system_log
    COMMENT '系统日志表';
DROP TABLE if exists t_fwfx_remind_todo;/*SkipError*/
CREATE TABLE t_fwfx_remind_todo
(
    id                  VARCHAR(64) NOT NULL COMMENT '主键',
    relation_id         VARCHAR(64) COMMENT '关联主键',
    data_type           VARCHAR(32) COMMENT '数据类型',
    personnel_id        VARCHAR(64) COMMENT '提醒人员主键',
    name                VARCHAR(32) COMMENT '名字',
    event_title         VARCHAR(1024) COMMENT '事件名称',
    target_email        VARCHAR(1024) COMMENT '目标邮箱',
    remind_date         DATETIME COMMENT '提醒日期',
    remind_date_regular VARCHAR(64) COMMENT '提醒日期正则',
    send_flag           INT      DEFAULT 0 COMMENT '是否已发送 0未发送(默认)，1已发送',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    creator             VARCHAR(64) COMMENT '创建人',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag         INT      DEFAULT 0 COMMENT '删除标记 0未删除，1删除',
    PRIMARY KEY (id)
) COMMENT = '提醒记录表 ';

ALTER TABLE t_fwfx_remind_todo
    COMMENT '提醒记录表';
