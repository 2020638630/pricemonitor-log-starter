package com.pricemonitor.starter.syslog.listener;

import com.pricemonitor.starter.syslog.dto.SystemLogDTO;
import com.pricemonitor.starter.syslog.processor.SystemLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 默认系统日志处理器
 * 
 * <p>当用户没有自定义 SystemLogProcessor 时，使用此默认处理器将日志输出到日志文件。
 * 避免因缺少处理器而导致日志丢失。</p>
 * 
 * <p>建议：在生产环境中，用户应该实现自定义的 SystemLogProcessor 来将日志持久化到数据库或其他存储。</p>
 * 
 * @author pricemonitor
 */
@Slf4j
public class DefaultSystemLogProcessor implements SystemLogProcessor, Ordered {

    /**
     * 处理系统日志（输出到日志文件）
     *
     * @param logDTO 系统日志DTO
     */
    @Override
    public void process(SystemLogDTO logDTO) {
        log.info("========== 系统日志 ==========");
        log.info("服务ID: {}", logDTO.getServiceId());
        log.info("日志类型: {}", logDTO.getType());
        log.info("链路追踪ID: {}", logDTO.getTraceId());
        log.info("业务模块: {}", logDTO.getModule());
        log.info("日志标题: {}", logDTO.getTitle());
        log.info("操作描述: {}", logDTO.getDescription());
        log.info("HTTP方法: {}", logDTO.getMethod());
        log.info("请求URL: {}", logDTO.getUrl());
        log.info("客户端IP: {}", logDTO.getIp());
        log.info("请求参数: {}", logDTO.getParams());
        log.info("执行耗时: {}ms", logDTO.getExecuteTime());
        log.info("执行结果: {}", logDTO.getResult());
        log.info("用户ID: {}", logDTO.getUserId());
        log.info("操作人: {}", logDTO.getOperator());
        log.info("租户ID: {}", logDTO.getTenantId());
        log.info("创建时间: {}", logDTO.getCreateTime());
        if (logDTO.getException() != null) {
            log.info("异常信息: {}", logDTO.getException());
        }
        log.info("==============================");
    }

    /**
     * 始终支持处理（作为兜底处理器）
     *
     * @param logDTO 系统日志DTO
     * @return true
     */
    @Override
    public boolean support(SystemLogDTO logDTO) {
        return true;
    }

    /**
     * 设置最低优先级（确保自定义处理器优先执行）
     *
     * @return 最大值，表示最低优先级
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
