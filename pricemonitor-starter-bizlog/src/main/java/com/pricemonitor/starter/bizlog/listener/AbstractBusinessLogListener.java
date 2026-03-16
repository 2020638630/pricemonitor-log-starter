package com.pricemonitor.starter.bizlog.listener;

import com.pricemonitor.starter.bizlog.dto.BusinessLogDTO;
import com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.util.Comparator;
import java.util.List;

/**
 * 抽象业务日志监听器
 * 负责监听日志事件并调用处理器进行处理
 *
 * @author pricemonitor
 */
@Slf4j
public abstract class AbstractBusinessLogListener {

    private final List<BusinessLogProcessor> processors;

    /**
     * 构造函数
     *
     * @param processors 业务日志处理器列表
     */
    protected AbstractBusinessLogListener(List<BusinessLogProcessor> processors) {
        // 按优先级排序
        processors.sort(Comparator.comparingInt(BusinessLogProcessor::getOrder));
        this.processors = processors;
    }

    /**
     * 处理日志事件
     *
     * @param event 日志事件
     */
    protected void handleEvent(ApplicationEvent event) {
        BusinessLogDTO logDTO = extractLogDTO(event);
        if (logDTO == null) {
            log.warn("无法从事件中提取业务日志数据");
            return;
        }

        if (processors == null || processors.isEmpty()) {
            log.warn("没有配置业务日志处理器，日志将被忽略: code={}", logDTO.getCode());
            return;
        }

        // 调用所有支持的处理器
        for (BusinessLogProcessor processor : processors) {
            try {
                if (processor.support(logDTO)) {
                    processor.process(logDTO);
                }
            } catch (Exception e) {
                log.error("业务日志处理器处理失败: processor={}, code={}",
                        processor.getClass().getSimpleName(), logDTO.getCode(), e);
            }
        }
    }

    /**
     * 从事件中提取日志DTO
     *
     * @param event 日志事件
     * @return 业务日志DTO
     */
    protected BusinessLogDTO extractLogDTO(ApplicationEvent event) {
        Object source = event.getSource();
        if (source instanceof BusinessLogDTO) {
            return (BusinessLogDTO) source;
        }
        return null;
    }
}
