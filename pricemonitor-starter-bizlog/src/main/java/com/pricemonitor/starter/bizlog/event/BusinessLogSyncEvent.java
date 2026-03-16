package com.pricemonitor.starter.bizlog.event;

import com.pricemonitor.starter.bizlog.dto.BusinessLogDTO;
import org.springframework.context.ApplicationEvent;

/**
 * 业务日志事件（同步）
 * 用于发布同步业务日志事件
 *
 * @author pricemonitor
 */
public class BusinessLogSyncEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param source 事件源（BusinessLogDTO对象）
     */
    public BusinessLogSyncEvent(BusinessLogDTO source) {
        super(source);
    }

    /**
     * 获取业务日志DTO
     *
     * @return 业务日志DTO
     */
    @Override
    public BusinessLogDTO getSource() {
        return (BusinessLogDTO) super.getSource();
    }
}
