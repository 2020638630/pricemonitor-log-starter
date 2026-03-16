package com.pricemonitor.starter.syslog.listener;

import com.pricemonitor.starter.syslog.event.SystemLogEvent;
import com.pricemonitor.starter.syslog.processor.SystemLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统日志监听器（异步）
 * 使用异步方式处理系统日志事件，不阻塞业务流程
 *
 * @author pricemonitor
 */
@Slf4j
@Order(10)
public class SystemLogListener extends AbstractSystemLogListener {

    /**
     * 构造函数
     *
     * @param processors 系统日志处理器列表
     */
    public SystemLogListener(List<SystemLogProcessor> processors) {
        super(processors);
    }

    /**
     * 监听系统日志事件
     *
     * @param event 系统日志事件
     */
    @Async
    @EventListener(SystemLogEvent.class)
    public void handleAsyncEvent(SystemLogEvent event) {
        log.info("收到系统日志事件: title={}, type={}",
                event.getSource().getTitle(), event.getSource().getType());
        handleEvent(event);
    }
}
