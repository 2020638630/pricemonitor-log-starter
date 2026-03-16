package com.pricemonitor.starter.bizlog.processor;

import com.pricemonitor.starter.bizlog.dto.BusinessLogDTO;

/**
 * 业务日志处理器接口
 * 用于处理业务日志的持久化、发送等操作
 * 使用者可以通过实现此接口来定义自己的日志处理逻辑
 *
 * @author pricemonitor
 */
public interface BusinessLogProcessor {

    /**
     * 处理业务日志
     *
     * @param logDTO 业务日志DTO
     */
    void process(BusinessLogDTO logDTO);

    /**
     * 是否支持处理该类型的日志
     *
     * @param logDTO 业务日志DTO
     * @return true-支持，false-不支持
     */
    default boolean support(BusinessLogDTO logDTO) {
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
