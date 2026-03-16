package com.pricemonitor.starter.syslog.processor;

import com.pricemonitor.starter.syslog.dto.SystemLogDTO;

/**
 * 系统日志处理器接口
 * 用于处理系统日志的持久化、发送等操作
 * 使用者可以通过实现此接口来定义自己的日志处理逻辑
 *
 * @author pricemonitor
 */
public interface SystemLogProcessor {

    /**
     * 处理系统日志
     *
     * @param logDTO 系统日志DTO
     */
    void process(SystemLogDTO logDTO);

    /**
     * 是否支持处理该类型的日志
     * 可以根据日志类型、模块等进行判断
     *
     * @param logDTO 系统日志DTO
     * @return true-支持，false-不支持
     */
    default boolean support(SystemLogDTO logDTO) {
        return true;
    }

    /**
     * 获取处理器优先级
     * 数值越小优先级越高
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }
}
