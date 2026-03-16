package com.pricemonitor.starter.syslog.configuration;

import com.pricemonitor.starter.syslog.aspect.SystemLogAspect;
import com.pricemonitor.starter.syslog.listener.SystemLogListener;
import com.pricemonitor.starter.syslog.processor.SystemLogProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 系统日志自动配置类
 * 精确配置系统日志相关组件，避免全包扫描
 *
 * @author pricemonitor
 */
@Configuration
public class SystemLogAutoConfiguration {

    /**
     * 系统日志切面
     */
    @Bean
    public SystemLogAspect systemLogAspect(ApplicationContext applicationContext) {
        return new SystemLogAspect(applicationContext);
    }

    /**
     * 系统日志监听器
     */
    @Bean
    public SystemLogListener systemLogListener(List<SystemLogProcessor> processors) {
        return new SystemLogListener(processors);
    }

    /**
     * 默认系统日志处理器
     * 如果没有自定义实现，则使用默认处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public SystemLogProcessor defaultSystemLogProcessor() {
        return new com.pricemonitor.starter.syslog.listener.DefaultSystemLogProcessor();
    }
}
