package com.pricemonitor.starter.bizlog.listener;

import com.pricemonitor.starter.bizlog.dto.BusinessLogDTO;
import com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 默认业务日志处理器
 * 
 * <p>当用户没有自定义 BusinessLogProcessor 时，使用此默认处理器将日志输出到日志文件。
 * 避免因缺少处理器而导致日志丢失。</p>
 * 
 * <p>建议：在生产环境中，用户应该实现自定义的 BusinessLogProcessor 来将日志持久化到数据库或其他存储。</p>
 * 
 * @author pricemonitor
 */
@Slf4j
public class DefaultBusinessLogProcessor implements BusinessLogProcessor, Ordered {

    /**
     * 处理业务日志（输出到日志文件）
     *
     * @param logDTO 业务日志DTO
     */
    @Override
    public void process(BusinessLogDTO logDTO) {
        log.info("========== 业务日志 ==========");
        log.info("业务编码: {}", logDTO.getCode());
        log.info("业务描述: {}", logDTO.getDescription());
        log.info("用户ID: {}", logDTO.getUserId());
        log.info("操作人: {}", logDTO.getUserName());
        log.info("租户ID: {}", logDTO.getTenantId());
        log.info("用户类型: {}", logDTO.getUserType());
        log.info("请求参数: {}", logDTO.getParams());
        log.info("执行结果: {}", logDTO.getResult());
        log.info("创建时间: {}", logDTO.getCreateTime());
        log.info("==============================");
    }

    /**
     * 始终支持处理（作为兜底处理器）
     *
     * @param logDTO 业务日志DTO
     * @return true
     */
    @Override
    public boolean support(BusinessLogDTO logDTO) {
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
