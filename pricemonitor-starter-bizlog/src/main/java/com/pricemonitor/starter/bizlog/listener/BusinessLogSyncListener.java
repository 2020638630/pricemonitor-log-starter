package com.pricemonitor.starter.bizlog.listener;

import com.pricemonitor.starter.bizlog.event.BusinessLogSyncEvent;
import com.pricemonitor.starter.bizlog.processor.BusinessLogProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 同步业务日志监听器
 * 使用同步方式处理业务日志事件，确保日志处理完成后再继续业务流程
 *
 * @author pricemonitor
 */
@Slf4j
@Order(5)
public class BusinessLogSyncListener extends AbstractBusinessLogListener {

    /**
     * 构造函数
     *
     * @param processors 业务日志处理器列表
     */
    public BusinessLogSyncListener(List<BusinessLogProcessor> processors) {
        super(processors);
    }

    /**
     * 监听同步业务日志事件
     *
     * @param event 业务日志事件
     */
    @EventListener(BusinessLogSyncEvent.class)
    public void handleSyncEvent(BusinessLogSyncEvent event) {
        log.info("收到同步业务日志事件: code={}", event.getSource().getCode());
        handleEvent(event);
    }
}
