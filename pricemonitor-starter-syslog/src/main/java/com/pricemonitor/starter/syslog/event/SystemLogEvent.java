package com.pricemonitor.starter.syslog.event;

import com.pricemonitor.starter.syslog.dto.SystemLogDTO;
import org.springframework.context.ApplicationEvent;

/**
 * 系统日志事件
 * 用于发布系统日志事件，供监听器处理
 *
 * @author pricemonitor
 */
public class SystemLogEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param source 事件源（SystemLogDTO对象）
     */
    public SystemLogEvent(SystemLogDTO source) {
        super(source);
    }

    /**
     * 获取系统日志DTO
     *
     * @return 系统日志DTO
     */
    @Override
    public SystemLogDTO getSource() {
        return (SystemLogDTO) super.getSource();
    }
}
