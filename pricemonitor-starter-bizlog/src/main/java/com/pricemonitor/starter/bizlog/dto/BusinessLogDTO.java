package com.pricemonitor.starter.bizlog.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 业务日志数据传输对象
 *
 * @author pricemonitor
 */
@Data
@Accessors(chain = true)
public class BusinessLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务编码
     */
    private String code;

    /**
     * 业务描述
     */
    private String description;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 执行结果
     */
    private Object result;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户ID（多租户场景）
     */
    private String tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 扩展信息
     */
    private Map<String, Object> extensions;
}
