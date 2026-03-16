package com.pricemonitor.starter.bizlog.listener;

import com.pricemonitor.starter.bizlog.event.BusinessLogEvent;
import com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异步业务日志监听器
 * 使用异步方式处理业务日志事件，不阻塞业务流程
 *
 * @author pricemonitor
 */
@Slf4j
@Order(10)
public class BusinessLogAsyncListener extends AbstractBusinessLogListener {

    /**
     * 构造函数
     *
     * @param processors 业务日志处理器列表
     */
    public BusinessLogAsyncListener(List<BusinessLogProcessor> processors) {
        super(processors);
    }

    /**
     * 监听异步业务日志事件
     *
     * @param event 业务日志事件
     */
    @Async
    @EventListener(BusinessLogEvent.class)
    public void handleAsyncEvent(BusinessLogEvent event) {
        log.info("收到异步业务日志事件: code={}", event.getSource().getCode());
        handleEvent(event);
    }
}
