package com.pricemonitor.starter.syslog.listener;

import com.pricemonitor.starter.syslog.dto.SystemLogDTO;
import com.pricemonitor.starter.syslog.processor.SystemLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.util.Comparator;
import java.util.List;

/**
 * 抽象系统日志监听器
 * 负责监听日志事件并调用处理器进行处理
 *
 * @author pricemonitor
 */
@Slf4j
public abstract class AbstractSystemLogListener {

    private final List<SystemLogProcessor> processors;

    /**
     * 构造函数
     *
     * @param processors 系统日志处理器列表
     */
    protected AbstractSystemLogListener(List<SystemLogProcessor> processors) {
        // 按优先级排序
        processors.sort(Comparator.comparingInt(SystemLogProcessor::getOrder));
        this.processors = processors;
    }

    /**
     * 处理日志事件
     *
     * @param event 日志事件
     */
    protected void handleEvent(ApplicationEvent event) {
        SystemLogDTO logDTO = extractLogDTO(event);
        if (logDTO == null) {
            log.warn("无法从事件中提取系统日志数据");
            return;
        }

        if (processors == null || processors.isEmpty()) {
            log.warn("没有配置系统日志处理器，日志将被忽略: title={}", logDTO.getTitle());
            return;
        }

        // 调用所有支持的处理器
        for (SystemLogProcessor processor : processors) {
            try {
                if (processor.support(logDTO)) {
                    processor.process(logDTO);
                }
            } catch (Exception e) {
                log.error("系统日志处理器处理失败: processor={}, title={}",
                        processor.getClass().getSimpleName(), logDTO.getTitle(), e);
            }
        }
    }

    /**
     * 从事件中提取日志DTO
     *
     * @param event 日志事件
     * @return 系统日志DTO
     */
    protected SystemLogDTO extractLogDTO(ApplicationEvent event) {
        Object source = event.getSource();
        if (source instanceof SystemLogDTO) {
            return (SystemLogDTO) source;
        }
        return null;
    }
}
