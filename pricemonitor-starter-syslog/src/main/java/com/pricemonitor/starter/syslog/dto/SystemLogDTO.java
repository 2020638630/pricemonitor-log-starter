package com.pricemonitor.starter.syslog.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志数据传输对象
 *
 * @author pricemonitor
 */
@Data
@Accessors(chain = true)
public class SystemLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务ID（应用名称）
     */
    private String serviceId;

    /**
     * 日志类型（INFO/ERROR）
     */
    private String type;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 日志标题
     */
    private String title;

    /**
     * 操作描述
     */
    private String description;

    /**
     * HTTP方法（GET/POST/PUT/DELETE等）
     */
    private String method;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 客户端IP地址
     */
    private String ip;

    /**
     * 执行耗时（毫秒）
     */
    private Long executeTime;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 操作用户
     */
    private String operator;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID（多租户场景）
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 异常信息
     */
    private String exception;
}
